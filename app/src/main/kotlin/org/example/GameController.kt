package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane

class GameController(
    parts: SlingshotGameParts,
    onExitToMenu: () -> Unit = {},
    private val collaboratorFactory: (Pane, PhysicsWorld) -> GameCollaborators
) :
    SlingshotGame(parts, onExitToMenu = onExitToMenu), AbilityEffects {

    private lateinit var collaborators: GameCollaborators

    private var timerStarted = false

    override fun setupLevel() {
        collaborators = collaboratorFactory(pane, physics)
        collaborators.angleIndicator.placeIn(pane)
        collaborators.scoreBoard.placeIn(pane)
        collaborators.scoreBoard.reset()
        collaborators.scoreBoard.onTimeOver = { collaborators.rankScreen.showFor(pane, collaborators.scoreBoard.score()) }
        collaborators.dianaManager.onTargetHit = { points -> collaborators.scoreBoard.addPoints(points) }
        collaborators.dianaManager.setup()
    }

    override fun onPhysicsStep(dt: Double) {
        if (timerStarted) collaborators.scoreBoard.tick(dt)
        collaborators.dianaManager.update(dt, inFlight)
    }

    override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {
        collaborators.angleIndicator.showAngle(pull)
        collaborators.aimAssist.show(stretchPos, pull)
    }

    override fun clearAimLayer() {
        collaborators.angleIndicator.reset()
        collaborators.aimAssist.clear()
    }

    override fun onBirdLoaded() {
        if (!timerStarted) {
            collaborators.scoreBoard.reset()
            timerStarted = true
        }
    }

    override fun onLevelReset() {
        timerStarted = false
    }

    override fun onAbilityKey() {
        lastLaunched?.useAbility(physics, config, fx = this)
    }

    override fun explosionAt(cx: Double, cy: Double, radiusPx: Double) {
        collaborators.explosionPlayer.playAt(cx, cy)
        collaborators.dianaManager.breakTargetsWithin(cx, cy, radiusPx)
    }
}