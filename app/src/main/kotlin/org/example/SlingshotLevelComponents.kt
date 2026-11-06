package org.example

/**
 * Colaboradores de un nivel con resortera, creados por una fábrica (DIP).
 */
data class SlingshotLevelComponents(
    val backgroundView: BackgroundView,
    val slingshot: SlingshotView,
    val launchZone: LaunchZone,
    val pile: BirdPile,
    val entityFactory: EntityFactory
)
