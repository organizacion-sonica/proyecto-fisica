package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane

class BasketGameController(
    parts: SlingshotGameParts,
    onExitToMenu: () -> Unit = {},
    private val collaboratorFactory: (Pane, PhysicsWorld) -> BasketGameCollaborators
) :
    SlingshotGame(parts, onExitToMenu = onExitToMenu) {

    private lateinit var collaborators: BasketGameCollaborators

    private var rankShown = false

    private var trajectoryFramesRemaining = -1

    private lateinit var hoopScorer: HoopScorer

    /** Pájaros que ya encestaron en este vuelo (evita puntajes repetidos). */
    private val scoredBirds = mutableSetOf<Bird>()

    override fun setupLevel() {
        collaborators = collaboratorFactory(pane, physics)
        BasketDefaults.createPole(pane, physics, collaborators.hoop, config)
    }

    override fun onLevelReset() {
        trajectoryFramesRemaining = -1
        rankShown = false
        scoredBirds.clear()
        collaborators.scoreBoard.reset()
        collaborators.timer.reset()
        hoopScorer.reset()
        collaborators.scoreBoard.refreshRank()
    }

    override fun onLaunch() {
        scoredBirds.clear()
    }

    override fun onBirdLoaded() {
        collaborators.timer.start()
    }

    override fun onPhysicsStep(dt: Double) {
        collaborators.timer.tick(dt)

        if (trajectoryFramesRemaining > 0) {
            trajectoryFramesRemaining--
            if (trajectoryFramesRemaining == 0) {
                collaborators.aimAssist.clear()
            }
        }

        if (!collaborators.timer.isOver()) { hoopScorer.tick(dt); hoopScorer.check() }

        if (collaborators.timer.isOver() && !rankShown) {
            rankShown = true
            collaborators.rankScreen.showFor(pane, collaborators.scoreBoard.score)
        }
    }

    private fun scoreBasket(bird: Bird) {
        if (collaborators.timer.isOver()) return
        collaborators.scoreBoard.addPoint()
        scoredBirds += bird
        collaborators.hoop.moveToNewPosition()
    }

    override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {
        if (trajectoryFramesRemaining == -1) {
            trajectoryFramesRemaining = TRAJECTORY_FRAMES
        }
        if (trajectoryFramesRemaining > 0) {
            collaborators.aimAssist.show(stretchPos, pull)
        }
    }

    override fun onBirdGrabbed() {
        trajectoryFramesRemaining = -1
    }

    override fun clearAimLayer() {
        collaborators.aimAssist.clear()
        trajectoryFramesRemaining = -1
    }

    companion object {
        const val BASKET_TIME_LIMIT = 90
        const val TRAJECTORY_FRAMES = 60
    }
}
