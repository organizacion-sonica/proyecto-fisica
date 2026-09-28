package org.example

/**
 * Servicio responsable de decidir si dos hitboxes se tocan (Responsabilidad
 * Única: la geometría vive en cada [Hitbox], la colisión vive acá). Es puro,
 * sin JavaFX, por lo que se puede probar en unit tests.
 */
object CollisionDetector {

    /** Devuelve true si las dos hitboxes se superponen (dispatch por tipo). */
    fun collides(a: Hitbox, b: Hitbox): Boolean = when {
        a is CircularHitbox && b is CircularHitbox -> circleCircle(a, b)
        a is CircularHitbox && b is BoxHitbox -> circleBox(a, b)
        a is BoxHitbox && b is CircularHitbox -> circleBox(b, a)
        a is BoxHitbox && b is BoxHitbox -> boxBox(a, b)
        else -> false
    }

    fun circleCircle(a: CircularHitbox, b: CircularHitbox): Boolean {
        val dx = b.centerX - a.centerX
        val dy = b.centerY - a.centerY
        val radii = a.radius + b.radius
        return dx * dx + dy * dy <= radii * radii
    }

    fun boxBox(a: BoxHitbox, b: BoxHitbox): Boolean =
        kotlin.math.abs(a.centerX - b.centerX) < (a.halfWidth + b.halfWidth) &&
        kotlin.math.abs(a.centerY - b.centerY) < (a.halfHeight + b.halfHeight)

    /** Círculo vs rectángulo: el punto del rectángulo más cercano al círculo. */
    fun circleBox(circle: CircularHitbox, box: BoxHitbox): Boolean {
        val clampX = circle.centerX.coerceIn(box.centerX - box.halfWidth, box.centerX + box.halfWidth)
        val clampY = circle.centerY.coerceIn(box.centerY - box.halfHeight, box.centerY + box.halfHeight)
        val dx = circle.centerX - clampX
        val dy = circle.centerY - clampY
        return dx * dx + dy * dy <= circle.radius * circle.radius
    }
}
