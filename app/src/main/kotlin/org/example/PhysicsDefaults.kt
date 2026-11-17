package org.example

object PhysicsDefaults {
    fun physicsFactory(config: GameConfig): () -> PhysicsWorld = {
        val m = DefaultScreenMapper(config)
        Box2DWorld(config.gravity, DefaultBodyFactory(config, m), DefaultEntitySynchronizer(m), DefaultExplosionForce(m))
    }
}
