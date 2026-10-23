package org.example

import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Minijuego 2: Tiro al aro de basket.
 *
 * Misma mecánica de resortera que el juego principal, pero sin línea predictora.
 * El aro de basket está en el costado derecho y cambia de posición en el eje Y.
 * Si el jugador encesta el pájaro en el aro: +1 punto.
 * Si falla: -1 punto (no puede tener puntos negativos).
 */
class BasketGameController(
    private val spriteSource: SpriteSource,
    private val config: GameConfig
) {
    private var physics = Box2DWorld(config)
    private val entityFactory = EntityFactory(spriteSource, config)

    private val slotTypes = listOf("red", "chuck", "bomb")

    // Pájaros en la pila de la esquina superior izquierda (suministro infinito).
    private val slotBirds = mutableListOf<Bird>()
    private val birdType = mutableMapOf<Bird, String>()

    // Pájaro que se está arrastrando desde la pila hacia la resortera
    private var dragBird: Bird? = null

    // Pájaro fijado en la resortera (listo para estirar)
    private var slingshotBird: Bird? = null
    // Pájaros ya lanzados que siguen en vuelo
    private val inFlight = mutableListOf<Bird>()

    private lateinit var slingshot: SlingshotView
    private lateinit var launchZone: LaunchZone
    private lateinit var pane: Pane
    private lateinit var scoreLabel: Label

    // ── Aro de basket ────────────────────────────────────────────────────
    private lateinit var hoopView: ImageView
    private var hoopY: Double = 0.0
    private val hoopWidth: Double = 140.0 * config.scale
    private val hoopHeight: Double = 140.0 * config.scale

    // Referencia al cuerpo físico del palo para poder moverlo después
    private var poleBody: org.dyn4j.dynamics.Body? = null

    // ── Puntuación ───────────────────────────────────────────────────────
    private var score = 0

    private var physicsTimer: AnimationTimer? = null
    private val grabRadius = 45.0 * config.scale

    // Tiempo para detectar si el pájaro pasó por el aro (cooldown)
    private var lastHoopCheckTime = 0.0
    private var globalTime = 0.0

    // Para evitar múltiples conteos del mismo tiro
    private var lastScoredBird: Bird? = null

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
        physics.dispose()
        physics = Box2DWorld(config)
        slotBirds.clear()
        birdType.clear()
        dragBird = null
        inFlight.clear()
        slingshotBird = null
        score = 0
        lastScoredBird = null
        globalTime = 0.0
        lastHoopCheckTime = 0.0

        pane.children.clear()
        setupWorld()
        physicsTimer?.stop()
        startPhysicsLoop()
    }

    // ── Construcción de la escena ───────────────────────────────────────

    private fun setupWorld() {
        BackgroundView(spriteSource, config).placeIn(pane)

        slingshot = SlingshotView(spriteSource, config)
        slingshot.placeIn(pane)

        launchZone = LaunchZone(config, slingshot.anchor.x, slingshot.anchor.y)

        // Rótulo de puntos
        scoreLabel = Label("Puntos: 0").apply {
            style = "-fx-font-size: ${20 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
            layoutX = config.windowWidth / 2 - 50.0 * config.scale
            layoutY = 40.0 * config.scale
        }
        pane.children.add(scoreLabel)

        // Crear aro de basket y poste de apoyo
        createHoop()
        createPole()

        createSlotBirds()
    }

    /** Crea el aro de basket ubicado para que su borde derecho esté en X = 1875. */
    private fun createHoop() {
        hoopView = ImageView(spriteSource.load("aro_de_basket.png")).apply {
            isPreserveRatio = true
            fitWidth = hoopWidth
            fitHeight = hoopHeight
        }
        // Posición X: el borde derecho visible del tablero (x=482 de 554) queda en X = 1875
        val targetRightX = 1875.0 * (config.windowWidth / 1920.0)
        val hoopX = targetRightX - hoopWidth * (482.0 / 554.0)
        // Posición Y: aleatoria entre 20% y 70% de la altura de la pantalla
        hoopY = config.windowHeight * (0.2 + Random.nextDouble() * 0.5)
        hoopView.layoutX = hoopX
        hoopView.layoutY = hoopY - hoopHeight / 2
        pane.children.add(hoopView)
    }

    /** Mueve el aro a una nueva posición Y aleatoria. */
    private fun moveHoopToNewPosition() {
        hoopY = config.windowHeight * (0.2 + Random.nextDouble() * 0.5)
        hoopView.layoutY = hoopY - hoopHeight / 2
    }

    /**
     * Crea un poste vertical estático de apoyo para el aro.
     * Ubicado pegado al borde derecho de la pantalla (pixel 1920), con un ancho delgado.
     * Tiene hitbox física para que el pájaro rebote contra el poste.
     */
    private fun createPole() {
        val poleWidth = 22.0 * config.scale
        val poleHeight = config.windowHeight
        val poleX = config.windowWidth - poleWidth
        val poleCenterX = poleX + poleWidth / 2.0
        val poleY = 0.0

        // Visual: poste delgado de madera contra el borde derecho (1920 px)
        val poleRect = javafx.scene.shape.Rectangle(poleX, poleY, poleWidth, poleHeight).apply {
            fill = javafx.scene.paint.Color.web("#5c3a21")
            stroke = javafx.scene.paint.Color.web("#2b1d0c")
            strokeWidth = 1.0
        }
        pane.children.add(poleRect)
        hoopView.toFront()

        // Hitbox física: cuerpo estático para que el pájaro rebote contra el poste
        val body = org.dyn4j.dynamics.Body()
        val shape = org.dyn4j.geometry.Rectangle(poleWidth / config.ppm, poleHeight / config.ppm)
        body.addFixture(shape, 0.0, 0.4, 0.4) // densidad 0, fricción 0.4, rebote 0.4
        body.setMass(org.dyn4j.geometry.MassType.INFINITE)
        val pos = physics.screenToWorld(poleCenterX, poleHeight / 2.0)
        body.translate(pos.x, pos.y)
        physics.world.addBody(body)
        poleBody = body
    }

    private fun createSlotBirds() {
        slotTypes.forEach { type -> spawnSlotBird(type) }
    }

    /** Coordenada (esq. sup. izq.) del pájaro para el tipo [type]. */
    private fun slotCoord(index: Int): Pair<Double, Double> {
        val gap = 8.0 * config.scale
        return (20.0 * config.scale to 20.0 * config.scale + index * (config.birdSize + gap))
    }

    /** Crea un pájaro del [type] como visual puro en la pila (sin física). */
    private fun spawnSlotBird(type: String) {
        val index = slotTypes.indexOf(type)
        val bird = entityFactory.createBird(type)
        val (sx, sy) = slotCoord(index)
        bird.placeAt(pane, sx, sy)
        slotBirds += bird
        birdType[bird] = type
    }

    private fun massOf(bird: Bird): Float = when (bird) {
        is Red -> config.redMass
        is Chuck -> config.chuckMass
        is Bomb -> config.bombMass
        else -> 5.0f
    }

    // ── Gestión de pájaros ──────────────────────────────────────────────

    private fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        return Point2D(x, y).distance(cx, cy) < grabRadius
    }

    /** Devuelve el pájaro de la pila a su posición original. */
    private fun returnBirdToSlot(bird: Bird) {
        val type = birdType[bird] ?: return
        val (sx, sy) = slotCoord(slotTypes.indexOf(type))
        bird.placeAt(pane, sx, sy)
    }

    // ── Input ───────────────────────────────────────────────────────────

    private fun handlePressed(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // 1) Si hay un pájaro fijado en la resortera → BLOQUEADO.
        //    El pájaro no se puede sacar de la resortera. La única acción
        //    posible es estirar para lanzar (forceStartDrag).
        if (slingshotBird != null) {
            slingshot.forceStartDrag(mx, my)
            return
        }

        // 2) Agarrar un pájaro de la pila → empezar arrastre directo
        val bird = slotBirds.firstOrNull { isNearBird(it, mx, my) }
        if (bird != null) {
            dragBird = bird
            bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
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
            return
        }

        // Arrastre directo del pájaro desde la pila (solo si no hay pájaro bloqueado).
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
            val pull = slingshot.release() ?: return
            if (pull.magnitude() > 10.0) {
                launchBird(loaded, pull)
            } else {
                // Estiramiento insuficiente, devolver el pájaro a la resortera.
                loaded.placeAt(pane, slingshot.anchor.x - loaded.width / 2, slingshot.anchor.y - loaded.height / 2)
                physics.setTransform(loaded, slingshot.anchor.x, slingshot.anchor.y)
            }
            return
        }

        // Soltar pájaro arrastrado desde la pila (solo si no hay pájaro bloqueado).
        if (slingshotBird != null) return
        val bird = dragBird
        if (bird != null) {
            dragBird = null
            if (launchZone.contains(mx, my)) {
                // Soltado sobre la zona → cargar en la resortera
                loadBirdOntoSlingshot(bird)
            } else {
                // Soltado fuera → devolver a la pila
                returnBirdToSlot(bird)
            }
        }
    }

    private fun handleKeyPressed(e: KeyEvent) {
        when (e.code) {
            KeyCode.R -> resetLevel()
            else -> {}
        }
    }

    // ── Lanzamiento ─────────────────────────────────────────────────────

    /**
     * Carga el pájaro en la resortera: lo posiciona en el ancla, lo registra
     * como cuerpo kinemático y lo prepara para estirar. El slot se repone
     * al instante con un pájaro del mismo tipo (suministro infinito).
     */
    private fun loadBirdOntoSlingshot(bird: Bird) {
        val type = birdType[bird] ?: slotTypes[0]

        // Si ya había un pájaro fijado, se descarta
        val old = slingshotBird
        if (old != null) {
            physics.remove(old)
            old.view.removeFrom(pane)
            slingshotBird = null
        }

        // Mover el pájaro al ancla de la resortera y registrar física
        bird.placeAt(pane, slingshot.anchor.x - bird.width / 2, slingshot.anchor.y - bird.height / 2)
        physics.registerBird(bird, massOf(bird))
        physics.setKinematic(bird)
        physics.setTransform(bird, slingshot.anchor.x, slingshot.anchor.y)
        slingshotBird = bird

        // BLOQUEO: cancelar cualquier arrastre activo y asegurar que el pájaro
        // quede fijo en la posición de la resortera. No se puede sacar de aquí,
        // solo se puede lanzar (estirar y soltar).
        dragBird = null

        // Reponer el slot con un pájaro fresco del mismo tipo
        slotBirds.remove(bird)
        spawnSlotBird(type)
    }

    /**
     * Lanza el pájaro desde su posición actual (donde el usuario lo dejó
     * al estirar). El impulso es proporcional a la dirección del pull.
     */
    private fun launchBird(bird: Bird, pull: Point2D) {
        slingshotBird = null
        inFlight += bird
        lastScoredBird = null

        val mag = min(pull.magnitude(), config.maxStretch)
        val ratio = mag / config.maxStretch
        val speed = ratio * config.maxLaunchSpeed

        val vx = pull.x / mag * speed
        val vy = pull.y / mag * speed

        physics.setDynamic(bird)
        physics.launchBird(bird, vx, vy)
    }

    // ── Detección de canasta ───────────────────────────────────────────

    /**
     * Verifica si algún pájaro en vuelo pasó por el aro de basket.
     * Si encesta: +1 punto. Si el pájaro se detuvo sin encestar: -1 punto.
     */
    private fun checkHoopCollisions(dtStep: Double) {
        globalTime += dtStep

        // Solo verificar si ha pasado suficiente tiempo desde el último conteo
        if (globalTime - lastHoopCheckTime < 0.5) return

        // Centro del aro según el sprite aro_de_basket.png (rim center ~315/554 x, 282/554 y)
        val hoopCenterX = hoopView.layoutX + hoopWidth * (315.0 / 554.0)
        val hoopCenterY = hoopView.layoutY + hoopHeight * (282.0 / 554.0)

        val birdsToRemove = mutableListOf<Bird>()

        for (bird in inFlight) {
            // Verificar si el pájaro está en reposo o fuera de límites
            val outOfBounds = bird.hitbox.centerX < -100 ||
                              bird.hitbox.centerX > config.windowWidth + 200 ||
                              bird.hitbox.centerY > config.windowHeight + 100

            if (physics.isResting(bird) || outOfBounds) {
                // Si no encestó, restar punto (pero no menos de 0)
                if (lastScoredBird !== bird) {
                    score = max(0, score - 1)
                    scoreLabel.text = "Puntos: $score"
                }
                birdsToRemove += bird
                physics.remove(bird)
                continue
            }

            // Verificar si el pájaro pasó por el aro
            val birdX = bird.hitbox.centerX
            val birdY = bird.hitbox.centerY
            val dx = birdX - hoopCenterX
            val dy = birdY - hoopCenterY

            // El aro tiene un radio de detección para encestar
            val hoopRadius = hoopWidth * 0.28
            if (dx * dx + dy * dy < hoopRadius * hoopRadius) {
                // ¡Canasta! +1 punto
                if (lastScoredBird !== bird) {
                    score++
                    scoreLabel.text = "Puntos: $score"
                    lastScoredBird = bird
                    lastHoopCheckTime = globalTime
                    // Mover el aro a una nueva posición
                    moveHoopToNewPosition()
                }
            }
        }

        inFlight.removeAll(birdsToRemove)
    }

    // ── Game Loop (60 fps fijos) ────────────────────────────────────────

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
                    checkHoopCollisions(fixedDt)
                    accumulator -= fixedDt
                }
            }
        }.also { it.start() }
    }
}
