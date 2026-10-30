package org.example

import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import kotlin.random.Random

/**
 * Minijuego 2: Tiro al aro de basket.
 *
 * Misma mecánica de resortera que el juego principal (compartida en
 * [SlingshotGame]) pero sin línea predictora. El aro está pegado al
 * costado derecho y cambia de altura en cada canasta: encestar suma
 * un punto; fallar resta (sin quedar en negativo).
 */
class BasketGameController(spriteSource: SpriteSource, config: GameConfig, onExitToMenu: () -> Unit = {}) :
    SlingshotGame(spriteSource, config, onExitToMenu = onExitToMenu) {

    // ── Aro de basket ─────────────────────────────────────────────────
    private lateinit var hoopView: ImageView
    private var hoopY: Double = 0.0
    private val hoopWidth: Double = 140.0 * config.scale
    private val hoopHeight: Double = 140.0 * config.scale

    // Línea de trayectoria predictora (muy transparente)
    private lateinit var trajectoryLayer: Pane
    private lateinit var trajectoryCalculator: TrajectoryCalculator

    // Estado de la trayectoria predictora: -1 = lista para iniciar al estirar,
    // 0..N = frames restantes, -2 = expirada (no se vuelve a mostrar hasta
    // soltar y volver a estirar).
    private var trajectoryFramesRemaining = -1

    // con el aro y bloquea el paso por detrás (evita tocar la red "gratis").
    private val backboardWidth: Double = 8.0 * config.scale
    private val backboardHeight: Double = 0.3 * 140.0 * config.scale

    // Marcador visual (borde rojo) de la hitbox invisible del backboard.
    private lateinit var backboardVisual: javafx.scene.shape.Rectangle

    // ── Puntuación ────────────────────────────────────────────────────
    private lateinit var scoreLabel: Label
    private lateinit var rankView: ImageView
    private lateinit var timerLabel: Label
    private lateinit var rankScreen: RankScreen
    private var score = 0
    private var timeLeft = BASKET_TIME_LIMIT.toDouble()
    private var timeOver = false
    private var timerStarted = false
    private var rankShown = false

    // Tiempo del mundo y cooldown para contar canastas
    private var globalTime = 0.0
    private var lastHoopCheckTime = 0.0

    // Pájaro que ya sumó canasta (evita conteos repetidos del mismo tiro)
    private var lastScoredBird: Bird? = null

    override fun setupLevel() {
        trajectoryLayer = Pane()
        pane.children.add(trajectoryLayer)
        trajectoryCalculator = TrajectoryCalculator(config)

        scoreLabel = Label("Puntos: 0").apply {
            style = "-fx-font-size: ${20 * config.scale}px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
            textFill = javafx.scene.paint.Color.WHITE
            layoutX = config.windowWidth / 2 - 50.0 * config.scale
            layoutY = 40.0 * config.scale
        }
        pane.children.add(scoreLabel)

        rankView = ImageView(spriteSource.load("${RankEvaluator.rankForBasket(score)} rank.png")).apply {
            isPreserveRatio = true
            fitWidth = 40.0 * config.scale
            fitHeight = 40.0 * config.scale
            layoutX = config.windowWidth / 2 - 110.0 * config.scale
            layoutY = 34.0 * config.scale
        }
        pane.children.add(rankView)

        rankScreen = RankScreen(spriteSource, config)

        timerLabel = Label("Tiempo: 01:00").apply {
            style = "-fx-font-size: ${16 * config.scale}px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
            textFill = javafx.scene.paint.Color.WHITE
            layoutX = config.windowWidth / 2 - 110.0 * config.scale
            layoutY = 78.0 * config.scale
        }
        pane.children.add(timerLabel)

        createHoop()
        createPole()
        createBackboard()
    }

    override fun onLevelReset() {
        score = 0
        timeLeft = BASKET_TIME_LIMIT.toDouble()
        timeOver = false
        timerStarted = false
        globalTime = 0.0
        lastHoopCheckTime = 0.0
        lastScoredBird = null
        trajectoryFramesRemaining = -1
        rankShown = false
        timerLabel.text = "Tiempo: 01:00"
        refreshRank()
    }

    override fun onBirdLoadedOntoSlingshot() {
        if (!timerStarted) {
            timeLeft = BASKET_TIME_LIMIT.toDouble()
            timeOver = false
            timerStarted = true
        }
    }

    override fun onLaunch() {
        lastScoredBird = null
    }

    // ── Escena: aro y poste ────────────────────────────────────────────

    /** Crea el aro de basket con su borde derecho en X = 1875 (escala). */
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

    /** Mueve el aro a una nueva posición Y aleatoria (tras una canasta). */
    private fun moveHoopToNewPosition() {
        hoopY = config.windowHeight * (0.2 + Random.nextDouble() * 0.5)
        hoopView.layoutY = hoopY - hoopHeight / 2
        moveBackboard()
    }

    /**
     * Poste vertical de apoyo para el aro, pegado al borde derecho de la
     * pantalla (pixel 1920). Visual de madera + caja física estática para
     * que el pájaro rebote contra él.
     */
    private fun createPole() {
        val poleWidth = 22.0 * config.scale
        val poleHeight = config.windowHeight
        val poleX = config.windowWidth - poleWidth
        val poleCenterX = poleX + poleWidth / 2.0

        val poleRect = javafx.scene.shape.Rectangle(poleX, 0.0, poleWidth, poleHeight).apply {
            fill = javafx.scene.paint.Color.web("#5c3a21")
            stroke = javafx.scene.paint.Color.web("#2b1d0c")
            strokeWidth = 1.0
        }
        pane.children.add(poleRect)
        hoopView.toFront()

        // Hitbox física: cuerpo estático para que el pájaro rebote contra el poste
        physics.addStaticBox(poleCenterX, poleHeight / 2.0, poleWidth, poleHeight)
    }

    /**
     * Barrita negra (backboard) del aro: pequeña, ubicada en el hueco del
     * aro, y se mueve verticalmente junto con el aro.
     */
    private fun createBackboard() {
        val wallX = backboardX()
        val wallY = hoopCenterY()

        // Marcador visual (borde rojo, sin relleno) para ver la hitbox.
        val rect = javafx.scene.shape.Rectangle(
            wallX - backboardWidth / 2.0,
            wallY - backboardHeight / 2.0,
            backboardWidth,
            backboardHeight
        ).apply {
            fill = javafx.scene.paint.Color.TRANSPARENT
            stroke = javafx.scene.paint.Color.RED
            strokeWidth = 2.0
        }
        pane.children.add(rect)
        hoopView.toFront()
        backboardVisual = rect

        physics.addNamedStaticBox(BACKBOARD_NAME, wallX, wallY, backboardWidth, backboardHeight)
    }

    /** X fija de la barrita (relativa al aro, que no cambia horizontalmente). */
    private fun backboardX(): Double {
        val targetRightX = 1875.0 * (config.windowWidth / 1920.0)
        val hoopX = targetRightX - hoopWidth * (482.0 / 554.0)
        return hoopX + hoopWidth * 0.30
    }

    /** Y del centro del aro (rim center), un poco más abajo para el backboard. */
    private fun hoopCenterY(): Double = hoopY + 15.0 * config.scale

    /** Desplaza la barrita y su hitbox a la nueva posición del aro. */
    private fun moveBackboard() {
        val wallX = backboardX()
        val wallY = hoopCenterY()
        backboardVisual.x = wallX - backboardWidth / 2.0
        backboardVisual.y = wallY - backboardHeight / 2.0
        physics.moveNamedStaticBox(BACKBOARD_NAME, wallX, wallY)
    }

    // ── Detección de canasta ──────────────────────────────────────────

    override fun onPhysicsStep(dt: Double) {
        globalTime += dt

        // Temporizador de 60 s (arranca al cargar un pájaro en la resortera)
        if (timerStarted && !timeOver) {
            timeLeft -= dt
            if (timeLeft <= 0.0) {
                timeLeft = 0.0
                timeOver = true
            }
            val remaining = kotlin.math.max(0, timeLeft.toInt())
            timerLabel.text = "Tiempo: %02d:%02d".format(remaining / 60, remaining % 60)
        }

        if (trajectoryFramesRemaining > 0) {
            trajectoryFramesRemaining--
            if (trajectoryFramesRemaining == 0) {
                trajectoryLayer.children.clear()
            }
        }
        if (!timeOver) checkHoops()

        if (timeOver && !rankShown) {
            rankShown = true
            rankScreen.show(pane, score, RankEvaluator.rankForBasket(score))
        }
    }

    /**
     * Verifica si algún pájaro en vuelo pasó por el aro (fuera de la
     * pantalla se considera tiro perdido y lo retira con punto en contra).
     */
    private fun checkHoops() {
        // Solo verificar si ha pasado suficiente tiempo desde el último conteo
        if (globalTime - lastHoopCheckTime < CHECK_COOLDOWN) return

        // Centro del aro según el sprite aro_de_basket.png (rim center ~315/554 x, 282/554 y)
        val hoopCenterX = hoopView.layoutX + hoopWidth * (315.0 / 554.0)
        val hoopCenterY = hoopView.layoutY + hoopHeight * (282.0 / 554.0)
        val hoopRadius = hoopWidth * HOOP_SCORE_RATIO

        // Retirar pájaros que salieron volando del nivel (tiro fallido).
        val fallen = inFlight.filter { isOutsideScreen(it) }
        for (bird in fallen) {
            physics.remove(bird)
            inFlight.remove(bird)
            inFlightTime.remove(bird)
            applyMiss(bird)
            settledBirds[bird] = 0.0
        }

        for (bird in inFlight) {
            val dx = bird.hitbox.centerX - hoopCenterX
            val dy = bird.hitbox.centerY - hoopCenterY
            val entered = dx * dx + dy * dy < hoopRadius * hoopRadius
            if (entered && lastScoredBird !== bird) {
                scoreBasket(bird)
            }
        }
    }

    /** True si el tamaño del [bird] quedó fuera de la pantalla. */
    private fun isOutsideScreen(bird: Bird): Boolean {
        val h = bird.hitbox
        return h.centerX < -100 ||
            h.centerX > config.windowWidth + 200 ||
            h.centerY > config.windowHeight + 100
    }

    /** ¡Canasta! +1 punto (zona verde), cooldown y el aro cambia de altura. */
    private fun scoreBasket(bird: Bird) {
        if (timeOver) return
        score += 1
        scoreLabel.text = "Puntos: $score"
        lastScoredBird = bird
        lastHoopCheckTime = globalTime
        moveHoopToNewPosition()
        refreshRank()
    }

    /** Tiro fallido (pájaro asentado o caído fuera de pantalla): sin puntos en contra. */
    private fun applyMiss(bird: Bird) {
        if (lastScoredBird === bird) return
        // No se restan puntos: el puntaje solo sube con las canastas.
    }

    /** Actualiza la imagen del rango según el puntaje actual. */
    private fun refreshRank() {
        rankView.image = spriteSource.load("${RankEvaluator.rankForBasket(score)} rank.png")
    }

    override fun onBirdSettled(bird: Bird) {
        applyMiss(bird)
    }

    override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {
        if (trajectoryFramesRemaining == -1) {
            trajectoryFramesRemaining = TRAJECTORY_FRAMES
        }
        if (trajectoryFramesRemaining > 0) {
            showTrajectory(stretchPos, pull)
        }
    }

    override fun onBirdGrabbed() {
        trajectoryFramesRemaining = -1
    }

    override fun clearAimLayer() {
        trajectoryLayer.children.clear()
        trajectoryFramesRemaining = -1
    }

    /** Dibuja los puntos de la trayectoria esperada (muy transparentes). */
    private fun showTrajectory(start: Point2D, pull: Point2D) {
        trajectoryLayer.children.clear()
        val dots = trajectoryCalculator.predictedDots(start, pull)
        for (dot in dots) {
            trajectoryLayer.children.add(
                javafx.scene.shape.Circle(dot.x, dot.y, 3.5 * config.scale).apply {
                    fill = javafx.scene.paint.Color.web("#FFFFFF", 0.22)
                    stroke = javafx.scene.paint.Color.web("#222222", 0.12)
                    strokeWidth = 0.5
                }
            )
        }
    }

    protected companion object {
        /** Nombre de la barrita negra (backboard) en el mundo físico. */
        const val BACKBOARD_NAME = "backboard"

        /** Duración del minijuego de basket (segundos). */
        const val BASKET_TIME_LIMIT = 90

        /** Frames que se muestra la trayectoria al estirar (1 s). */
        const val TRAJECTORY_FRAMES = 60

        /** Segundos sin contar canastas tras sumar una. */
        const val CHECK_COOLDOWN = 0.5

        /** Radio de la zona verde (verificación) como fracción del ancho del aro. */
        const val HOOP_SCORE_RATIO = 0.18
    }
}
