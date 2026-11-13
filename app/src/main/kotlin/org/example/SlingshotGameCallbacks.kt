package org.example

import javafx.geometry.Point2D

/**
 * Callbacks del juego con resortera que el manejador de entrada necesita
 * invocar (DIP: el manejador depende de la abstracción, no del juego).
 */
interface SlingshotGameCallbacks {
    fun onBirdGrabbed()
    fun onBirdLoaded()
    fun onBirdSettled(bird: Bird)
    fun onLevelReset()
    fun onLaunch()
    fun setLastLaunchedBird(bird: Bird?)
    fun onStretchUpdate(pull: Point2D, stretchPos: Point2D)
    fun clearAimLayer()
    fun onAbilityKey()
    fun resetLevel()
}
