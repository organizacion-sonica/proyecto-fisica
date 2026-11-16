package org.example

import javafx.scene.layout.Pane

/**
 * Defaults del minijuego de basket (HUD + colaboradores + controlador).
 */
object BasketDefaults {
    fun basketCollaboratorFactory(spriteSource: SpriteSource, config: GameConfig): (Pane, PhysicsWorld) -> BasketGameCollaborators =
        { pane, physics ->
            val hud = BasketHud(spriteSource, config)
            hud.addTo(pane)
            BackgroundView(spriteSource, config).placeIn(pane)
            val trajectoryLayer = Pane().also { pane.children.add(it) }
            val trajectoryCalculator = TrajectoryCalculator(config)
            val aimAssist = AimAssist(
                trajectoryLayer,
                TrajectoryRenderer(trajectoryLayer, trajectoryCalculator),
                3.5 * config.scale,
                0.22,
                0.12,
                0.5
            )
            val scoreBoard = BasketScoreBoard(spriteSource, config, hud.scoreLabel, hud.rankView)
            val timer = BasketTimer(config, hud.timerLabel, BasketGameController.BASKET_TIME_LIMIT)
            BasketGameCollaborators(
                aimAssist = aimAssist,
                scoreLabel = hud.scoreLabel,
                rankView = hud.rankView,
                timerLabel = hud.timerLabel,
                hoop = Hoop(spriteSource, config, pane, physics),
                rankScreen = RankScreen(spriteSource, config),
                scoreBoard = scoreBoard,
                timer = timer
            )
        }

    fun basketController(
        spriteSource: SpriteSource,
        config: GameConfig,
        onExitToMenu: () -> Unit
    ): BasketGameController = BasketGameController(
        spriteSource,
        config,
        onExitToMenu = onExitToMenu,
        physicsFactory = LevelDefaults.physicsFactory(config),
        levelFactory = LevelDefaults.levelFactory(spriteSource, config),
        collaboratorFactory = basketCollaboratorFactory(spriteSource, config)
    )
}
