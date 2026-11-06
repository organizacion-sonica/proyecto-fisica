package org.example

import org.dyn4j.dynamics.Body

/**
 * Registro de cuerpos físicos separado por tipo (pájaros vs. cajas estáticas),
 * para mejorar la cohesión de [Box2DWorld] (SRP).
 */
class BodyRegistry {
    private val birdBodies = mutableMapOf<GameEntity, Body>()
    private val staticBodies = mutableMapOf<String, Body>()

    fun registerBird(entity: GameEntity, body: Body) { birdBodies[entity] = body }
    fun registerStatic(name: String, body: Body) { staticBodies[name] = body }

    fun birdBody(entity: GameEntity): Body? = birdBodies[entity]
    fun staticBody(name: String): Body? = staticBodies[name]

    fun removeBird(entity: GameEntity): Body? = birdBodies.remove(entity)
    fun removeStatic(name: String): Body? = staticBodies.remove(name)

    fun birdBodies(): Map<GameEntity, Body> = birdBodies
    fun staticBodies(): Map<String, Body> = staticBodies

    fun clear() { birdBodies.clear(); staticBodies.clear() }
}
