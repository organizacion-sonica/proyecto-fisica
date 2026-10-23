package org.example

import javafx.scene.layout.Pane

/**
 * Gestiona las dianas: la fija grande, las móviles (su movimiento sinusoide),
 * el ciclo de vida de cada una (viva / golpeada / reaparición) y la detección
 * de colisiones con los pájaros en vuelo.
 * Responsabilidad única: el objetivo y su movimiento; no conoce puntuación
 * ni la entrada del jugador.
 */
class DianaManager(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val pane: Pane
) {
    private val targets = mutableListOf<Target>()

    /** Segundos que llevan los pájaros en vuelo desde el último punto del mundo. */
    private var globalTime = 0.0

    /** Notifica cada vez que una diana pasa a estado "golpeada". */
    var onTargetHit: ((Target) -> Unit)? = null

    fun setup() {
        targets.clear()
        createFixedDiana()
        createMobileDianas()
    }

    /** Avanza el mundo y comprueba colisiones con los pájaros en vuelo. */
    fun update(dtStep: Double, inFlight: List<Bird>) {
        globalTime += dtStep
        moveTargets()
        checkBirdCollisions(inFlight)
    }

    // ── Movimiento de las dianas ─────────────────────────────────────

    private fun moveTargets() {
        for (t in targets) {
            if (!t.alive) {
                // Respawn tras el tiempo de espera
                if (globalTime >= t.respawnAt) {
                    t.alive = true
                    t.hit = false
                    val cy = t.cy(globalTime)
                    t.view.setPosition(t.cx - t.size / 2, cy - t.size / 2)
                    t.view.imageView.isVisible = true
                    t.view.setHitboxVisible(true)
                }
                continue
            }
            val cy = t.cy(globalTime)
            t.view.setPosition(t.cx - t.size / 2, cy - t.size / 2)
        }
    }

    // ── Colisiones ──────────────────────────────────────────────────

    /**
     * Comprueba si algún pájaro en vuelo toca alguna diana viva.
     * Al golpearla se marca, se notifica y programa su reaparición.
     */
    private fun checkBirdCollisions(birds: List<Bird>) {
        for (bird in birds) {
            for (t in targets) {
                if (!t.alive || t.hit) continue
                if (CollisionDetector.collides(bird.hitbox, t.hitbox)) {
                    markHit(t)
                    onTargetHit?.invoke(t)
                }
            }
        }
    }

    /**
     * Derriba todas las dianas dentro del radio y notifica el golpeo
     * (habilidad especial de la Bomba).
     */
    fun breakTargetsWithin(cx: Double, cy: Double, radius: Double) {
        for (t in targets) {
            if (!t.alive || t.hit) continue
            val dx = t.cx - cx
            val dy = t.cy(globalTime) - cy
            if (kotlin.math.sqrt(dx * dx + dy * dy) <= radius) {
                markHit(t)
                onTargetHit?.invoke(t)
            }
        }
    }

    private fun markHit(t: Target) {
        t.hit = true
        t.alive = false
        t.respawnAt = globalTime + RESPAWN_DELAY
        t.view.imageView.isVisible = false
        t.view.setHitboxVisible(false)
    }

    // ── Creación de las dianas ───────────────────────────────────────

    private fun createFixedDiana() {
        val size = 150.0 * config.scale
        val x = config.windowWidth * 0.85
        val y = config.windowHeight * 0.52
        val fixed = Target(
            view = EntityView(spriteSource, "diana.png", size, CircularHitbox(size * 0.42), true),
            cx = x,
            baseY = y,
            amp = 0.0,
            speed = 0.0,
            radius = size * 0.42,
            size = size
        )
        fixed.view.placeAt(pane, x - size / 2, y - size / 2)
        targets += fixed

        // Barrote de hierro: poste desde la diana hasta el suelo
        val barrote = javafx.scene.shape.Rectangle(
            x - 6 * config.scale,
            y + size / 2,
            12 * config.scale,
            config.windowHeight - (y + size / 2)
        )
        barrote.fill = javafx.scene.paint.Color.DARKSLATEGRAY
        pane.children.add(barrote)
    }

    private fun createMobileDianas() {
        val specs = listOf(
            DianaSpec(config.windowWidth * 0.38, config.windowHeight * 0.35, 60.0, 0.9),
            DianaSpec(config.windowWidth * 0.50, config.windowHeight * 0.55, 80.0, 1.2),
            DianaSpec(config.windowWidth * 0.63, config.windowHeight * 0.30, 70.0, 1.6),
            DianaSpec(config.windowWidth * 0.72, config.windowHeight * 0.60, 55.0, 1.0),
            DianaSpec(config.windowWidth * 0.44, config.windowHeight * 0.72, 65.0, 1.3)
        )
        for (spec in specs) {
            val size = 90.0 * config.scale
            val t = Target(
                view = EntityView(spriteSource, "diana.png", size, CircularHitbox(size * 0.42), true),
                cx = spec.cx,
                baseY = spec.baseY,
                amp = spec.amp * config.scale,
                speed = spec.speed,
                radius = size * 0.42,
                size = size
            )
            t.view.placeAt(pane, spec.cx - size / 2, spec.baseY - size / 2)
            targets += t
        }
    }

    /** Definición de una diana móvil. */
    private data class DianaSpec(
        val cx: Double,
        val baseY: Double,
        val amp: Double,
        val speed: Double
    )

    private companion object {
        /** Segundos que la diana permanece fuera tras ser golpeada. */
        const val RESPAWN_DELAY = 2.0
    }
}
