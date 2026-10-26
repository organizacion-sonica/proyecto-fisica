package org.example

/**
 * Fábrica de entidades del juego. Responsabilidad única: crear pájaros y
 * bloques con sus propiedades físicas (masa/densidad) tomadas de
 * [GameConfig]. Sigue OCP: agregar una entidad nueva implica agregar un
 * método acá, no modificar el controller.
 */
class EntityFactory(private val spriteSource: SpriteSource, private val config: GameConfig) {

    /** Crea un pájaro por nombre, con la masa configurada. */
    fun createBird(name: String): Bird = when (name) {
        "red" -> Red(spriteSource, config.birdSize, mass = config.redMass)
        "chuck" -> Chuck(spriteSource, config.birdSize, mass = config.chuckMass)
        "bomb" -> Bomb(spriteSource, config.birdSize, mass = config.bombMass)
        else -> throw IllegalArgumentException("Pájaro desconocido: $name")
    }

    /** Crea un bloque por tipo ("cubo"/"tabla") y material ("madera"/"piedra"). */
    fun createBlock(type: String, material: String): Block {
        val size = if (type == "tabla") config.tablaSize else config.blockSize
        val density = if (material == "madera") config.woodDensity else config.stoneDensity
        return when (material to type) {
            "madera" to "cubo" -> Cubo_maderta(spriteSource, size, density = density)
            "piedra" to "cubo" -> Cubo_piedra(spriteSource, size, density = density)
            "madera" to "tabla" -> Tabla_madera(spriteSource, size, density = density)
            "piedra" to "tabla" -> Tabla_piedra(spriteSource, size, density = density)
            else -> throw IllegalArgumentException("Bloque desconocido: $material $type")
        }
    }
}
