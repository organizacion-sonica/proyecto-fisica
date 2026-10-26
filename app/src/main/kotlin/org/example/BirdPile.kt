package org.example

import javafx.scene.layout.Pane

/**
 * Pila de pájaros disponible (esquina superior izquierda, suministro
 * infinito). Responsabilidad única: crear, mostrar y reubicar los pájaros
 * de la pila. La lógica de lanzamiento vive fuera, en [SlingshotGame].
 */
class BirdPile(
    private val pane: Pane,
    private val entityFactory: EntityFactory,
    private val config: GameConfig
) {
    /** Tipos y orden de la pila. */
    private val slotTypes = listOf("red", "chuck", "bomb")

    private val slotBirds = mutableListOf<Bird>()
    private val birdType = mutableMapOf<Bird, String>()

    /** Crea los tres pájaros iniciales de la pila. */
    fun setup() {
        slotTypes.forEach { spawnSlotBird(it) }
    }

    /** Pájaros disponibles para agarrar (instantánea). */
    fun available(): List<Bird> = slotBirds.toList()

    /** Coordenada (esq. sup. izq.) del slot [index]. */
    fun slotCoord(index: Int): Pair<Double, Double> {
        val gap = 8.0 * config.scale
        return 20.0 * config.scale to 20.0 * config.scale + index * (config.birdSize + gap)
    }

    /** Crea un pájaro del [type] como visual puro en la pila (sin física). */
    private fun spawnSlotBird(type: String) {
        val index = slotTypes.indexOf(type)
        val bird = entityFactory.createBird(type)
        val (sx, sy) = slotCoord(index)
        bird.placeAt(pane, sx, sy)
        slotBirds += bird
        birdType[bird] = type
    }

    /** Retira el pájaro de la pila y repone un ejemplar nuevo del mismo tipo. */
    fun withdraw(bird: Bird) {
        val type = birdType[bird] ?: return
        slotBirds.remove(bird)
        spawnSlotBird(type)
    }

    /** Devuelve un pájaro no lanzado a la posición de su slot. */
    fun putBack(bird: Bird) {
        val type = birdType[bird] ?: return
        val (sx, sy) = slotCoord(slotTypes.indexOf(type))
        bird.placeAt(pane, sx, sy)
    }
}
