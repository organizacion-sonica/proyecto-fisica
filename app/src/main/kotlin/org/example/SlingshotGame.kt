package org.example

import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane

abstract class SlingshotGame(
    val parts: SlingshotGameParts,
    private val onExitToMenu: () -> Unit = {}
) : SlingshotGameCallbacks {
    protected val spriteSource get() = parts.spriteSource
    protected val config get() = parts.config

    protected var physics = parts.physicsFactory()
    private var gameLoop: GameLoop? = null

    private lateinit var levelComponents: SlingshotLevelComponents

    private lateinit var flightTracker: FlightTracker

    protected val inFlight: MutableList<Bird> get() = flightTracker.inFlight
    protected var lastLaunched: Bird? = null

    protected lateinit var pane: Pane

    private lateinit var input: SlingshotInput

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

    override fun resetLevel() {
        stopPhysicsLoop()
        physics.dispose()
        physics = parts.physicsFactory()
        lastLaunched = null
        flightTracker.clear()
        input.clear()

        pane.children.clear()
        setupWorld()
        onLevelReset()
        startPhysicsLoop()
    }

    private fun setupWorld() {
        levelComponents = parts.levelFactory(pane, physics)

        flightTracker = FlightTracker(physics, pane, config) { onBirdSettled(it) }

        input = SlingshotInput(
            pane,
            levelComponents.slingshot,
            levelComponents.launchZone,
            physics,
            levelComponents.pile,
            this
        )

        setupLevel()
    }

    protected abstract fun setupLevel()

    protected open fun onLevelReset() { Unit }

    protected abstract fun onPhysicsStep(dt: Double)

    override fun onBirdLaunched(bird: Bird) {
        lastLaunched = bird
        flightTracker.recordLaunch(bird)
    }

    override fun onExitRequested() {
        stopPhysicsLoop()
        onExitToMenu()
    }

    protected open fun onBirdSettled(bird: Bird) { }

    protected fun markBirdSettled(bird: Bird) = flightTracker.markSettled(bird)

    private fun startPhysicsLoop() {
        gameLoop = GameLoop(
            physics = physics,
            onStep = { dt -> onPhysicsStep(dt) },
            onRemoveSettled = { flightTracker.removeSettled() },
            onTickSettled = { dt -> flightTracker.tick(dt) }
        )
        gameLoop?.start()
    }

    private fun stopPhysicsLoop() {
        gameLoop?.stop()
        gameLoop = null
    }
}
