package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane

/**
 * Minijuego 2: Tiro al aro de basket.
 */
class BasketGameController(
    spriteSource: SpriteSource,
    config: GameConfig,
    onExitToMenu: () -> Unit = {},
    physicsFactory: () -> PhysicsWorld,
    levelFactory: (Pane, PhysicsWorld) -> SlingshotLevelComponents,
    private val collaboratorFactory: (Pane, PhysicsWorld) -> BasketGameCollaborators
) :
    SlingshotGame(spriteSource, config, onExitToMenu = onExitToMenu, physicsFactory = physicsFactory, levelFactory = levelFactory) {

    private lateinit var collaborators: BasketGameCollaborators

    private val aimAssist: AimAssist get() = collaborators.aimAssist
    private val hoop: BasketHoop get() = collaborators.hoop
    private val rankScreen: RankDisplay get() = collaborators.rankScreen
    private val scoreBoard: BasketScoreBoard get() = collaborators.scoreBoard
    private val timer: BasketTimer get() = collaborators.timer

    private var rankShown = false

    private var trajectoryFramesRemaining = -1

    private lateinit var hoopScorer: HoopScorer

    /** Pájaros que ya encestaron en este vuelo (evita puntajes repetidos). */
    private val scoredBirds = mutableSetOf<Bird>()

    override fun setupLevel() {
        collaborators = collaboratorFactory(pane, physics)
        createPole()
    }

    override fun onLevelReset() {
        trajectoryFramesRemaining = -1
        rankShown = false
        scoredBirds.clear()
        scoreBoard.reset()
        timer.reset()
        hoopScorer.reset()
        scoreBoard.refreshRank()
    }

    override fun onLaunch() {
        scoredBirds.clear()
    }

    override fun onBirdLoaded() {
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

        physics.addStaticBox(BoxSpec(poleCenterX, poleHeight / 2.0, poleWidth, poleHeight))
    }

    override fun onPhysicsStep(dt: Double) {
        timer.tick(dt)

        if (trajectoryFramesRemaining > 0) {
            trajectoryFramesRemaining--
            if (trajectoryFramesRemaining == 0) {
                aimAssist.clear()
            }
        }

        if (!timer.isOver()) { hoopScorer.tick(dt); hoopScorer.check() }

        if (timer.isOver() && !rankShown) {
            rankShown = true
            rankScreen.show(pane, scoreBoard.score, RankEvaluator.rankForBasket(scoreBoard.score))
        }
    }


    private fun scoreBasket(bird: Bird) {
        if (timer.isOver()) return
        scoreBoard.addPoint()
        scoredBirds += bird
        hoop.moveToNewPosition()
    }

    override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {
        if (trajectoryFramesRemaining == -1) {
            trajectoryFramesRemaining = TRAJECTORY_FRAMES
        }
        if (trajectoryFramesRemaining > 0) {
            aimAssist.show(stretchPos, pull)
        }
    }

    override fun onBirdGrabbed() {
        trajectoryFramesRemaining = -1
    }

    override fun clearAimLayer() {
        aimAssist.clear()
        trajectoryFramesRemaining = -1
    }

    companion object {
        const val BASKET_TIME_LIMIT = 90
        const val TRAJECTORY_FRAMES = 60
    }
}
