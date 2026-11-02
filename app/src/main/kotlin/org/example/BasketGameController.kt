package org.example

import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import kotlin.random.Random

/**
 * Minijuego 2: Tiro al aro de basket.
 */
class BasketGameController(
    spriteSource: SpriteSource,
    config: GameConfig,
    onExitToMenu: () -> Unit = { Unit },
    private val collaboratorFactory: (Pane, PhysicsWorld) -> BasketGameCollaborators
) :
    SlingshotGame(spriteSource, config, onExitToMenu = onExitToMenu) {

    private lateinit var collaborators: BasketGameCollaborators

    private val trajectoryLayer: Pane get() = collaborators.trajectoryLayer
    private val trajectoryCalculator: TrajectoryPredictor get() = collaborators.trajectoryCalculator
    private val scoreLabel: Label get() = collaborators.scoreLabel
    private val rankView: ImageView get() = collaborators.rankView
    private val timerLabel: Label get() = collaborators.timerLabel
    private val hoop: BasketHoop get() = collaborators.hoop
    private val rankScreen: RankDisplay get() = collaborators.rankScreen

    private var score = 0
    private var timeLeft = BASKET_TIME_LIMIT.toDouble()
    private var timeOver = false
    private var timerStarted = false
    private var rankShown = false

    private var globalTime = 0.0
    private var lastHoopCheckTime = 0.0
    private var lastScoredBird: Bird? = null

    private var trajectoryFramesRemaining = -1

    override fun setupLevel() {
        collaborators = collaboratorFactory(pane, physics)
        pane.children.add(trajectoryLayer)
        pane.children.add(scoreLabel)
        pane.children.add(rankView)
        pane.children.add(timerLabel)
        createPole()
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
        scoreLabel.text = "Puntos: 0"
        timerLabel.text = "Tiempo: 01:00"
        refreshRank()
    }

    override fun onLaunch() {
        lastScoredBird = null
    }

    override fun onBirdLoadedOntoSlingshot() {
        if (!timerStarted) {
            timeLeft = BASKET_TIME_LIMIT.toDouble()
            timeOver = false
            timerStarted = true
        }
    }

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
        hoop.view.toFront()

        physics.addStaticBox(poleCenterX, poleHeight / 2.0, poleWidth, poleHeight)
    }

    override fun onPhysicsStep(dt: Double) {
        globalTime += dt

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

    private fun checkHoops() {
        if (globalTime - lastHoopCheckTime < CHECK_COOLDOWN) return

        val hoopCenterX = hoop.centerX
        val hoopCenterY = hoop.centerY
        val hoopRadiusSq = hoop.scoreRadiusSq

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
            val entered = dx * dx + dy * dy < hoopRadiusSq
            if (entered && lastScoredBird !== bird) {
                scoreBasket(bird)
            }
        }
    }

    private fun isOutsideScreen(bird: Bird): Boolean {
        val h = bird.hitbox
        return h.centerX < -100 ||
            h.centerX > config.windowWidth + 200 ||
            h.centerY > config.windowHeight + 100
    }

    private fun scoreBasket(bird: Bird) {
        if (timeOver) return
        score += 1
        scoreLabel.text = "Puntos: $score"
        lastScoredBird = bird
        lastHoopCheckTime = globalTime
        hoop.moveToNewPosition()
        refreshRank()
    }

    private fun applyMiss(bird: Bird) {
        if (lastScoredBird === bird) return
    }

    override fun onBirdSettled(bird: Bird) {
        applyMiss(bird)
    }

    private fun refreshRank() {
        rankView.image = spriteSource.load("${RankEvaluator.rankForBasket(score)} rank.png")
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

    private fun showTrajectory(start: Point2D, pull: Point2D) {
        TrajectoryRenderer(trajectoryLayer, trajectoryCalculator).render(start, pull) { dot ->
            javafx.scene.shape.Circle(dot.x, dot.y, 3.5 * config.scale).apply {
                fill = javafx.scene.paint.Color.web("#FFFFFF", 0.22)
                stroke = javafx.scene.paint.Color.web("#222222", 0.12)
                strokeWidth = 0.5
            }
        }
    }

    protected companion object {
        const val BASKET_TIME_LIMIT = 90
        const val TRAJECTORY_FRAMES = 60
        const val CHECK_COOLDOWN = 0.5
    }
}
