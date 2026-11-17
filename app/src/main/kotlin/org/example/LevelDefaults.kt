package org.example

import javafx.scene.layout.Pane

object LevelDefaults {
    fun levelFactory(spriteSource: SpriteSource, config: GameConfig): (Pane, PhysicsWorld) -> SlingshotLevelComponents =
        { pane, _ ->
            val bg = BackgroundView(spriteSource, config)
            val sl = SlingshotView(spriteSource, config)
            sl.placeIn(pane)
            val lz = LaunchZone(config, sl.anchor.x, sl.anchor.y)
            val ef = EntityFactory(spriteSource, config)
            val pl = BirdPile(pane, ef, config)
            pl.setup()
            bg.placeIn(pane)
            SlingshotLevelComponents(bg, sl, lz, pl, ef)
        }

    fun floorHitbox(config: GameConfig): BoxHitbox =
        BoxHitbox(halfWidth = config.windowWidth / 2.0, halfHeight = 5.0).apply {
            recenter(config.windowWidth / 2.0, config.windowHeight - 5.0)
        }
}
