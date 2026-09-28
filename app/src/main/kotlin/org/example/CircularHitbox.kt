package org.example

/**
 * Hitbox circular.
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
}
