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

    // ── Óvalo vs otras formas ─────────────────────────────────────────

    /** Óvalo vs círculo: se escala el espacio al círculo unitario. */
    fun ovalCircle(oval: OvalHitbox, circle: CircularHitbox): Boolean {
        val rx = oval.radiusX.coerceAtLeast(1.0)
        val ry = oval.radiusY.coerceAtLeast(1.0)
        val dx = (circle.centerX - oval.centerX) / rx
        val dy = (circle.centerY - oval.centerY) / ry
        val d = kotlin.math.sqrt(dx * dx + dy * dy)
        val effective = circle.radius / kotlin.math.min(rx, ry)
        return d <= 1.0 + effective
    }

    /** Círculo vs óvalo (parámetros al revés). */
    fun circleOval(circle: CircularHitbox, oval: OvalHitbox): Boolean = ovalCircle(oval, circle)

    /** Óvalo vs rectángulo: aproximación por esquinas y centro. */
    fun ovalBox(oval: OvalHitbox, box: BoxHitbox): Boolean {
        // Centro del óvalo dentro de la caja
        if (box.contains(oval.centerX, oval.centerY)) return true
        // Centro de la caja dentro del óvalo
        if (oval.contains(box.centerX, box.centerY)) return true
        // Cualquier esquina de la caja dentro del óvalo
        val corners = listOf(
            box.centerX - box.halfWidth to box.centerY - box.halfHeight,
            box.centerX + box.halfWidth to box.centerY - box.halfHeight,
            box.centerX - box.halfWidth to box.centerY + box.halfHeight,
            box.centerX + box.halfWidth to box.centerY + box.halfHeight
        )
        for ((cx, cy) in corners) {
            if (oval.contains(cx, cy)) return true
        }
        return false
    }

    /** Rectángulo vs óvalo (parámetros al revés). */
    fun boxOval(box: BoxHitbox, oval: OvalHitbox): Boolean = ovalBox(oval, box)

    /** Óvalo vs óvalo: aproximación con círculos envolventes. */
    fun ovalOval(a: OvalHitbox, b: OvalHitbox): Boolean {
        val ra = kotlin.math.max(a.radiusX, a.radiusY)
        val rb = kotlin.math.max(b.radiusX, b.radiusY)
        val dx = b.centerX - a.centerX
        val dy = b.centerY - a.centerY
        val r = ra + rb
        return dx * dx + dy * dy <= r * r
    }
}
