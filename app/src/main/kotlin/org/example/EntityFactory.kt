package org.example

/**
 * Fábrica de entidades del juego. Responsabilidad única: crear pájaros y
 * bloques con sus propiedades físicas (masa/densidad) tomadas de
 * [GameConfig]. Sigue OCP: agregar una entidad nueva implica agregar un
 * método acá, no modificar el controller.
 */
class EntityFactory(private val spriteSource: SpriteSource, private val config: GameConfig) {

    /** Fábricas de pájaros por nombre (OCP: agregar un pájaro = agregar una entrada). */
    private val birdFactories: Map<String, () -> Bird> = mapOf(
        "red" to { Red(spriteSource, config.birdSize, mass = config.redMass) },
        "chuck" to { Chuck(spriteSource, config.birdSize, mass = config.chuckMass) },
        "bomb" to { Bomb(spriteSource, config.birdSize, mass = config.bombMass) }
    )

    /** Crea un pájaro por nombre, con la masa configurada. */
    fun createBird(name: String): Bird =
        birdFactories[name]?.invoke() ?: throw IllegalArgumentException("Pájaro desconocido: $name")
}
