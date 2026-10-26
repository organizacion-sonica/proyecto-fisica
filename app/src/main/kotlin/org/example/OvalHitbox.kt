package org.example

import javafx.scene.shape.Ellipse
import javafx.scene.shape.Shape
import kotlin.math.sqrt

/**
 * Hitbox oval (elipse) centrada en (centerX, centerY) con radios [radiusX]
 * y [radiusY]. Permite hitboxes un poco más "ovaladas" que el círculo,
 * por ejemplo para dianas cuyo sprite no es perfectamente circular.
 */
class OvalHitbox(var radiusX: Double, var radiusY: Double) : Hitbox() {

    private var _cx = 0.0
    private var _cy = 0.0

    override val centerX: Double get() = _cx
    override val centerY: Double get() = _cy

    override fun recenter(cx: Double, cy: Double) {
        _cx = cx
        _cy = cy
    }

    override fun contains(px: Double, py: Double): Boolean {
        val dx = (px - _cx) / radiusX
        val dy = (py - _cy) / radiusY
        return dx * dx + dy * dy <= 1.0
    }

    // ── Colisiones (doble dispatch) ─────────────────────────────────────

    override fun collides(other: Hitbox): Boolean = other.collidesWithOval(this)

    override fun collidesWithCircle(circle: CircularHitbox): Boolean =
        CollisionDetector.ovalCircle(this, circle)

    override fun collidesWithBox(box: BoxHitbox): Boolean =
        CollisionDetector.ovalBox(this, box)

    override fun collidesWithOval(oval: OvalHitbox): Boolean =
        CollisionDetector.ovalOval(this, oval)

    // ── Aparamiento físico ───────────────────────────────────────────────

    override fun circleSpec(): Triple<Double, Double, Double> =
        Triple(_cx, _cy, maxOf(radiusX, radiusY))

    override fun rectangleSpec(): Pair<Pair<Double, Double>, Pair<Double, Double>> =
        Pair(Pair(_cx, _cy), Pair(radiusX, radiusY))

    // ── Contorno ─────────────────────────────────────────────────────────

    override fun createOverlayNode(): Shape {
        val ellipse = Ellipse(_cx, _cy, radiusX, radiusY)
        Hitbox.paintRed(ellipse)
        return ellipse
    }

    override fun syncOverlayNode(node: Shape) {
        val ellipse = node as Ellipse
        ellipse.centerX = _cx
        ellipse.centerY = _cy
        ellipse.radiusX = radiusX
        ellipse.radiusY = radiusY
    }
}
