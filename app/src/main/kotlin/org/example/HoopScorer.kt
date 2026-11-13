package org.example

/**
 * Detección de canastas y tiros fallidos del minijuego de basket (SRP:
 * la lógica de puntuación del aro vive aparte del controlador).
 */
class HoopScorer(
    private val config: GameConfig,
    private val hoop: BasketHoop,
    private val scoreBoard: BasketScoreBoard,
    private val timer: BasketTimer,
    private val physics: PhysicsWorld,
    private val inFlight: MutableList<Bird>,
    private val onBirdFallen: (Bird) -> Unit
) {
    private var globalTime = 0.0
    private var lastHoopCheckTime = 0.0
    private var lastScoredBird: Bird? = null

    /** Avanza el tiempo del mundo. */
    fun tick(dt: Double) { globalTime += dt }

    /** True si el pájaro quedó fuera de la pantalla. */
    fun isOutsideScreen(bird: Bird): Boolean {
        val h = bird.hitbox
        return h.centerX < -100 ||
            h.centerX > config.windowWidth + 200 ||
            h.centerY > config.windowHeight + 100
    }

    /** Procesa canastas y tiros fallidos. */
    fun check() {
        if (globalTime - lastHoopCheckTime < CHECK_COOLDOWN) return

        val hoopCenterX = hoop.centerX
        val hoopCenterY = hoop.centerY
        val hoopRadiusSq = hoop.scoreRadiusSq

        // Retirar pájaros que salieron volando del nivel.
        val fallen = inFlight.filter { isOutsideScreen(it) }
        for (bird in fallen) {
            physics.remove(bird)
            inFlight.remove(bird)
            onBirdFallen(bird)
        }

        for (bird in inFlight) {
            val dx = bird.hitbox.centerX - hoopCenterX
            val dy = bird.hitbox.centerY - hoopCenterY
            val entered = dx * dx + dy * dy < hoopRadiusSq
            if (entered && lastScoredBird !== bird && !timer.isOver()) {
                scoreBoard.addPoint()
                lastScoredBird = bird
                lastHoopCheckTime = globalTime
                hoop.moveToNewPosition()
            }
        }
    }

    fun reset() {
        globalTime = 0.0
        lastHoopCheckTime = 0.0
        lastScoredBird = null
    }

    companion object {
        const val CHECK_COOLDOWN = 0.5
    }
}
