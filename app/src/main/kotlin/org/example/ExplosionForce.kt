package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import kotlin.math.sqrt

/**
 * Aplica la onda expansiva de la Bomba sobre los cuerpos dinámicos cercanos.
 * Responsabilidad única: fuerza radial. Separada de [Box2DWorld] (SRP).
 */
class ExplosionForce(private val mapper: ScreenMapper) {

    /** Aplica la onda expansiva centrada en [bird]. */
    fun apply(bird: GameEntity, radiusPx: Float, force: Float, bodies: Map<GameEntity, Body>) {
        val (cx, cy) = centerOf(bird, bodies)
        val radiusM = (radiusPx / mapper.pixelsPerMeter).toDouble()
        applyImpulses(cx, cy, radiusM, force, bird, bodies)
    }

    /** Centro del pájaro en coordenadas del mundo (cuerpo o hitbox). */
    private fun centerOf(bird: GameEntity, bodies: Map<GameEntity, Body>): Pair<Double, Double> {
        val body = bodies[bird]
        return if (body != null) {
            body.transform.translationX to body.transform.translationY
        } else {
            val v = mapper.toWorld(bird.hitbox.centerX, bird.hitbox.centerY)
            v.x to v.y
        }
    }

    /** Aplica el impulso radial a los cuerpos dinámicos dentro del radio. */
    private fun applyImpulses(
        cx: Double, cy: Double, radiusM: Double, force: Float,
        bird: GameEntity, bodies: Map<GameEntity, Body>
    ) {
        val ppm = mapper.pixelsPerMeter
        for ((entity, otherBody) in bodies) {
            if (entity === bird) continue
            if (otherBody.mass.type != MassType.NORMAL) continue
            val otherT = otherBody.transform
            val dx = otherT.translationX - cx
            val dy = otherT.translationY - cy
            val dist = sqrt(dx * dx + dy * dy)
            if (dist <= 0 || dist >= radiusM) continue
            val falloff = 1.0 - dist / radiusM
            val fx = (dx / dist) * force * falloff / ppm
            val fy = (dy / dist) * force * falloff / ppm
            otherBody.applyImpulse(Vector2(fx, fy))
        }
    }
}
