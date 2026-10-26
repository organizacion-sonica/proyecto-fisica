package org.example

import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

/**
 * Hitbox rectangular (AABB, alineada a los ejes), centrada en (centerX, centerY).
 * Implementa su colisión con cada forma concreta (doble dispatch).
 */
class BoxHitbox(var halfWidth: Double, var halfHeight: Double) : Hitbox() {

    private var _cx = 0.0
    private var _cy = 0.0

    override val centerX: Double get() = _cx
    override val centerY: Double get() = _cy

    override fun recenter(cx: Double, cy: Double) {
        _cx = cx
        _cy = cy
    }

    override fun contains(px: Double, py: Double): Boolean =
        px in (_cx - halfWidth)..(_cx + halfWidth) &&
        py in (_cy - halfHeight)..(_cy + halfHeight)

    // ── Colisiones: el receptor decide, la otra parte visita ─────────────

    override fun collides(other: Hitbox): Boolean = other.collidesWithBox(this)

    override fun collidesWithCircle(circle: CircularHitbox): Boolean =
        CollisionDetector.boxCircle(this, circle)

    override fun collidesWithBox(box: BoxHitbox): Boolean =
        CollisionDetector.boxBox(this, box)

    // ── Aparamiento físico ───────────────────────────────────────────────

    override fun circleSpec(): Triple<Double, Double, Double> =
        Triple(_cx, _cy, kotlin.math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight))

    override fun rectangleSpec(): Pair<Pair<Double, Double>, Pair<Double, Double>> =
        Pair(Pair(_cx, _cy), Pair(halfWidth, halfHeight))

    // ── Contorno ───────────────────────────────────────────────────────────

    override fun createOverlayNode(): Shape {
        val rect = Rectangle(_cx - halfWidth, _cy - halfHeight, halfWidth * 2, halfHeight * 2)
        Hitbox.paintRed(rect)
        return rect
    }

    override fun syncOverlayNode(node: Shape) {
        val rect = node as Rectangle
        rect.x = _cx - halfWidth
        rect.y = _cy - halfHeight
        rect.width = halfWidth * 2
        rect.height = halfHeight * 2
    }
}
