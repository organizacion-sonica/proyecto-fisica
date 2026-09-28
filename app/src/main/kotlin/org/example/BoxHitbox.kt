package org.example

/**
 * Hitbox rectangular (AABB, alineada a los ejes), centrada en (centerX, centerY).
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
}
