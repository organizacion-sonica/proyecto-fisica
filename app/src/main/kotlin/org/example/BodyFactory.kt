package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle

/**
 * Especificación de una caja estática (SRP: agrupa los 6 parámetros en un
 * objeto de valor, evitando métodos con demasiados parámetros).
 */
data class BoxSpec(
    val centerXPx: Double,
    val centerYPx: Double,
    val widthPx: Double,
    val heightPx: Double,
    val friction: Double = 0.4,
    val restitution: Double = 0.4
)

/**
 * Crea los cuerpos de dyn4j para pájaros y bloques, separando la
 * construcción física del adaptador [Box2DWorld] (SRP + DIP). El adaptador
 * solo registra y sincroniza; esta fábrica solo construye.
 */
interface BodyFactory {
    fun createBirdBody(bird: GameEntity, mass: Float): org.dyn4j.dynamics.Body
    fun createStaticBoxBody(spec: BoxSpec): org.dyn4j.dynamics.Body
    fun createGroundBody(): org.dyn4j.dynamics.Body
}

class DefaultBodyFactory(private val config: GameConfig, private val mapper: ScreenMapper) : BodyFactory {

    private val ppm: Float get() = mapper.pixelsPerMeter

    /** Cuerpo dinámico para un pájaro (círculo) con la masa indicada. */
    override fun createBirdBody(bird: GameEntity, mass: Float): Body {
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
    override fun createStaticBoxBody(spec: BoxSpec): Body {
        val body = Body()
        val shape = Rectangle(spec.widthPx / ppm, spec.heightPx / ppm)
        body.addFixture(shape, 0.0, spec.friction, spec.restitution)
        body.setMass(MassType.INFINITE)
        val pos = mapper.toWorld(spec.centerXPx, spec.centerYPx)
        body.translate(pos.x, pos.y)
        return body
    }

    /** Piso estático. */
    override fun createGroundBody(): Body {
        val ground = Body()
        ground.setMass(MassType.INFINITE)
        val groundWidth = config.windowWidth / ppm * 2
        val groundShape = Rectangle(groundWidth, 1.0)
        ground.addFixture(groundShape, 0.0, 0.0, 0.0)
        ground.translate(config.windowWidth / 2.0 / ppm, config.floorMargin / ppm - 0.5)
        return ground
    }
}
