package org.example

import javafx.scene.layout.Pane

/**
 * Gestiona el conjunto de dianas: decide dónde se crean, avanza su
 * movimiento/lifecycle a través de [Target.update] (cada diana se maneja
 * sola) y detecta los impactos de los pájaros en vuelo.
 * Responsabilidad única: la escena de dianas y su detección de impactos;
 * no sabe puntajes ni entrada del jugador (los notifica vía [onTargetHit]).
 */
class DianaManager(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val pane: Pane
) : TargetScene {
    private val targets = mutableListOf<Target>()

    /** Segundos que lleva el mundo (para movimientos y reapariciones). */
    private var globalTime = 0.0

    /** Notifica cada vez que una diana pasa a "golpeada", con los puntos que vale. */
    override var onTargetHit: ((Int) -> Unit)? = null

    /** Crea las dianas del nivel. */
    override fun setup() {
        targets.clear()
        createFixedDiana()
        createMobileDianas()
    }

    /** Avanza el mundo (posiciones + reapariciones) y comprueba impactos. */
    override fun update(dt: Double, inFlight: List<Bird>) {
        globalTime += dt
        for (target in targets) target.update(globalTime)
        checkBirdCollisions(inFlight)
    }

    // ── Impactos ──────────────────────────────────────────────────────

    /** Comprueba si algún pájaro en vuelo toca alguna diana viva. */
    private fun checkBirdCollisions(birds: List<Bird>) {
        for (bird in birds) {
            for (target in targets) {
                if (!target.isHittable) continue
                if (CollisionDetector.collides(bird.hitbox, target.hitbox)) {
                    // Target.strike devuelve false si ya no cuenta (por la
                    // primera diana alcanzada por este mismo pájaro).
                    if (target.strike(globalTime)) onTargetHit?.invoke(target.pointValue())
                }
            }
        }
    }

    /**
     * Derriba todas las dianas dentro del radio y notifica el golpeo
     * (onda expansiva de la Bomba).
     */
    override fun breakTargetsWithin(cx: Double, cy: Double, radius: Double) {
        for (target in targets) {
            if (!target.isHittable) continue
            val dx = target.centerX - cx
            val dy = target.yAt(globalTime) - cy
            if (kotlin.math.sqrt(dx * dx + dy * dy) <= radius) {
                if (target.strike(globalTime)) onTargetHit?.invoke(target.pointValue())
            }
        }
    }

    // ── Creación de las dianas ───────────────────────────────────────

    private fun createFixedDiana() {
        targets += TargetFactory.createFixed(spriteSource, config, pane)
    }

    private fun createMobileDianas() {
        val specs = listOf(
            Triple(config.windowWidth * 0.38, config.windowHeight * 0.35, 60.0),
            Triple(config.windowWidth * 0.50, config.windowHeight * 0.55, 80.0),
            Triple(config.windowWidth * 0.63, config.windowHeight * 0.30, 70.0),
            Triple(config.windowWidth * 0.72, config.windowHeight * 0.60, 55.0),
            Triple(config.windowWidth * 0.44, config.windowHeight * 0.72, 65.0)
        )
        val speeds = listOf(0.9, 1.2, 1.6, 1.0, 1.3)
        for ((i, spec) in specs.withIndex()) {
            val (cx, baseY, amp) = spec
            targets += TargetFactory.createMobile(
                spriteSource, config, pane,
                TargetSpec(cx, baseY, amp, speeds[i])
            )
        }
    }
}
