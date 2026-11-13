package org.example

import javafx.geometry.Point2D
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane

/**
 * Manejo de la entrada (mouse/teclado) de un minijuego con resortera (SRP).
 * Depende de la abstracción [SlingshotGameCallbacks] (DIP), no del juego.
 */
class SlingshotInput(
    private val config: GameConfig,
    private val pane: Pane,
    private val slingshot: SlingshotView,
    private val launchZone: LaunchZone,
    private val physics: PhysicsWorld,
    private val pile: BirdPile,
    private val flightTracker: FlightTracker,
    private val callbacks: SlingshotGameCallbacks,
    private val onExitToMenu: () -> Unit
) {
    private val grabRadius = 45.0 * config.scale

    var dragBird: Bird? = null
        private set
    var slingshotBird: Bird? = null
        private set

    fun availableBirds(): List<Bird> = pile.available()

    fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        return Point2D(x, y).distance(cx, cy) < grabRadius
    }

    fun handlePressed(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        if (slingshotBird != null) {
            slingshot.forceStartDrag(mx, my)
            return
        }

        val bird = pile.available().firstOrNull { isNearBird(it, mx, my) }
        if (bird != null) {
            dragBird = bird
            bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
            callbacks.onBirdGrabbed()
        }
    }

    fun handleDragged(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        if (slingshot.isDragging) {
            val loaded = slingshotBird ?: return
            slingshot.drag(mx, my)
            val pos = slingshot.birdStretchPos
            loaded.placeAt(pane, pos.x - loaded.width / 2, pos.y - loaded.height / 2)
            physics.setTransform(loaded, pos.x, pos.y)
            callbacks.onStretchUpdate(slingshot.pull, pos)
            return
        }

        if (slingshotBird != null) return
        val bird = dragBird ?: return
        bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
    }

    fun handleReleased(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        if (slingshot.isDragging) {
            val loaded = slingshotBird ?: return
            callbacks.clearAimLayer()
            val pull = slingshot.release() ?: return
            if (pull.magnitude() > 10.0) {
                launch(loaded, pull)
            } else {
                loaded.placeAt(pane, slingshot.anchor.x - loaded.width / 2, slingshot.anchor.y - loaded.height / 2)
                physics.setTransform(loaded, slingshot.anchor.x, slingshot.anchor.y)
            }
            return
        }

        if (slingshotBird != null) return
        val bird = dragBird ?: return
        dragBird = null
        if (launchZone.contains(mx, my)) {
            loadOntoSlingshot(bird)
        } else {
            pile.putBack(bird)
        }
    }

    fun handleKeyPressed(e: KeyEvent) {
        when (e.code) {
            KeyCode.ESCAPE -> onExitToMenu()
            KeyCode.G -> callbacks.onAbilityKey()
            KeyCode.R -> callbacks.resetLevel()
            else -> Unit
        }
    }

    fun loadOntoSlingshot(bird: Bird) {
        pile.withdraw(bird)

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

        dragBird = null
        callbacks.onBirdGrabbed()
        callbacks.onBirdLoaded()
    }

    private fun launch(bird: Bird, pull: Point2D) {
        slingshotBird = null
        flightTracker.recordLaunch(bird)
        callbacks.setLastLaunchedBird(bird)

        val (vx, vy) = launchVelocity(pull)
        physics.setDynamic(bird)
        physics.launchBird(bird, vx, vy)
        callbacks.onLaunch()
    }

    private fun launchVelocity(pull: Point2D): Pair<Double, Double> {
        val mag = kotlin.math.min(pull.magnitude(), config.maxStretch)
        val speed = mag / config.maxStretch * config.maxLaunchSpeed
        return pull.x / mag * speed to pull.y / mag * speed
    }

    fun putBack(bird: Bird) = pile.putBack(bird)

    fun clear() {
        dragBird = null
        slingshotBird = null
    }
}
