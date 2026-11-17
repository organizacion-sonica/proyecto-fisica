package org.example

import javafx.scene.layout.Pane

object TargetGameDefaults {
    fun gameCollaboratorFactory(spriteSource: SpriteSource, config: GameConfig): (Pane, PhysicsWorld) -> GameCollaborators =
        { pane, _ ->
            val trajectoryLayer = Pane().also { pane.children.add(it) }
            val trajectoryCalculator = TrajectoryCalculator(config)
            val aimAssist = AimAssist(
                trajectoryLayer,
                TrajectoryRenderer(trajectoryLayer, trajectoryCalculator),
                4.5 * config.scale,
                0.85,
                0.6,
                1.0
            )
            GameCollaborators(
                angleIndicator = AngleIndicator(config),
                aimAssist = aimAssist,
                explosionPlayer = ExplosionPlayer(pane, spriteSource, config),
                scoreBoard = ScoreBoard(config),
                dianaManager = DianaManager(spriteSource, config, pane),
                rankScreen = RankScreen(spriteSource, config)
            )
        }

    fun gameController(
        parts: SlingshotGameParts,
        onExitToMenu: () -> Unit
    ): GameController = GameController(
        parts,
        onExitToMenu = onExitToMenu,
        collaboratorFactory = gameCollaboratorFactory(parts.spriteSource, parts.config)
    )
}
