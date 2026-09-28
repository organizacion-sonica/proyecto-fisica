package org.example

/**
 * Fábrica de entidades del juego. Responsabilidad única: crear pájaros y bloques.
 * Sigue OCP: agregar una entidad nueva implica agregar un método, no modificar
 * el controller.
 */
class EntityFactory(private val spriteSource: SpriteSource, private val config: GameConfig) {

    /** Crea un pájaro por nombre. */
    fun createBird(name: String): Bird = when (name) {
        "red" -> Red(spriteSource, config.birdSize, mass = config.redMass)
        "chuck" -> Chuck(spriteSource, config.birdSize, mass = config.chuckMass)
        "bomb" -> Bomb(spriteSource, config.birdSize, mass = config.bombMass)
        else -> throw IllegalArgumentException("Pájaro desconocido: $name")
    }

    /** Crea los tres pájaros del juego en orden de lanzamiento. */
    fun createBirdQueue(): List<Bird> = listOf(
        createBird("red"),
        createBird("chuck"),
        createBird("bomb")
    )

    /** Crea un bloque por tipo y material. */
    fun createBlock(type: String, material: String): Block {
        val size = when (type) {
            "tabla" -> config.tablaSize
            "cubo" -> config.blockSize
            else -> config.blockSize
        }
        return when (material to type) {
            "madera" to "cubo" -> Cubo_maderta(spriteSource, size)
            "piedra" to "cubo" -> Cubo_piedra(spriteSource, size)
            "madera" to "tabla" -> Tabla_madera(spriteSource, size)
            "piedra" to "tabla" -> Tabla_piedra(spriteSource, size)
            else -> throw IllegalArgumentException("Bloque desconocido: $material $type")
        }
    }
}