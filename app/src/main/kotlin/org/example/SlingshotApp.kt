package org.example

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import java.io.File

class SlingshotApp : Application() {

    private val assetsDir = "D:\\downloads\\proyecto-fisica\\elementos graficos"

    private lateinit var spriteSource: SpriteSource
    private lateinit var config: GameConfig
    private lateinit var stage: Stage
    private lateinit var menuController: MenuController

    override fun start(stage: Stage) {
        this.stage = stage
        spriteSource = FileSpriteSource(File(assetsDir))

        val bounds = javafx.stage.Screen.getPrimary().bounds
        config = GameConfig(windowWidth = bounds.width, windowHeight = bounds.height)

        stage.initStyle(StageStyle.UNDECORATED)
        stage.isAlwaysOnTop = true
        stage.isFullScreen = true
        stage.fullScreenExitHint = ""
        stage.fullScreenExitKeyCombination = javafx.scene.input.KeyCombination.NO_MATCH

        showMenu()
    }

    private fun showMenu() {
        val menuBuilder = MenuDefaults.menuBuilder(spriteSource, config) { menuController }
        menuController = MenuController(
            config,
            onStartGame = { startGame() },
            onStartBasketGame = { startBasket() },
            menuBuilder = menuBuilder
        )
        stage.scene = menuController.menuScene()
        stage.title = "Minijuegos de Fisica"
        stage.width = config.windowWidth
        stage.height = config.windowHeight
        stage.x = 0.0
        stage.y = 0.0
        stage.show()
        stage.toFront()
        stage.requestFocus()
    }

    private fun showScene(scene: javafx.scene.Scene, title: String) {
        stage.scene = scene
        stage.title = title
        stage.isFullScreen = true
        stage.show()
        stage.toFront()
        stage.requestFocus()
    }

    private fun startGame() {
        val parts = SlingshotGameParts(spriteSource, config, PhysicsDefaults.physicsFactory(config), LevelDefaults.levelFactory(spriteSource, config))
        val controller = TargetGameDefaults.gameController(
            parts,
            onExitToMenu = { Platform.runLater { showMenu() } }
        )
        showScene(controller.buildScene(), "Minijuegos de Fisica - Juego")
    }

    private fun startBasket() {
        val parts = SlingshotGameParts(spriteSource, config, PhysicsDefaults.physicsFactory(config), LevelDefaults.levelFactory(spriteSource, config))
        val controller = BasketDefaults.basketController(
            parts,
            onExitToMenu = { Platform.runLater { showMenu() } }
        )
        showScene(controller.buildScene(), "Minijuegos de Fisica - Tiro al Aro")
    }
}
