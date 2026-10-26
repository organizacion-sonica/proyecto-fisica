package org.example

/**
 * Servicio responsable de enlazar colisiones entre [Hitbox]s.
 *
 * La decisión de QUÉ combinación aplicar vive en cada hitbox (doble dispatch,
 * OCP: no hay `when` por tipos y agregar una forma nueva no modifica esto);
 * este objeto conserva solo la matemática pura de cada par de formas.
 * No depende de JavaFX, por lo que se puede probar en unit tests.
 */
object CollisionDetector {

    /** True si las dos hitboxes se superponen (dispatch polimórfico). */
    fun collides(a: Hitbox, b: Hitbox): Boolean = a.collides(b)

    /** Círculo vs círculo: comparación de distancia contra suma de radios. */
    fun circleCircle(a: CircularHitbox, b: CircularHitbox): Boolean {
        val dx = b.centerX - a.centerX
        val dy = b.centerY - a.centerY
        val radii = a.radius + b.radius
        return dx * dx + dy * dy <= radii * radii
    }

    /** Caja vs caja: AABB contra AABB. */
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

    /** Rectángulo vs círculo (mismo caso, parámetros al revés). */
    fun boxCircle(box: BoxHitbox, circle: CircularHitbox): Boolean = circleBox(circle, box)
}
