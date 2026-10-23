package org.example

/**
 * Objetivo/diana de práctica. Responsabilidad única: guardar su posición
 * y su estado (viva/destruida y cuándo revivir). El movimiento sinusoidal
 * se calcula con [cy].
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
    var respawnAt: Double = Double.POSITIVE_INFINITY // tiempo del mundo donde reaparece
) {
    /** Hitbox de la diana, centrado donde se encuentra el sprite. */
    val hitbox: Hitbox
        get() = view.hitbox

    /** Y de la diana en función del tiempo del mundo. */
    fun cy(t: Double): Double = baseY + amp * kotlin.math.sin(speed * t)
}
