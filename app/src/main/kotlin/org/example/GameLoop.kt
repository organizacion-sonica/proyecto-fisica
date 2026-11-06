package org.example

import javafx.animation.AnimationTimer

/**
 * Bucle de juego a 60 fps fijos (SRP: la simulación a paso fijo vive aparte
 * del juego, que solo decide qué hacer en cada paso).
 */
class GameLoop(
    private val physics: PhysicsWorld,
    private val onStep: (Double) -> Unit,
    private val onRemoveSettled: () -> Unit,
    private val onTickSettled: (Double) -> Unit
) {
    private var timer: AnimationTimer? = null

    fun start() {
        var lastTime = 0L
        var accumulator = 0.0
        val fixedDt = 1.0 / 60.0

        timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (lastTime == 0L) {
                    lastTime = now
                    return
                }
                val elapsed = (now - lastTime) / 1_000_000_000.0
                lastTime = now
                accumulator += elapsed.coerceAtMost(0.1)

                while (accumulator >= fixedDt) {
                    physics.step(fixedDt)
                    onStep(fixedDt)
                    accumulator -= fixedDt
                }

                onRemoveSettled()
                onTickSettled(fixedDt)
            }
        }.also { it.start() }
    }

    fun stop() {
        timer?.stop()
    }
}
