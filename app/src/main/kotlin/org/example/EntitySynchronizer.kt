package org.example

import org.dyn4j.dynamics.Body

/**
 * Sincroniza la posición/rotación de las entidades con sus cuerpos de dyn4j.
 * Responsabilidad única: traducir el estado del mundo físico a la vista.
 * Separada de [Box2DWorld] para reducir su tamaño y acoplamiento (SRP).
 */
interface EntitySynchronizer {
    fun sync(bodies: Map<GameEntity, org.dyn4j.dynamics.Body>)
}

class DefaultEntitySynchronizer(private val mapper: ScreenMapper) : EntitySynchronizer {

    /** Sincroniza todas las entidades registradas con sus cuerpos. */
    override fun sync(bodies: Map<GameEntity, Body>) {
        for ((entity, body) in bodies) {
            val t = body.transform
            val (sx, sy) = mapper.toScreen(t.translationX, t.translationY)
            val dx = sx - entity.hitbox.centerX
            val dy = sy - entity.hitbox.centerY
            if (kotlin.math.abs(dx) > 0.01 || kotlin.math.abs(dy) > 0.01) {
                entity.moveBy(dx, dy)
            }
            val angleDeg = Math.toDegrees(t.rotationAngle)
            if (kotlin.math.abs(angleDeg) > 0.1) {
                entity.setRotation(angleDeg)
            }
        }
    }
}
