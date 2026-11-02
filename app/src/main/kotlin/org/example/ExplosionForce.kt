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

    /** Aplica la onda expansiva centrada en [bird] (o su hitbox si ya no tiene cuerpo). */
    fun apply(bird: GameEntity, radiusPx: Float, force: Float, bodies: Map<GameEntity, Body>) {
        val body = bodies[bird]
        val centerWorld = if (body != null) {
            Vector2(body.transform.translationX, body.transform.translationY)
        } else {
            mapper.toWorld(bird.hitbox.centerX, bird.hitbox.centerY)
        }
        val cx = centerWorld.x
        val cy = centerWorld.y
        val radiusM = radiusPx / mapper.pixelsPerMeter
        val ppm = mapper.pixelsPerMeter

        for ((entity, otherBody) in bodies) {
            if (entity === bird) continue
            if (otherBody.mass.type != MassType.NORMAL) continue

            val otherT = otherBody.transform
            val dx = otherT.translationX - cx
            val dy = otherT.translationY - cy
            val dist = sqrt(dx * dx + dy * dy)

            if (dist > 0 && dist < radiusM) {
                val falloff = 1.0 - dist / radiusM
                val fx = (dx / dist) * force * falloff / ppm
                val fy = (dy / dist) * force * falloff / ppm
                otherBody.applyImpulse(Vector2(fx, fy))
            }
        }
    }
}
