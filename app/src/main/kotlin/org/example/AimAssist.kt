package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

/**
 * Ayuda de tiro: dibuja la trayectoria predictora sobre una capa y la limpia
 * al soltar. Agrupa la capa + renderer para evitar duplicar showTrajectory
 * entre los minijuegos (DRY). El estilo del punto se parametriza por
 * constructor, porque difiere entre dianas y basket.
 */
class AimAssist(
    private val layer: Pane,
    private val renderer: TrajectoryRenderer,
    private val dotRadius: Double,
    private val fillOpacity: Double,
    private val strokeOpacity: Double,
    private val strokeWidth: Double
) {
    fun show(start: Point2D, pull: Point2D) {
        renderer.render(start, pull) { dot ->
            Circle(dot.x, dot.y, dotRadius).apply {
                fill = Color.web("#FFFFFF", fillOpacity)
                stroke = Color.web("#222222", strokeOpacity)
                strokeWidth = this@AimAssist.strokeWidth
            }
        }
    }

    fun clear() {
        layer.children.clear()
    }
}
