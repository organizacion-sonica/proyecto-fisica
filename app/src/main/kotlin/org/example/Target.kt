package org.example

/**
 * Objetivo/diana de práctica. Responsabilidad única: mantener su posición
 * y su estado (viva/destruida y cuándo revivir). El recorrido/ubicación
 * sinusoidal se calcula con baseY, amp y speed.
 */
class Target(
    val view: EntityView,
    val cx: Double,
    val baseY: Double,
    val amp: Double,
    val speed: Double,
    val radius: Double,
    val size: Double,
    var hit: Boolean = false,
    var alive: Boolean = true,
    var respawnAt: Double = Double.POSITIVE_INFINITY
) {
    /** Y del target en función del tiempo del mundo. */
    fun cy(t: Double): Double = baseY + amp * kotlin.math.sin(speed * t)
}
