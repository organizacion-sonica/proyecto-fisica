package org.example

import javafx.scene.shape.Circle
import javafx.scene.shape.Shape

/**
 * Hitbox circular. Implementa su colisión con cada forma concreta
 * (doble dispatch, sin más `when`s a la vista ni checks de tipos).
 */
class CircularHitbox(var radius: Double) : Hitbox() {

    private var _cx = 0.0
    private var _cy = 0.0

    override val centerX: Double get() = _cx
    override val centerY: Double get() = _cy

    override fun recenter(cx: Double, cy: Double) {
        _cx = cx
        _cy = cy
    }

    override fun contains(px: Double, py: Double): Boolean {
        val dx = px - _cx
        val dy = py - _cy
        return dx * dx + dy * dy <= radius * radius
    }

    // ── Colisiones: el receptor decide, la otra parte visita ─────────────

    override fun collides(other: Hitbox): Boolean = other.collidesWithCircle(this)

    override fun collidesWithCircle(circle: CircularHitbox): Boolean =
        CollisionDetector.circleCircle(this, circle)

    override fun collidesWithBox(box: BoxHitbox): Boolean =
        CollisionDetector.boxCircle(box, this)

    override fun collidesWithOval(oval: OvalHitbox): Boolean =
        CollisionDetector.circleOval(this, oval)

    // ── Aparamiento físico ───────────────────────────────────────────────

    override fun circleSpec(): Triple<Double, Double, Double> = Triple(_cx, _cy, radius)

    override fun rectangleSpec(): Pair<Pair<Double, Double>, Pair<Double, Double>> =
        Pair(Pair(_cx, _cy), Pair(radius, radius))

    // ── Contorno ───────────────────────────────────────────────────────────

    override fun createOverlayNode(): Shape {
        val circle = Circle(_cx, _cy, radius)
        Hitbox.paintRed(circle)
        return circle
    }

    override fun syncOverlayNode(node: Shape) {
        val circle = node as Circle
        circle.centerX = _cx
        circle.centerY = _cy
        circle.radius = radius
    }
}
