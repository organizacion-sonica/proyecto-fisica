package org.example

/**
 * Zona invisible cuadrada junto a la resortera donde se fija el pájaro
 * antes de ser lanzado. Responsabilidad única: determinar si un punto
 * está dentro de la zona de lanzamiento (sus coordenadas quedan internas).
 */
class LaunchZone(
    config: GameConfig,
    private val anchorX: Double,
    private val anchorY: Double
) {
    /** Tamaño (px) del lado de la zona cuadrada. */
    private val size: Double = config.launchZoneSize

    /** Esquina superior izquierda de la zona (centrada en el ancla). */
    private val x: Double get() = anchorX - size / 2
    private val y: Double get() = anchorY - size / 2

    /** Devuelve true si el punto (px, py) está dentro de la zona. */
    fun contains(px: Double, py: Double): Boolean =
        px in x..(x + size) && py in y..(y + size)

    /** Área (px²) de la zona de lanzamiento (comportamiento del concepto). */
    fun area(): Double = size * size
}
