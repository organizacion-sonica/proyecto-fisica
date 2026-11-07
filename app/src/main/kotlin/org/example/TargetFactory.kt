package org.example

import javafx.scene.layout.Pane

/**
 * Fábrica de dianas: concentra la creación de [EntityView]/[OvalHitbox]
 * fuera de [DianaManager], para que este dependa de abstracciones (DIP).
 */
object TargetFactory {

    fun createFixedView(spriteSource: SpriteSource, config: GameConfig, size: Double): EntityView =
        EntityView(spriteSource, "diana potente.png", size,
            OvalHitbox(size * Target.RADIUS_X, size * Target.RADIUS_Y), false,
            hitboxOffsetX = -6.0 * config.scale)

    fun createMobileView(spriteSource: SpriteSource, config: GameConfig, size: Double): EntityView =
        EntityView(spriteSource, "diana.png", size,
            OvalHitbox(size * Target.RADIUS_X, size * Target.RADIUS_Y), false,
            hitboxOffsetX = -6.0 * config.scale)
}
