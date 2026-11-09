package org.example

/**
 * Fábrica de hitboxes: aísla la creación de formas concretas fuera de las
 * entidades (DIP).
 */
object HitboxFactory {
    fun circular(radius: Double): CircularHitbox = CircularHitbox(radius)
    fun box(halfWidth: Double, halfHeight: Double): BoxHitbox = BoxHitbox(halfWidth, halfHeight)
    fun oval(radiusX: Double, radiusY: Double): OvalHitbox = OvalHitbox(radiusX, radiusY)
}
