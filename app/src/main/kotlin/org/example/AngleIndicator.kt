package org.example

import javafx.geometry.Point2D
import javafx.scene.text.Text
import kotlin.math.atan2

/**
 * Muestra el ángulo de tiro mientras el jugador estira la resortera y lo
 * oculta tras soltar. Responsabilidad única: el texto de ayuda sobre pantalla.
 */
class AngleIndicator(private val config: GameConfig) {

    val text: Text = Text("Ángulo de tiro: —").apply {
        style = "-fx-font-size: ${18 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
    }

    fun placeIn(pane: javafx.scene.layout.Pane) {
        text.layoutX = config.windowWidth - 220.0 * config.scale
        text.layoutY = 135.0 * config.scale
        pane.children.add(text)
    }

    /** Refresca el ángulo según el vector de estiramiento actual. */
    fun showAngle(pull: Point2D) {
        if (pull.magnitude() <= 10.0) {
            text.text = "Ángulo de tiro: —"
            return
        }
        val angleDeg = Math.toDegrees(atan2(-pull.y, pull.x))
        text.text = "Ángulo de tiro: %.0f°".format(angleDeg)
    }

    /** Restablece el texto al estado inicial (después de soltar la resortera). */
    fun reset() {
        text.text = "Ángulo de tiro: —"
    }
}
