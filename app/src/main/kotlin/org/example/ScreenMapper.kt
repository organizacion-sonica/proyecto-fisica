package org.example

import org.dyn4j.geometry.Vector2

/**
 * Conversión de coordenadas entre pantalla (px, Y-down) y el mundo físico
 * dyn4j (m, Y-up). Responsabilidad única: traducir unidades; no conoce
 * cuerpos ni entidades (SRP + ISP).
 */
class ScreenMapper(private val config: GameConfig) {

    private val ppm: Float = config.ppm

    /** Pantalla (px, Y-down) → dyn4j (m, Y-up). */
    fun toWorld(sx: Double, sy: Double): Vector2 {
        val wx = sx / ppm
        val wy = (config.windowHeight - sy) / ppm
        return Vector2(wx, wy)
    }

    /** dyn4j (m, Y-up) → Pantalla (px, Y-down). */
    fun toScreen(wx: Double, wy: Double): Pair<Double, Double> {
        val sx = wx * ppm
        val sy = config.windowHeight - wy * ppm
        return sx to sy
    }

    /** Píxeles por metro (escala física). */
    val pixelsPerMeter: Float get() = ppm
}
