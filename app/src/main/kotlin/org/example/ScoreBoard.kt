package org.example

import javafx.scene.control.Label
import javafx.scene.layout.Pane

/**
 * Encapsula la puntuación y el contador de tiempo del nivel.
 * Responsabilidad única: llevar el marcador visible y avisar cuando se
 * agota el tiempo (la construcción de la pantalla final es otra responsabilidad).
 */
class ScoreBoard(private val config: GameConfig) {

    private val scale = config.scale

    private val scoreLabel = Label("Puntos: 0").apply {
        style = "-fx-font-size: ${20 * scale}px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
        textFill = javafx.scene.paint.Color.WHITE
    }
    private val timerLabel = Label("Tiempo: 01:45").apply {
        style = "-fx-font-size: ${20 * scale}px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
        textFill = javafx.scene.paint.Color.WHITE
    }

    private var score: Int = 0

    /** Puntaje actual (solo lectura desde fuera). */
    fun score(): Int = score

    private var timeLeft = config.timeLimitSeconds.toDouble() // segundos restantes
    private var timeOver = false

    /** Se invoca una sola vez cuando el tiempo se agota. */
    var onTimeOver: (() -> Unit)? = null

    fun placeIn(pane: Pane) {
        val rightX = config.windowWidth - 160.0 * scale
        scoreLabel.layoutX = rightX
        scoreLabel.layoutY = 20.0 * scale
        timerLabel.layoutX = rightX
        timerLabel.layoutY = 52.0 * scale
        pane.children.addAll(scoreLabel, timerLabel)
    }

    /** Reinicia el marcador y el temporizador (al reiniciar el nivel). */
    fun reset() {
        score = 0
        timeLeft = config.timeLimitSeconds.toDouble()
        timeOver = false
        timerLabel.text = "Tiempo: ${initialTimeText()}"
        scoreLabel.text = "Puntos: 0"
    }

    /** Suma [n] puntos y actualiza el marcador visible. */
    fun addPoints(n: Int) {
        score += n
        scoreLabel.text = "Puntos: $score"
    }

    /** Suma un punto y actualiza el marcador visible. */
    fun addPoint() = addPoints(1)

    /** Avanza el tiempo; avisa cuando se agota el límite. */
    fun tick(dtStep: Double) {
        if (timeOver) return
        timeLeft -= dtStep
        if (timeLeft <= 0.0) {
            timeOver = true
            onTimeOver?.invoke()
            return
        }
        val remaining = kotlin.math.max(0, timeLeft.toInt())
        timerLabel.text = "Tiempo: %02d:%02d".format(remaining / 60, remaining % 60)
    }

    /** Texto inicial del temporizador, calculado a partir del límite configurado. */
    private fun initialTimeText(): String {
        val total = config.timeLimitSeconds
        return "Tiempo: %02d:%02d".format(total / 60, total % 60)
    }
}
