package org.example

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File

/**
 * Raíz de composición.
 *
 * La aplicación se muestra siempre en pantalla completa real (ventana sin
 * decoraciones del tamaño de la pantalla): sin barra de tareas ni botones
 * de cerrar/minimizar. ESC vuelve al menú principal.
 */
class SlingshotApp : Application() {

    private val assetsDir = "/home/simonkrasner/proyecto-fisica/elementos graficos/"

    private lateinit var spriteSource: SpriteSource
    private lateinit var config: GameConfig
    private lateinit var stage: Stage
    private lateinit var menuController: MenuController

    override fun start(stage: Stage) {
        this.stage = stage
        spriteSource = FileSpriteSource(File(assetsDir))

        val bounds = javafx.stage.Screen.getPrimary().bounds
        config = GameConfig(windowWidth = bounds.width, windowHeight = bounds.height)

        // Pantalla completa real: sin barra de tareas ni decoraciones.
        stage.initStyle(StageStyle.UNDECORATED)
        stage.isAlwaysOnTop = true
        stage.isFullScreen = true
        stage.fullScreenExitHint = ""
        stage.fullScreenExitKeyCombination = javafx.scene.input.KeyCombination.NO_MATCH

        showMenu()
    }

    /** Muestra (o re-muestra) el menú principal con una instancia fresca. */
    private fun showMenu() {
        menuController = MenuController(
            spriteSource,
            config,
            onStartGame = { startGame() },
            onStartBasketGame = { startBasket() }
        )
        stage.scene = menuController.menuScene
        stage.title = "Minijuegos de Fisica"
        stage.width = config.windowWidth
        stage.height = config.windowHeight
        stage.x = 0.0
        stage.y = 0.0
        stage.show()
        stage.toFront()
        stage.requestFocus()
    }

    private fun startGame() {
        val controller = GameController(spriteSource, config, onExitToMenu = {
            Platform.runLater { showMenu() }
        })
        stage.scene = controller.buildScene()
        stage.title = "Minijuegos de Fisica - Juego"
        stage.isFullScreen = true
        stage.show()
        stage.toFront()
        stage.requestFocus()
    }

    private fun startBasket() {
        val controller = BasketGameController(spriteSource, config, onExitToMenu = {
            Platform.runLater { showMenu() }
        })
        stage.scene = controller.buildScene()
        stage.title = "Minijuegos de Fisica - Tiro al Aro"
        stage.isFullScreen = true
        stage.show()
        stage.toFront()
        stage.requestFocus()
    }
}
