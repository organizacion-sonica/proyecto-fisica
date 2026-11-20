package org.example

import javafx.geometry.Point2D

/**
 * Callbacks del juego con resortera que el manejador de entrada necesita
 * invocar (DIP: el manejador depende de la abstracción, no del juego).
 */
interface AimCallbacks {
    fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {}
    fun clearAimLayer() {}
}

interface BirdCallbacks {
    fun onBirdGrabbed() {}
    fun onBirdLoaded() {}
    fun onBirdLaunched(bird: Bird)
    fun onLaunch() {}
}

interface GameKeyCallbacks {
    fun onAbilityKey() {}
    fun resetLevel() {}
    fun onExitRequested()
}

interface SlingshotGameCallbacks : AimCallbacks, BirdCallbacks, GameKeyCallbacks