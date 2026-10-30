package org.example

import javafx.scene.layout.Pane

/**
 * Objetivo/diana de práctica. Responsabilidad única: manejarse sola —
 * posición y movimiento sinusoidal, visibilidad y su ciclo de vida
 * (viva → golpeada → reaparición). El manager solo la consulta y la golpea.
 *
 * La creación es por fábricas ([fixed]/[mobile]); nada del exterior
 * manipula sus campos (encapsulación).
 */
class Target private constructor(
    private val view: EntityView,
    private val cx: Double,
    private val baseY: Double,
    private val amp: Double,
    private val speed: Double,
    val points: Int = 1
) {
    /** Hitbox de la diana, centrada donde se encuentra el sprite. */
    val hitbox: Hitbox get() = view.hitbox

    /** True si puede ser golpeada (está viva y no fue derribada hace nada). */
    val isHittable: Boolean get() = alive && !hit

    private var hit = false
    private var alive = true
    private var respawnAt = Double.POSITIVE_INFINITY

    /** Y de la diana en función del tiempo del mundo. */
    fun yAt(time: Double): Double = baseY + amp * kotlin.math.sin(speed * time)

    /** X fija de la diana. */
    val centerX: Double get() = cx

    /**
     * Avanza un tick: si está muerta comprueba su reaparición; si está viva
     * se mueve siguiendo su onda.
     */
    fun update(time: Double) {
        if (!alive) {
            if (time >= respawnAt) revive()
            return
        }
        val y = yAt(time)
        view.setPosition(cx - view.width / 2, y - view.height / 2)
    }

    /**
     * La golpean: queda fuera de juego y agenda su reaparición.
     * Devuelve false si no estaba en condiciones de ser golpeada.
     */
    fun strike(time: Double): Boolean {
        if (!isHittable) return false
        hit = true
        alive = false
        respawnAt = time + RESPAWN_DELAY
        view.setVisible(false)
        return true
    }

    private fun revive() {
        alive = true
        hit = false
        respawnAt = Double.POSITIVE_INFINITY
        view.setVisible(true)
    }

    /** Coloca la diana en el panel (primera vez). */
    fun placeIn(pane: Pane) {
        view.placeAt(pane, cx - view.width / 2, baseY - view.height / 2)
    }

    companion object {
        /** Proporción de los radios de la hitbox oval respecto del tamaño del sprite. */
        private const val RADIUS_X = 0.27
        private const val RADIUS_Y = 0.40

        /** Segundos que la diana permanece fuera tras ser golpeada. */
        private const val RESPAWN_DELAY = 2.0

        /** Tamaño (base) de la diana grande fija y de las móviles. */
        private const val FIXED_SIZE = 80.0
        private const val MOBILE_SIZE = 90.0

        /** Diana grande en una coordenada fija (columna de la diana). */
        fun fixed(
            spriteSource: SpriteSource,
            config: GameConfig,
            cx: Double,
            baseY: Double,
            amp: Double = 0.0,
            speed: Double = 0.0,
            points: Int = 3,
            hitboxOffsetX: Double = 0.0,
            assetName: String = "diana.png"
        ): Target {
            val size = FIXED_SIZE * config.scale
            return Target(
                EntityView(spriteSource, assetName, size, OvalHitbox(size * RADIUS_X, size * RADIUS_Y), true, hitboxOffsetX = hitboxOffsetX),
                cx = cx,
                baseY = baseY,
                amp = amp * config.scale,
                speed = speed,
                points = points
            )
        }

        /** Diana pequeña con movimiento sinusoidal (amp ya escalado). */
        fun mobile(
            spriteSource: SpriteSource,
            config: GameConfig,
            cx: Double,
            baseY: Double,
            amp: Double,
            speed: Double,
            points: Int = 1,
            hitboxOffsetX: Double = 0.0
        ): Target {
            val size = MOBILE_SIZE * config.scale
            return Target(
                EntityView(spriteSource, "diana.png", size, OvalHitbox(size * RADIUS_X, size * RADIUS_Y), true, hitboxOffsetX = hitboxOffsetX),
                cx = cx,
                baseY = baseY,
                amp = amp * config.scale,
                speed = speed,
                points = points
            )
        }
    }
}
