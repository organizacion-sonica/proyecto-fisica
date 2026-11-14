package org.example

import javafx.scene.layout.Pane

/**
 * Valores por defecto de las fábricas de [SlingshotGame]: concentran la
 * composición de concretos en un único lugar (composition root), de modo
 * que [SlingshotGame] depende de abstracciones y no de implementaciones.
 */
object SlingshotDefaults {

    fun physicsFactory(config: GameConfig): () -> PhysicsWorld = {
        val m = DefaultScreenMapper(config)
        Box2DWorld(config.gravity, m, DefaultBodyFactory(config, m), DefaultEntitySynchronizer(m), DefaultExplosionForce(m))
    }

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

    fun floorHitbox(config: GameConfig): BoxHitbox =
        BoxHitbox(halfWidth = config.windowWidth / 2.0, halfHeight = 5.0).apply {
            recenter(config.windowWidth / 2.0, config.windowHeight - 5.0)
        }

    /** Fábrica de menús con navegación diferida (permite inyectar el controlador después). */
    fun menuBuilder(spriteSource: SpriteSource, config: GameConfig, navigator: () -> MenuNavigator): MenuBuilder =
        MenuFactory(spriteSource, config, DeferredMenuNavigator { navigator() })

    fun gameController(
        spriteSource: SpriteSource,
        config: GameConfig,
        onExitToMenu: () -> Unit
    ): GameController = GameController(
        spriteSource,
        config,
        onExitToMenu = onExitToMenu,
        collaboratorFactory = gameCollaboratorFactory(spriteSource, config)
    )

    fun basketController(
        spriteSource: SpriteSource,
        config: GameConfig,
        onExitToMenu: () -> Unit
    ): BasketGameController = BasketGameController(
        spriteSource,
        config,
        onExitToMenu = onExitToMenu,
        collaboratorFactory = basketCollaboratorFactory(spriteSource, config)
    )

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
}

