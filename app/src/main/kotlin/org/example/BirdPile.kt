package org.example

import javafx.geometry.Point2D
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

    /** Radio de agarre para detectar pájaros bajo el puntero. */
    val grabRadius: Double = config.slingshotGrabRadius

    /** Crea los tres pájaros iniciales de la pila. */
    fun setup() {
        slotTypes.forEach { spawnSlotBird(it) }
    }

    /** Pájaros disponibles para agarrar (instantánea). */
    fun available(): List<Bird> = slotBirds.toList()

    /** Devuelve el pájaro más cercano al punto (x, y), o null si no hay ninguno dentro del radio de agarre. */
    fun birdNear(x: Double, y: Double): Bird? {
        return available().firstOrNull { isNearBird(it, x, y) }
    }

    private fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        return Point2D(x, y).distance(cx, cy) < grabRadius
    }

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
