package org.example

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
    private val onExitToMenu: () -> Unit = { Unit },
    /** Fábrica del mundo físico (Inversión de Dependencias: un test podría simularlo). */
    private val physicsFactory: () -> PhysicsWorld = SlingshotDefaults.physicsFactory(config),
    /** Fábrica de los colaboradores del nivel con resortera (DIP). */
    private val levelFactory: (Pane, PhysicsWorld) -> SlingshotLevelComponents = SlingshotDefaults.levelFactory(spriteSource, config)
) {
    protected var physics: PhysicsWorld = physicsFactory()
    private var gameLoop: GameLoop? = null

    // Colaboradores del nivel (creados por la fábrica).
    private lateinit var slingshot: SlingshotView
    protected lateinit var launchZone: LaunchZone
    private lateinit var pile: BirdPile
    protected val entityFactory: EntityFactory get() = levelComponents.entityFactory
    private lateinit var levelComponents: SlingshotLevelComponents

    // Hitbox al raz del piso (unos píxeles), con borde rojo para verla.
    private lateinit var floorHitbox: BoxHitbox
    // Pájaro que se está arrastrando desde la pila hacia la resortera
    private var dragBird: Bird? = null

    // Pájaro fijado en la resortera (listo para estirar)
    private var slingshotBird: Bird? = null

    // Seguimiento de pájaros en vuelo/asentados (SRP).
    private lateinit var flightTracker: FlightTracker

    // Pájaros ya lanzados que siguen en vuelo (delegado al tracker).
    protected val inFlight: MutableList<Bird> get() = flightTracker.inFlight

    // Último pájaro lanzado; la habilidad especial solo aplica a él.
    protected var lastLaunched: Bird? = null

    protected lateinit var pane: Pane

    /** Maneja la entrada de mouse/teclado, separado para reducir el tamaño de esta clase (SRP). */
    private val input = InputHandler()

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
        scene.setOnKeyPressed { e -> input.handleKeyPressed(e) }
        scene.setOnMousePressed { e -> input.handlePressed(e) }
        scene.setOnMouseDragged { e -> input.handleDragged(e) }
        scene.setOnMouseReleased { e -> input.handleReleased(e) }
        return scene
    }

    fun resetLevel() {
        stopPhysicsLoop()
        physics.dispose()
        physics = physicsFactory()
        dragBird = null
        slingshotBird = null
        lastLaunched = null
        flightTracker.clear()

        pane.children.clear()
        setupWorld()
        onLevelReset()
        startPhysicsLoop()
    }

    /** Fondo + resortera + zona de lanzamiento, luego lo propio del nivel. */
    private fun setupWorld() {
        levelComponents = levelFactory(pane, physics)
        slingshot = levelComponents.slingshot
        launchZone = levelComponents.launchZone
        pile = levelComponents.pile

        // Hitbox al raz del piso (invisible).
        floorHitbox = SlingshotDefaults.floorHitbox(config)

        flightTracker = FlightTracker(physics, pane, floorHitbox) { onBirdSettled(it) }

        setupLevel()
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
        lastLaunched = bird
        flightTracker.recordLaunch(bird)

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

    // ── Input delegado a InputHandler ─────────────────────────────────────

    // ── Game loop (60 fps fijos) ──────────────────────────────────────

    private fun startPhysicsLoop() {
        gameLoop = SlingshotDefaults.gameLoop(
            physics = physics,
            onStep = { dt -> onPhysicsStep(dt) },
            onRemoveSettled = { removeSettledBirds() },
            onTickSettled = { dt -> flightTracker.tick(dt) }
        )
        gameLoop?.start()
    }

    private fun stopPhysicsLoop() {
        gameLoop?.stop()
        gameLoop = null
    }

    /**
     * Retira del mundo los pájaros lanzados que ya quedaron en reposo.
     * NO limpia [lastLaunched]: la bomba debe poder explotar con G aunque
     * ya haya caído al piso (solo se reemplaza al lanzar otro pájaro).
     */
    private fun removeSettledBirds() {
        flightTracker.removeSettled()
    }

    /** Hace desaparecer los pájaros que llevan ≥ 3 s en el piso. */
    /** Marca un pájaro como asentado (p. ej. salió de la pantalla) para que se desvanezca. */
    protected fun markBirdSettled(bird: Bird) = flightTracker.markSettled(bird)

    /** Manejador de entrada (mouse/teclado), extraído para reducir el tamaño de esta clase (SRP). */
    inner class InputHandler {
        fun handlePressed(e: MouseEvent) {
            val mx = e.sceneX
            val my = e.sceneY

            if (this@SlingshotGame.slingshotBird != null) {
                this@SlingshotGame.slingshot.forceStartDrag(mx, my)
                return
            }

            val bird = this@SlingshotGame.pile.available().firstOrNull { isNearBird(it, mx, my) }
            if (bird != null) {
                this@SlingshotGame.dragBird = bird
                bird.placeAt(this@SlingshotGame.pane, mx - bird.width / 2, my - bird.height / 2)
                this@SlingshotGame.onBirdGrabbed()
            }
        }

        fun handleDragged(e: MouseEvent) {
            val mx = e.sceneX
            val my = e.sceneY

            if (this@SlingshotGame.slingshot.isDragging) {
                val loaded = this@SlingshotGame.slingshotBird ?: return
                this@SlingshotGame.slingshot.drag(mx, my)
                val pos = this@SlingshotGame.slingshot.birdStretchPos
                loaded.placeAt(this@SlingshotGame.pane, pos.x - loaded.width / 2, pos.y - loaded.height / 2)
                this@SlingshotGame.physics.setTransform(loaded, pos.x, pos.y)
                this@SlingshotGame.onStretchUpdate(this@SlingshotGame.slingshot.pull, pos)
                return
            }

            if (this@SlingshotGame.slingshotBird != null) return
            val bird = this@SlingshotGame.dragBird ?: return
            bird.placeAt(this@SlingshotGame.pane, mx - bird.width / 2, my - bird.height / 2)
        }

        fun handleReleased(e: MouseEvent) {
            val mx = e.sceneX
            val my = e.sceneY

            if (this@SlingshotGame.slingshot.isDragging) {
                val loaded = this@SlingshotGame.slingshotBird ?: return
                this@SlingshotGame.clearAimLayer()
                val pull = this@SlingshotGame.slingshot.release() ?: return
                if (pull.magnitude() > MIN_STRETCH) {
                    this@SlingshotGame.launchBird(loaded, pull)
                } else {
                    loaded.placeAt(this@SlingshotGame.pane, this@SlingshotGame.slingshot.anchor.x - loaded.width / 2, this@SlingshotGame.slingshot.anchor.y - loaded.height / 2)
                    this@SlingshotGame.physics.setTransform(loaded, this@SlingshotGame.slingshot.anchor.x, this@SlingshotGame.slingshot.anchor.y)
                }
                return
            }

            if (this@SlingshotGame.slingshotBird != null) return
            val bird = this@SlingshotGame.dragBird ?: return
            this@SlingshotGame.dragBird = null
            if (this@SlingshotGame.launchZone.contains(mx, my)) {
                this@SlingshotGame.loadBirdOntoSlingshot(bird)
            } else {
                this@SlingshotGame.pile.putBack(bird)
            }
        }

        fun handleKeyPressed(e: KeyEvent) {
            when (e.code) {
                KeyCode.ESCAPE -> {
                    this@SlingshotGame.stopPhysicsLoop()
                    this@SlingshotGame.onExitToMenu()
                }
                KeyCode.G -> this@SlingshotGame.onAbilityKey()
                KeyCode.R -> this@SlingshotGame.resetLevel()
                else -> Unit
            }
        }

        private fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
            val cx = bird.x + bird.width / 2
            val cy = bird.y + bird.height / 2
            return Point2D(x, y).distance(cx, cy) < this@SlingshotGame.grabRadius
        }
    }

    protected companion object {
        /** Estiramiento mínimo para considerarlo un tiro válido. */
        const val MIN_STRETCH = 10.0

        /** Segundos que un pájaro queda en el piso antes de desvanecerse. */
        const val SETTLE_FADE_START = 0.0
    }
}
