package org.example

object MenuDefaults {
    fun menuBuilder(spriteSource: SpriteSource, config: GameConfig, navigator: () -> MenuNavigator): MenuBuilder =
        MenuFactory(spriteSource, config, DeferredMenuNavigator { navigator() })
}
