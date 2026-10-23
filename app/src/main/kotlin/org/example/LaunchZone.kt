package org.example

import javafx.scene.layout.Pane

/**
 * Zona invisible cuadrada junto a la resortera donde se fija el pájaro
 * antes de ser lanzado. Responsabilidad única: determinar si un punto
 * está dentro de la zona de lanzamiento.
 */
class LaunchZone(
    private val config: GameConfig,
    private val anchorX: Double,
    private val anchorY: Double
) {
    val size: Double = config.launchZoneSize

    /** Esquina superior izquierda de la zona (centrada en el ancla). */
    val x: Double get() = anchorX - size / 2
    val y: Double get() = anchorY - size / 2

    /** Centro de la zona. */
    val centerX: Double get() = anchorX
    val centerY: Double get() = anchorY

    /** Devuelve true si el punto (px, py) está dentro de la zona. */
    fun contains(px: Double, py: Double): Boolean =
        px in (x)..(x + size) && py in (y)..(y + size)
}
