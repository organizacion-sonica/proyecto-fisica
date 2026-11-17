package org.example

import javafx.scene.layout.Pane

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
        parts: SlingshotGameParts,
        onExitToMenu: () -> Unit
    ): BasketGameController = BasketGameController(
        parts,
        onExitToMenu = onExitToMenu,
        collaboratorFactory = basketCollaboratorFactory(parts.spriteSource, parts.config)
    )

    fun createPole(pane: Pane, physics: PhysicsWorld, hoop: BasketHoop, config: GameConfig) {
        val poleWidth = 22.0 * config.scale
        val poleHeight = config.windowHeight
        val poleX = config.windowWidth - poleWidth
        val poleCenterX = poleX + poleWidth / 2.0

        val poleRect = javafx.scene.shape.Rectangle(poleX, 0.0, poleWidth, poleHeight).apply {
            fill = javafx.scene.paint.Color.web("#5c3a21")
            stroke = javafx.scene.paint.Color.web("#2b1d0c")
            strokeWidth = 1.0
        }
        pane.children.add(poleRect)
        hoop.view.toFront()

        physics.addStaticBox(BoxSpec(poleCenterX, poleHeight / 2.0, poleWidth, poleHeight))
    }
}
