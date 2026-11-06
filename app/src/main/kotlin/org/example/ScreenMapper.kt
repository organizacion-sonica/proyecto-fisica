package org.example

import org.dyn4j.geometry.Vector2

/**
 * Abstracción de la conversión de coordenadas pantalla ↔ mundo físico
 * (DIP): los colaboradores dependen de esta interfaz, no de la
 * implementación concreta.
 */
interface ScreenMapper {
    /** Pantalla (px, Y-down) → dyn4j (m, Y-up). */
    fun toWorld(sx: Double, sy: Double): Vector2

    /** dyn4j (m, Y-up) → Pantalla (px, Y-down). */
    fun toScreen(wx: Double, wy: Double): Pair<Double, Double>

    /** Píxeles por metro (escala física). */
    val pixelsPerMeter: Float
}

/**
 * Implementación concreta de [ScreenMapper] basada en [GameConfig].
 */
class DefaultScreenMapper(private val config: GameConfig) : ScreenMapper {

    private val ppm: Float = config.ppm

    override fun toWorld(sx: Double, sy: Double): Vector2 {
        val wx = sx / ppm
        val wy = (config.windowHeight - sy) / ppm
        return Vector2(wx, wy)
    }

    override fun toScreen(wx: Double, wy: Double): Pair<Double, Double> {
        val sx = wx * ppm
        val sy = config.windowHeight - wy * ppm
        return sx to sy
    }

    override val pixelsPerMeter: Float get() = ppm
}
