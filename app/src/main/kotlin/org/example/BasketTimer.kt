package org.example

import javafx.scene.control.Label

/**
 * Temporizador del minijuego de basket (SRP: el tiempo vive aparte).
 */
class BasketTimer(private val config: GameConfig, private val label: Label, private val limitSeconds: Int = 90) {
    private val limit = limitSeconds
    var timeLeft: Double = limit.toDouble()
        private set
    var started: Boolean = false
        private set
    var over: Boolean = false
        private set

    fun reset() {
        timeLeft = limit.toDouble()
        started = false
        over = false
        label.text = "Tiempo: 01:00"
    }

    fun start() {
        if (!started) {
            timeLeft = limit.toDouble()
            over = false
            started = true
        }
    }

    /** Avanza el timer; devuelve true si recién se agotó. */
    fun tick(dt: Double): Boolean {
        if (!started || over) return false
        timeLeft -= dt
        if (timeLeft <= 0.0) {
            timeLeft = 0.0
            over = true
            label.text = "Tiempo: 00:00"
            return true
        }
        val remaining = kotlin.math.max(0, timeLeft.toInt())
        label.text = "Tiempo: %02d:%02d".format(remaining / 60, remaining % 60)
        return false
    }

    fun isOver(): Boolean = over
    fun isStarted(): Boolean = started
}
