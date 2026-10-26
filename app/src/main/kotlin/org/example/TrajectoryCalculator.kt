package org.example

import javafx.geometry.Point2D
import kotlin.math.min

/**
 * Calcula la trayectoria balística esperada (en píxeles de pantalla) para saber
 * qué puntos dibujar mientras el jugador estira la resortera.
 * No depende de JavaFX: solo matemática, por lo que es testeable sin toolkit.
 *
 * La integración replica exactamente la del juego real (misma gravedad,
 * mismo amortecimiento) para que la línea dibujada sea una buena
 * aproximación de lo que ocurrirá al soltar.
 */
class TrajectoryCalculator(private val config: GameConfig) {

    /**
     * Devuelve los puntos (esquina superior izquierda) de la trayectoria
     * esperada a partir del estiramiento actual.
     */
    fun predictedDots(start: Point2D, pull: Point2D): List<Point2D> {
        val dots = mutableListOf<Point2D>()
        val mag = min(pull.magnitude(), config.maxStretch)
        if (mag <= 10.0) return dots

        var (vx, vy) = initialVelocity(pull, mag)
        val gPx = config.gravity * config.ppm // px/s² hacia abajo en pantalla

        var x = start.x
        var y = start.y
        var t = 0.0
        val step = 1.0 / 60.0
        var stepsSinceDot = 0
        while (t < 2.5) {
            vx *= damping(step)
            vy *= damping(step)
            vy += gPx * step
            x += vx * step
            y += vy * step
            t += step
            stepsSinceDot++

            // Un punto cada 3 pasos (~0.05 s)
            if (stepsSinceDot >= 3) {
                stepsSinceDot = 0
                if (leftCanvas(x, y)) break
                dots.add(Point2D(x, y))
            }
        }
        return dots
    }

    /** Velocidad inicial (px/s) según la magnitud del estiramiento. */
    private fun initialVelocity(pull: Point2D, mag: Double): Pair<Double, Double> {
        val speed = mag / config.maxStretch * config.maxLaunchSpeed
        return pull.x / mag * speed to pull.y / mag * speed
    }

    /** Factor de amortiguamiento del aire por paso (igual al del juego real). */
    private fun damping(step: Double): Double = 1.0 / (1.0 + 0.1 * step)

    /** True si el punto ya salió de la pantalla (cortar la predicción). */
    private fun leftCanvas(x: Double, y: Double): Boolean =
        y > config.windowHeight || x < 0 || x > config.windowWidth
}
