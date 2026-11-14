package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import kotlin.math.sqrt

/**
 * Aplica la onda expansiva de la Bomba sobre los cuerpos dinámicos cercanos.
 * Responsabilidad única: fuerza radial. Separada de [Box2DWorld] (SRP).
 */
interface ExplosionForce {
    fun apply(bird: GameEntity, spec: ExplosionSpec, bodies: Map<GameEntity, Body>)
}

/** Especificación de la onda expansiva: radio y fuerza. */
data class ExplosionSpec(
    val radiusPx: Float,
    val force: Float
)

/** Resultado de la explosión ya convertido a unidades del mundo físico. */
data class Explosion(
    val centerX: Double,
    val centerY: Double,
    val radiusM: Double,
    val force: Float,
    val pixelsPerMeter: Float
)

class DefaultExplosionForce(private val mapper: ScreenMapper) : ExplosionForce {

    /** Aplica la onda explosiva centrada en [bird]. */
    override fun apply(bird: GameEntity, spec: ExplosionSpec, bodies: Map<GameEntity, Body>) {
        val (cx, cy) = centerOf(bird, bodies)
        val radiusM = (spec.radiusPx / mapper.pixelsPerMeter).toDouble()
        val explosion = Explosion(cx, cy, radiusM, spec.force, mapper.pixelsPerMeter)
        applyImpulses(explosion, bird, bodies)
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
    private fun applyImpulses(explosion: Explosion, bird: GameEntity, bodies: Map<GameEntity, Body>) {
        for ((entity, otherBody) in bodies) {
            if (entity === bird || otherBody.mass.type != MassType.NORMAL) continue
            applyImpulseTo(otherBody, explosion)
        }
    }

    /** Aplica el impulso a un cuerpo si está dentro del radio. */
    private fun applyImpulseTo(body: Body, explosion: Explosion) {
        val t = body.transform
        val dx = t.translationX - explosion.centerX
        val dy = t.translationY - explosion.centerY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist <= 0 || dist >= explosion.radiusM) return
        val falloff = 1.0 - dist / explosion.radiusM
        val fx = (dx / dist) * explosion.force * falloff / explosion.pixelsPerMeter
        val fy = (dy / dist) * explosion.force * falloff / explosion.pixelsPerMeter
        body.applyImpulse(Vector2(fx, fy))
    }
}
