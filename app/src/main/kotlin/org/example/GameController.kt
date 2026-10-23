package org.example

import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import kotlin.math.min

/**
 * Orquesta el juego: crea la escena, conecta la entrada del mouse/teclado
 * con la [SlingshotView], traduce el estiramiento en el lanzamiento del
 * pájaro activo dentro de [Box2DWorld] y coordina las pantallas finales.
 *
 * Responsabilidades:
 *  - Composición de la escena (fondo, resortera, entidades) y sus sub-sistemas:
 *    [ScoreBoard] (marcador y tiempo), [DianaManager] (dianas), [AngleIndicator],
 *    [TrajectoryCalculator] (trayectoria), [ExplosionPlayer] (explosión).
 *  - Entrada del mouse (arrastre, estiramiento, soltado) y teclado (G, R),
 *    a nivel de escena.
 *  - Lanzamiento de pájaros y reset del nivel.
 *  - Game loop (AnimationTimer → Box2DWorld.step) a paso fijo.
 *  - Pantalla final con el rango obtenido tras agotarse el tiempo.
 */
class GameController(
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
    // Último pájaro lanzado; la habilidad especial solo aplica a él.
    private var lastLaunched: Bird? = null

    private lateinit var slingshot: SlingshotView
    private lateinit var launchZone: LaunchZone
    private lateinit var pane: Pane
    private lateinit var angleIndicator: AngleIndicator
    private lateinit var trajectoryLayer: Pane
    private lateinit var trajectoryCalculator: TrajectoryCalculator
    private lateinit var scoreBoard: ScoreBoard
    private lateinit var dianaManager: DianaManager
    private lateinit var explosionPlayer: ExplosionPlayer

    private var physicsTimer: AnimationTimer? = null
    private val grabRadius = 45.0 * config.scale

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
        slingshotBird = null
        inFlight.clear()
        lastLaunched = null

        pane.children.clear()
        setupWorld()
        scoreBoard.reset()
        physicsTimer?.stop()
        startPhysicsLoop()
    }

    // ── Construcción de la escena ─────────────────────────────────────

    private fun setupWorld() {
        BackgroundView(spriteSource, config).placeIn(pane)

        slingshot = SlingshotView(spriteSource, config)
        slingshot.placeIn(pane)

        launchZone = LaunchZone(config, slingshot.anchor.x, slingshot.anchor.y)

        angleIndicator = AngleIndicator(config).also { it.placeIn(pane) }
        trajectoryLayer = Pane()
        pane.children.add(trajectoryLayer)
        trajectoryCalculator = TrajectoryCalculator(config)

        explosionPlayer = ExplosionPlayer(pane, spriteSource, config)

        scoreBoard = ScoreBoard(config).also { it.placeIn(pane) }
        dianaManager = DianaManager(spriteSource, config, pane).also {
            it.onTargetHit = { _ -> scoreBoard.addPoint() }
            it.setup()
        }
        createSlotBirds()
    }

    // ── Pila de pájaros ───────────────────────────────────────────────

    /** Coordenada (esq. sup. izq.) del pájaro de la pila para el tipo [type]. */
    private fun slotCoord(index: Int): Pair<Double, Double> {
        val gap = 8.0 * config.scale
        return (20.0 * config.scale to 20.0 * config.scale + index * (config.birdSize + gap))
    }

    /** Crea los tres pájaros de la pila (suministro infinito). */
    private fun createSlotBirds() {
        slotTypes.forEach { type -> spawnSlotBird(type) }
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

    /** Mueve un pájaro no lanzado de vuelta a su posición original. */
    private fun returnBirdToSlot(bird: Bird) {
        val type = birdType[bird] ?: return
        val (sx, sy) = slotCoord(slotTypes.indexOf(type))
        bird.placeAt(pane, sx, sy)
    }

    /** Masa (kg) según el tipo de ave, para que colisionen como real. */
    private fun massOf(bird: Bird): Float = when (bird) {
        is Red -> config.redMass
        is Chuck -> config.chuckMass
        is Bomb -> config.bombMass
        else -> 5.0f
    }

    /**
     * Fija el pájaro en la resortera: queda bloqueado hasta lanzarlo. El slot
     * se repone al instante con un pájaro nuevo del mismo tipo.
     */
    private fun loadBirdOntoSlingshot(bird: Bird) {
        val type = birdType[bird] ?: slotTypes[0]

        val old = slingshotBird
        if (old != null) {
            physics.remove(old)
            old.view.removeFrom(pane)
            slingshotBird = null
        }

        bird.placeAt(pane, slingshot.anchor.x - bird.width / 2, slingshot.anchor.y - bird.height / 2)
        physics.registerBird(bird, massOf(bird))
        physics.setKinematic(bird)
        physics.setTransform(bird, slingshot.anchor.x, slingshot.anchor.y)
        slingshotBird = bird

        // Reponer el slot con un pájaro fresco del mismo tipo
        slotBirds.remove(bird)
        spawnSlotBird(type)
    }

    /**
     * Lanza el pájaro. El impulso es proporcional a la dirección del pull:
     * cuanto más se estira la resortera, más rápido y lejos vuela.
     */
    private fun launchBird(bird: Bird, pull: Point2D) {
        slingshotBird = null
        inFlight += bird
        lastLaunched = bird

        val mag = min(pull.magnitude(), config.maxStretch)
        val ratio = mag / config.maxStretch
        val speed = ratio * config.maxLaunchSpeed

        val vx = pull.x / mag * speed
        val vy = pull.y / mag * speed

        physics.setDynamic(bird)
        physics.launchBird(bird, vx, vy)
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

            // Mostrar el ángulo de tiro y la trayectoria esperada
            angleIndicator.showAngle(slingshot.pull)
            showTrajectory(slingshot.birdStretchPos, slingshot.pull)
            return
        }

        // Arrastre directo del pájaro desde la pila.
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
            angleIndicator.reset()
            trajectoryLayer.children.clear()
            if (pull.magnitude() > 10.0) {
                launchBird(loaded, pull)
            } else {
                // Estiramiento insuficiente: devolver el pájaro a la resortera.
                loaded.placeAt(pane, slingshot.anchor.x - loaded.width / 2, slingshot.anchor.y - loaded.height / 2)
                physics.setTransform(loaded, slingshot.anchor.x, slingshot.anchor.y)
            }
            return
        }

        // Soltar pájaro arrastrado desde la pila.
        val bird = dragBird
        if (bird != null) {
            dragBird = null
            if (launchZone.contains(mx, my)) {
                // Soltado sobre la zona → cargar en la resortera.
                loadBirdOntoSlingshot(bird)
            } else {
                // Soltado fuera → volver a la pila.
                returnBirdToSlot(bird)
            }
        }
    }

    private fun handleKeyPressed(e: KeyEvent) {
        when (e.code) {
            KeyCode.G -> useSpecialAbility()
            KeyCode.R -> resetLevel()
            else -> {}
        }
    }

    private fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        return Point2D(x, y).distance(cx, cy) < grabRadius
    }

    // ── Habilidades especiales ───────────────────────────────────────

    /**
     * Habilidad del pájaro lanzado (G). Solo funciona si ese pájaro sigue
     * en vuelo; una vez usada, queda consumida.
     * Usa polimorfismo (Bird.useAbility) en lugar de checks de tipo (OCP).
     */
    private fun useSpecialAbility() {
        val bird = lastLaunched ?: return
        val didUse = bird.useAbility(physics, config)
        if (didUse && bird is Bomb) {
            val bx = bird.centerX
            val by = bird.centerY
            explosionPlayer.playAt(bx, by)
            // La onda expansiva derriba dianas dentro de su radio
            dianaManager.breakTargetsWithin(bx, by, config.bombExplosionRadius.toDouble())
        }
    }

    // ── Trayectoria ──────────────────────────────────────────────────

    /** Dibuja los puntos de la trayectoria esperada sobre la capa de la escena. */
    private fun showTrajectory(start: Point2D, pull: Point2D) {
        trajectoryLayer.children.clear()
        val dots = trajectoryCalculator.predictedDots(start, pull)
        for (dot in dots) {
            trajectoryLayer.children.add(
                javafx.scene.shape.Circle(dot.x, dot.y, 4.5 * config.scale).apply {
                    fill = javafx.scene.paint.Color.web("#FFFFFF", 0.85)
                    stroke = javafx.scene.paint.Color.web("#222222", 0.6)
                    strokeWidth = 1.0
                }
            )
        }
    }

    // ── Game Loop (60 fps fijos) ──────────────────────────────────────

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
                    scoreBoard.tick(fixedDt)
                    dianaManager.update(fixedDt, inFlight)
                    accumulator -= fixedDt
                }

                // Limpiar pájaros lanzados que ya se detuvieron. NO limpiamos
                // lastLaunched: la bomba debe poder explotar con G aunque ya
                // haya caído al piso (solo se reemplaza al lanzar otro pájaro).
                val resting = mutableListOf<Bird>()
                for (f in inFlight) {
                    if (physics.isResting(f)) {
                        physics.remove(f)
                        resting += f
                    }
                }
                inFlight.removeAll(resting)
            }
        }.also { it.start() }
    }

    // ── Pantalla final ────────────────────────────────────────────────

    /**
     * Al agotarse el tiempo se muestra la pantalla final: fondo, los tres
     * pájaros en horizontal, el rango obtenido y el puntaje.
     */
    private fun showRankScreen() {
        pane.children.clear()
        BackgroundView(spriteSource, config).placeIn(pane)

        val rankImg = spriteSource.load("${RankEvaluator.rankFor(scoreBoard.score)} rank.png")
        val rankSize = 200.0 * config.scale
        val rankView = javafx.scene.image.ImageView(rankImg).apply {
            isPreserveRatio = true
            fitHeight = rankSize
            layoutX = config.windowWidth / 2 - rankSize / 2
            layoutY = config.windowHeight * 0.12
        }
        pane.children.add(rankView)

        val title = Label("Rango obtenido").apply {
            style = "-fx-font-size: ${28 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
            layoutY = config.windowHeight * 0.10
            layoutX = config.windowWidth / 2 - 100 * config.scale
        }
        pane.children.add(title)

        val scoreText = Label("Puntaje: ${scoreBoard.score}").apply {
            style = "-fx-font-size: ${32 * config.scale}px; -fx-fill: gold; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
            layoutY = config.windowHeight * 0.12 + rankSize + 20 * config.scale
            layoutX = config.windowWidth / 2 - 120 * config.scale
        }
        pane.children.add(scoreText)

        // Los tres pájaros en fila horizontal, un poco a la izquierda de
        // pantalla y con el mismo tamaño que en el juego (solo sprites,
        // sin hitbox). Simulan la pantalla del minijuego tras terminar.
        val birdPng = mapOf("red" to "Red.png", "chuck" to "chuck .png", "bomb" to "bomb.png")
        val size = config.birdSize
        val gapBetween = 20.0 * config.scale
        val totalW = slotTypes.size * size + (slotTypes.size - 1) * gapBetween
        val startX = 40.0 * config.scale
        val yPos = config.windowHeight * 0.62
        slotTypes.forEachIndexed { i, type ->
            val iv = javafx.scene.image.ImageView(spriteSource.load(birdPng[type]!!)).apply {
                isPreserveRatio = true
                fitWidth = size
                fitHeight = size
                layoutX = startX + i * (size + gapBetween)
                layoutY = yPos
            }
            pane.children.add(iv)
        }
    }
}
