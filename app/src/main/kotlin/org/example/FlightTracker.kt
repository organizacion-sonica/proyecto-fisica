package org.example

import javafx.scene.layout.Pane

/**
 * Seguimiento de los pájaros lanzados: cuáles siguen en vuelo, cuáles
 * cayeron al piso y cuándo desaparecen con fade out. Responsabilidad única:
 * el ciclo de vida post-lanzamiento (SRP).
 */
class FlightTracker(
    private val physics: PhysicsWorld,
    private val pane: Pane,
    private val floorHitbox: Hitbox,
    private val onBirdSettled: (Bird) -> Unit
) {
    /** Pájaros ya lanzados que siguen en vuelo. */
    val inFlight: MutableList<Bird> = mutableListOf()

    private val inFlightTime = mutableMapOf<Bird, Double>()
    private val settledBirds = mutableMapOf<Bird, Double>()
    private val fadingBirds = mutableSetOf<Bird>()

    /** Registra un lanzamiento. */
    fun recordLaunch(bird: Bird) {
        inFlight += bird
        inFlightTime[bird] = 0.0
    }

    /** Retira del mundo los pájaros que ya quedaron en reposo / tocaron el piso. */
    fun removeSettled() {
        val resting = inFlight.filter {
            physics.isResting(it) ||
                (inFlightTime[it] ?: 0.0) > 6.0 ||
                CollisionDetector.collides(it.hitbox, floorHitbox)
        }
        for (bird in resting) {
            physics.remove(bird)
            inFlight.remove(bird)
            inFlightTime.remove(bird)
            onBirdSettled(bird)
            settledBirds[bird] = 0.0
        }
    }

    /** Acumula tiempo y desvanece los pájaros que llevan tiempo en el piso. */
    fun tick(dt: Double) {
        for (bird in inFlight) {
            inFlightTime[bird] = (inFlightTime[bird] ?: 0.0) + dt
        }

        val toFade = mutableListOf<Bird>()
        for ((bird, elapsed) in settledBirds) {
            if (bird in fadingBirds) continue
            val next = elapsed + dt
            if (next >= 0.0) {
                toFade += bird
                settledBirds[bird] = next
            } else {
                settledBirds[bird] = next
            }
        }
        for (bird in toFade) {
            fadingBirds += bird
            bird.view.fadeOutAndRemove(pane) {
                settledBirds.remove(bird)
                fadingBirds.remove(bird)
            }
        }
    }

    /** Marca un pájaro como asentado (p. ej. salió de la pantalla) para que se desvanezca. */
    fun markSettled(bird: Bird) {
        settledBirds[bird] = 0.0
    }

    /** Limpia el estado (al reiniciar el nivel). */
    fun clear() {
        inFlight.clear()
        inFlightTime.clear()
        settledBirds.clear()
        fadingBirds.clear()
    }
}
