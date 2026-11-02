package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane
import javafx.scene.Node

/**
 * Dibuja la trayectoria predictora sobre una capa. Responsabilidad única:
 * evitar duplicar el bucle de dibujo entre minijuegos (DRY).
 */
class TrajectoryRenderer(
    private val layer: Pane,
    private val calculator: TrajectoryPredictor
) {
    fun render(start: Point2D, pull: Point2D, dotFactory: (Point2D) -> Node) {
        layer.children.clear()
        for (dot in calculator.predictedDots(start, pull)) {
            layer.children.add(dotFactory(dot))
        }
    }
}
