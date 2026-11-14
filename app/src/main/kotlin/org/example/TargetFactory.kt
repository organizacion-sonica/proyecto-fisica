package org.example

import javafx.scene.layout.Pane

/**
 * Fábrica de dianas: concentra la creación de [EntityView], [OvalHitbox] y
 * [Target] fuera de [DianaManager], para que este dependa de abstracciones
 * (DIP) y solo se ocupe de la escena y los impactos.
 */
/** Especificación de una diana móvil (reduce parámetros sueltos). */
data class TargetSpec(
    val cx: Double,
    val baseY: Double,
    val amp: Double,
    val speed: Double
)

object TargetFactory {

    fun createFixedView(spriteSource: SpriteSource, config: GameConfig, size: Double): EntityView =
        EntityView(spriteSource, "diana potente.png", size,
            OvalHitbox(size * Target.RADIUS_X, size * Target.RADIUS_Y), false,
            hitboxOffsetX = -6.0 * config.scale)

    fun createMobileView(spriteSource: SpriteSource, config: GameConfig, size: Double): EntityView =
        EntityView(spriteSource, "diana.png", size,
            OvalHitbox(size * Target.RADIUS_X, size * Target.RADIUS_Y), false,
            hitboxOffsetX = -6.0 * config.scale)

    /** Diana grande fija, ya colocada en el panel. */
    fun createFixed(spriteSource: SpriteSource, config: GameConfig, pane: Pane): Target {
        val size = Target.FIXED_SIZE * config.scale
        val view = createFixedView(spriteSource, config, size)
        val fixed = Target.fixed(
            view = view,
            cx = config.windowWidth * 0.90,
            baseY = config.windowHeight * 0.52,
            amp = 40.0 * config.scale,
            speed = 1.5,
            points = 3
        )
        fixed.placeIn(pane)
        return fixed
    }

    /** Diana móvil con movimiento sinusoidal, ya colocada en el panel. */
    fun createMobile(
        spriteSource: SpriteSource,
        config: GameConfig,
        pane: Pane,
        spec: TargetSpec
    ): Target {
        val size = Target.MOBILE_SIZE * config.scale
        val view = createMobileView(spriteSource, config, size)
        val target = Target.mobile(
            view = view,
            cx = spec.cx,
            baseY = spec.baseY,
            amp = spec.amp * config.scale,
            speed = spec.speed
        )
        target.placeIn(pane)
        return target
    }
}
