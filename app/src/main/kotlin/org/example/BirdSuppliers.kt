package org.example

/**
 * Proveedores por defecto de pájaros (composición de concretos), aislados de
 * [EntityFactory] para que esta dependa de abstracciones (DIP).
 */
object BirdSuppliers {
    fun default(spriteSource: SpriteSource, config: GameConfig): Map<String, () -> Bird> = mapOf(
        "red" to { Red(spriteSource, config.birdSize, mass = config.redMass) },
        "chuck" to { Chuck(spriteSource, config.birdSize, mass = config.chuckMass) },
        "bomb" to { Bomb(spriteSource, config.birdSize, mass = config.bombMass) }
    )
}
