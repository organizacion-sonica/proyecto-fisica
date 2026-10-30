package org.example

import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import kotlin.math.min

/**
 * Base común de todos los minijuegos con resortera (DRY): maneja la pila
 * de pájaros, la carga en la resortera, el estiramiento, el lanzamiento, la
 * entrada de mouse/teclado y el game loop a 60 fps.
 *
 * Las subclases solo implementan lo propio del nivel (SRP):
 *  - [setupLevel]: qué aparece en la escena, además del fondo y la resortera.
 *  - [onPhysicsStep]: qué pasa en cada paso fijo de física.
 *  - hooks opcionales (habilidad especial, avisos de pájaro asentado, etc.)
 */
abstract class SlingshotGame(
    protected val spriteSource: SpriteSource,
    protected val config: GameConfig,
    /** Callback para volver al menú principal (tecla ESC). */
    private val onExitToMenu: () -> Unit = {},
    /** Fábrica del mundo físico (Inversión de Dependencias: un test podría simularlo). */
    private val physicsFactory: () -> PhysicsWorld = { Box2DWorld(config) }
) {
    protected var physics: PhysicsWorld = physicsFactory()
    protected val entityFactory = EntityFactory(spriteSource, config)

    // Pila de pájaros (se crea al armar la escena, junto con la resortera).
    private lateinit var pile: BirdPile

    // Pájaro que se está arrastrando desde la pila hacia la resortera
    private var dragBird: Bird? = null

    // Pájaro fijado en la resortera (listo para estirar)
    private var slingshotBird: Bird? = null

    // Pájaros ya lanzados que siguen en vuelo
    protected val inFlight = mutableListOf<Bird>()

    // Hitbox al raz del piso: el pájaro se desvanece al tocarla.
    private lateinit var floorHitbox: BoxHitbox
    private lateinit var floorVisual: javafx.scene.shape.Rectangle

    protected val inFlightTime = mutableMapOf<Bird, Double>()

    // Pájaros que quedaron en el piso y cuánto tiempo llevan ahí
    // (se desvanecen tras SETTLE_FADE_START segundos).
    protected val settledBirds = mutableMapOf<Bird, Double>()
    protected val fadingBirds = mutableSetOf<Bird>()

    // Último pájaro lanzado; la habilidad especial solo aplica a él.
    protected var lastLaunched: Bird? = null

    private lateinit var slingshot: SlingshotView
    private lateinit var launchZone: LaunchZone
    protected lateinit var pane: Pane

    private var physicsTimer: AnimationTimer? = null
    private val grabRadius = 45.0 * config.scale

    // ── Construcción de la escena ─────────────────────────────────────

    fun buildScene(): Scene {
        pane = Pane()
        pane.prefWidth = config.windowWidth
        pane.prefHeight = config.windowHeight
        pane.isFocusTraversable = true

        setupWorld()
        startPhysicsLoop()

        val scene = Scene(pane, config.windowWidth, config.windowHeight)
        scene.setOnKeyPressed { e -> handleKeyPressed(e) }
        scene.setOnMousePressed { e -> handlePressed(e) }
        scene.setOnMouseDragged { e -> handleDragged(e) }
        scene.setOnMouseReleased { e -> handleReleased(e) }
        return scene
    }

    fun resetLevel() {
        physicsTimer?.stop()
        physics.dispose()
        physics = physicsFactory()
        dragBird = null
        slingshotBird = null
        inFlight.clear()
        lastLaunched = null
        settledBirds.clear()
        fadingBirds.clear()
        inFlightTime.clear()

        pane.children.clear()
        setupWorld()
        onLevelReset()
        startPhysicsLoop()
    }

    /** Fondo + resortera + zona de lanzamiento, luego lo propio del nivel. */
    private fun setupWorld() {
        BackgroundView(spriteSource, config).placeIn(pane)

        slingshot = SlingshotView(spriteSource, config)
        slingshot.placeIn(pane)

        launchZone = LaunchZone(config, slingshot.anchor.x, slingshot.anchor.y)

        // Hitbox al raz del piso (unos píxeles), con borde rojo para verla.
        floorHitbox = BoxHitbox(halfWidth = config.windowWidth / 2.0, halfHeight = 5.0)
        floorHitbox.recenter(config.windowWidth / 2.0, config.windowHeight - 5.0)
        val fv = javafx.scene.shape.Rectangle(
            0.0,
            config.windowHeight - 10.0,
            config.windowWidth,
            10.0
        ).apply {
            fill = javafx.scene.paint.Color.TRANSPARENT
            stroke = javafx.scene.paint.Color.RED
            strokeWidth = 1.0
        }
        pane.children.add(fv)
        floorVisual = fv

        setupLevel()

        pile = BirdPile(pane, entityFactory, config)
        pile.setup()
    }

    /** Construye lo propio del nivel (dianas, aro, marcadores...). */
    protected abstract fun setupLevel()

    /** Ajustes al reiniciar el nivel (puntaje a cero, cronómetro...). */
    protected open fun onLevelReset() { Unit }

    /** Lógica propia del nivel en cada paso fijo de física. */
    protected abstract fun onPhysicsStep(dt: Double)

    /** Se sigue estirando: efectos de ayuda de tiro (ángulo, trayectoria...). */
    protected open fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) { Unit }

    /** Se suelta la resortera (antes de decidir si el tiro es válido). */
    protected open fun clearAimLayer() { Unit }

    /** Tecla G: habilidad especial del último pájaro lanzado, si la hay. */
    protected open fun onAbilityKey() { Unit }

    /** Tras lanzar un pájaro (para que cada nivel limpie su estado). */
    protected open fun onLaunch() { Unit }

    /** Un pájaro lanzado quedó en reposo (puntos de basket, etc.). */
    protected open fun onBirdSettled(bird: Bird) { Unit }

    /** Se acaba de agarrar un pájaro (de la pila o para la resortera). */
    protected open fun onBirdGrabbed() { Unit }

    /** Un pájaro quedó fijado en la resortera (momento en que arranca el timer). */
    protected open fun onBirdLoadedOntoSlingshot() { Unit }

    // ── Carga de pájaros ──────────────────────────────────────────────

    /**
     * Fija el pájaro en la resortera: queda bloqueado hasta lanzarlo. El
     * slot se repone al instante con un pájaro nuevo del mismo tipo.
     */
    private fun loadBirdOntoSlingshot(bird: Bird) {
        pile.withdraw(bird)

        // Si ya había un pájaro fijado, se descarta
        val old = slingshotBird
        if (old != null) {
            physics.remove(old)
            old.view.removeFrom(pane)
            slingshotBird = null
        }

        bird.placeAt(pane, slingshot.anchor.x - bird.width / 2, slingshot.anchor.y - bird.height / 2)
        physics.registerBird(bird, bird.mass)
        physics.setKinematic(bird)
        physics.setTransform(bird, slingshot.anchor.x, slingshot.anchor.y)
        slingshotBird = bird

        // BLOQUEO: el pájaro fijado no se puede sacar de la resortera;
        // solo se puede estirar y lanzar.
        dragBird = null

        onBirdGrabbed()
        onBirdLoadedOntoSlingshot()
    }

    /**
     * Lanza el pájaro. El impulso es proporcional a la dirección del pull:
     * cuanto más se estira la resortera, más rápido y lejos vuela.
     */
    private fun launchBird(bird: Bird, pull: Point2D) {
        slingshotBird = null
        inFlight += bird
        lastLaunched = bird
        inFlightTime[bird] = 0.0

        val (vx, vy) = launchVelocity(pull)
        physics.setDynamic(bird)
        physics.launchBird(bird, vx, vy)
        onLaunch()
    }

    /** Convierte el estiramiento en velocidad de lanzamiento (px/s). */
    private fun launchVelocity(pull: Point2D): Pair<Double, Double> {
        val mag = min(pull.magnitude(), config.maxStretch)
        val speed = mag / config.maxStretch * config.maxLaunchSpeed
        return pull.x / mag * speed to pull.y / mag * speed
    }

    // ── Input ─────────────────────────────────────────────────────────

    private fun handlePressed(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // 1) Si hay un pájaro fijado en la resortera → activar estiramiento
        //    (sin verificar distancia: cualquier clic activa la resortera).
        if (slingshotBird != null) {
            slingshot.forceStartDrag(mx, my)
            return
        }

        // 2) Agarrar un pájaro de la pila → empezar arrastre directo.
        val bird = pile.available().firstOrNull { isNearBird(it, mx, my) }
        if (bird != null) {
            dragBird = bird
            bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
            onBirdGrabbed()
        }
    }

    private fun handleDragged(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // Estiramiento de la resortera: el pájaro va con el pull.
        if (slingshot.isDragging) {
            val loaded = slingshotBird ?: return
            slingshot.drag(mx, my)
            val pos = slingshot.birdStretchPos
            loaded.placeAt(pane, pos.x - loaded.width / 2, pos.y - loaded.height / 2)
            physics.setTransform(loaded, pos.x, pos.y)
            onStretchUpdate(slingshot.pull, pos)
            return
        }

        // Arrastre directo del pájaro desde la pila (si no hay pájaro fijado).
        if (slingshotBird != null) return
        val bird = dragBird ?: return
        bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
    }

    private fun handleReleased(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // Soltar la resortera → lanzar si hay estiramiento suficiente.
        if (slingshot.isDragging) {
            val loaded = slingshotBird ?: return
            clearAimLayer()
            val pull = slingshot.release() ?: return
            if (pull.magnitude() > MIN_STRETCH) {
                launchBird(loaded, pull)
            } else {
                // Estiramiento insuficiente: devolver el pájaro a la resortera.
                loaded.placeAt(pane, slingshot.anchor.x - loaded.width / 2, slingshot.anchor.y - loaded.height / 2)
                physics.setTransform(loaded, slingshot.anchor.x, slingshot.anchor.y)
            }
            return
        }

        // Soltar pájaro arrastrado desde la pila.
        if (slingshotBird != null) return
        val bird = dragBird ?: return
        dragBird = null
        if (launchZone.contains(mx, my)) {
            // Soltado sobre la zona → cargar en la resortera.
            loadBirdOntoSlingshot(bird)
        } else {
            // Soltado fuera → volver a la pila.
            pile.putBack(bird)
        }
    }

    private fun handleKeyPressed(e: KeyEvent) {
        when (e.code) {
            KeyCode.ESCAPE -> {
                physicsTimer?.stop()
                onExitToMenu()
            }
            KeyCode.G -> onAbilityKey()
            KeyCode.R -> resetLevel()
            else -> {}
        }
    }

    private fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        return Point2D(x, y).distance(cx, cy) < grabRadius
    }

    // ── Game loop (60 fps fijos) ──────────────────────────────────────

    private fun startPhysicsLoop() {
        var lastTime = 0L
        var accumulator = 0.0
        val fixedDt = 1.0 / 60.0

        physicsTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (lastTime == 0L) {
                    lastTime = now
                    return
                }
                val elapsed = (now - lastTime) / 1_000_000_000.0
                lastTime = now
                accumulator += elapsed.coerceAtMost(0.1)

                while (accumulator >= fixedDt) {
                    physics.step(fixedDt)
                    onPhysicsStep(fixedDt)
                    accumulator -= fixedDt
                }

                removeSettledBirds()
                tickSettled(fixedDt)
            }
        }.also { it.start() }
    }

    /**
     * Retira del mundo los pájaros lanzados que ya quedaron en reposo.
     * NO limpia [lastLaunched]: la bomba debe poder explotar con G aunque
     * ya haya caído al piso (solo se reemplaza al lanzar otro pájaro).
     */
    private fun removeSettledBirds() {
        val resting = inFlight.filter {
            CollisionDetector.collides(it.hitbox, floorHitbox) ||
                (inFlightTime[it] ?: 0.0) > 6.0
        }
        for (bird in resting) {
            physics.remove(bird)
            inFlight.remove(bird)
            inFlightTime.remove(bird)
            onBirdSettled(bird)
            settledBirds[bird] = 0.0
        }
    }

    /** Hace desaparecer los pájaros que llevan ≥ 3 s en el piso. */
    private fun tickSettled(dt: Double) {
        // Acumular tiempo de vuelo de los pájaros aún en vuelo
        for (bird in inFlight) {
            inFlightTime[bird] = (inFlightTime[bird] ?: 0.0) + dt
        }

        val toFade = mutableListOf<Bird>()
        for ((bird, elapsed) in settledBirds) {
            if (bird in fadingBirds) continue
            val next = elapsed + dt
            if (next >= SETTLE_FADE_START) {
                toFade += bird
                settledBirds[bird] = next
            } else {
                settledBirds[bird] = next
            }
        }
        for (bird in toFade) {
            fadingBirds += bird
            bird.view.setHitboxVisible(false)
            bird.view.fadeOutAndRemove(pane) {
                settledBirds.remove(bird)
                fadingBirds.remove(bird)
            }
        }
    }

    protected companion object {
        /** Estiramiento mínimo para considerarlo un tiro válido. */
        const val MIN_STRETCH = 10.0

        /** Segundos que un pájaro queda en el piso antes de desvanecerse. */
        const val SETTLE_FADE_START = 0.0
    }
}
