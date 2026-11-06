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
    private val trajectoryRenderer: TrajectoryRenderer get() = collaborators.trajectoryRenderer
    private val scoreLabel: Label get() = collaborators.scoreLabel
    private val rankView: ImageView get() = collaborators.rankView
    private val timerLabel: Label get() = collaborators.timerLabel
    private val hoop: BasketHoop get() = collaborators.hoop
    private val rankScreen: RankDisplay get() = collaborators.rankScreen

    private lateinit var scoreBoard: BasketScoreBoard
    private lateinit var timer: BasketTimer
    private var rankShown = false

    private var globalTime = 0.0
    private var lastHoopCheckTime = 0.0
    private var lastScoredBird: Bird? = null

    private var trajectoryFramesRemaining = -1

    override fun setupLevel() {
        collaborators = collaboratorFactory(pane, physics)
        BackgroundView(spriteSource, config).placeIn(pane)
        scoreBoard = BasketScoreBoard(spriteSource, config, scoreLabel, rankView)
        timer = BasketTimer(config, timerLabel, BASKET_TIME_LIMIT)
        createPole()
    }

    override fun onLevelReset() {
        globalTime = 0.0
        lastHoopCheckTime = 0.0
        lastScoredBird = null
        trajectoryFramesRemaining = -1
        rankShown = false
        scoreBoard.reset()
        timer.reset()
        refreshRank()
    }

    override fun onLaunch() {
        lastScoredBird = null
    }

    override fun onBirdLoadedOntoSlingshot() {
        timer.start()
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

        val timeJustOver = timer.tick(dt)

        if (trajectoryFramesRemaining > 0) {
            trajectoryFramesRemaining--
            if (trajectoryFramesRemaining == 0) {
                trajectoryLayer.children.clear()
            }
        }

        if (!timer.isOver()) checkHoops()

        if (timer.isOver() && !rankShown) {
            rankShown = true
            rankScreen.show(pane, scoreBoard.score, RankEvaluator.rankForBasket(scoreBoard.score))
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
            markBirdSettled(bird)
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
        if (timer.isOver()) return
        scoreBoard.addPoint()
        lastScoredBird = bird
        lastHoopCheckTime = globalTime
        hoop.moveToNewPosition()
    }

    private fun refreshRank() {
        scoreBoard.refreshRank()
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
        trajectoryRenderer.render(start, pull) { dot ->
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
