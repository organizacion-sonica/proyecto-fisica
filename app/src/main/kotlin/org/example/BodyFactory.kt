package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle

/**
 * Crea los cuerpos de dyn4j para pájaros y bloques, separando la
 * construcción física del adaptador [Box2DWorld] (SRP + DIP). El adaptador
 * solo registra y sincroniza; esta fábrica solo construye.
 */
class BodyFactory(private val config: GameConfig, private val mapper: ScreenMapper) {

    private val ppm: Float get() = mapper.pixelsPerMeter

    /** Cuerpo dinámico para un pájaro (círculo) con la masa indicada. */
    fun createBirdBody(bird: GameEntity, mass: Float): Body {
        val (bx, by, radius) = bird.hitbox.circleSpec()
        val pos = mapper.toWorld(bx, by)

        val body = Body()
        val shape = Circle(radius / ppm)
        val density = mass / (Math.PI * (radius / ppm) * (radius / ppm))
        body.addFixture(shape, density, 0.4, 0.3)
        body.setMass(MassType.NORMAL)
        body.translate(pos.x, pos.y)
        body.linearDamping = 0.1
        return body
    }

    /** Cuerpo estático para rebotes (caja). */
    fun createStaticBoxBody(centerXPx: Double, centerYPx: Double, widthPx: Double, heightPx: Double, friction: Double, restitution: Double): Body {
        val body = Body()
        val shape = Rectangle(widthPx / ppm, heightPx / ppm)
        body.addFixture(shape, 0.0, friction, restitution)
        body.setMass(MassType.INFINITE)
        val pos = mapper.toWorld(centerXPx, centerYPx)
        body.translate(pos.x, pos.y)
        return body
    }

    /** Piso estático. */
    fun createGroundBody(): Body {
        val ground = Body()
        ground.setMass(MassType.INFINITE)
        val groundWidth = config.windowWidth / ppm * 2
        val groundShape = Rectangle(groundWidth, 1.0)
        ground.addFixture(groundShape, 0.0, 0.0, 0.0)
        ground.translate(config.windowWidth / 2.0 / ppm, config.floorMargin / ppm - 0.5)
        return ground
    }
}
