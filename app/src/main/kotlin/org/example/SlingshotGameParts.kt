package org.example

import javafx.scene.layout.Pane

class SlingshotGameParts(
    val spriteSource: SpriteSource,
    val config: GameConfig,
    val physicsFactory: () -> PhysicsWorld,
    val levelFactory: (Pane, PhysicsWorld) -> SlingshotLevelComponents
)