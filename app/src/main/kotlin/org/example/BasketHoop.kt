package org.example

import javafx.scene.image.ImageView

/**
 * Abstracción del aro de basket (DIP): el controlador depende de esta
 * interfaz y no de la implementación concreta [Hoop].
 */
interface BasketHoop {
    val view: ImageView
    val centerX: Double
    val centerY: Double
    val scoreRadius: Double
    val scoreRadiusSq: Double
    fun moveToNewPosition()
}
