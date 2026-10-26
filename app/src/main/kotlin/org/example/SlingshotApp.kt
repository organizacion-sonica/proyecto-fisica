package org.example

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import java.io.File

/**
 * Raíz de composición: el ÚNICO lugar donde se conocen las implementaciones
 * concretas (FileSpriteSource, GameController, MenuController, tamaños de ventana).
 * Todo lo demás depende de abstracciones. Responsabilidad única: cablear y lanzar.
 */
class SlingshotApp : Application() {

    // Ruta absoluta donde están todos los gráficos.
    private val assetsDir = "/home/simonkrasner/proyecto-fisica/elementos graficos/"

    private lateinit var spriteSource: SpriteSource
    private lateinit var config: GameConfig
    private lateinit var stage: Stage
    private lateinit var menuController: MenuController

    override fun start(stage: Stage) {
        this.stage = stage
        spriteSource = FileSpriteSource(File(assetsDir))
        // Usar el área visible de la pantalla (sin la barra del sistema) para
        // que el contenido se adapte siempre, en 1920x1080 o en cualquier otra.
        val bounds = javafx.stage.Screen.getPrimary().visualBounds
        config = GameConfig(windowWidth = bounds.width, windowHeight = bounds.height)

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
        stage.title = "Angry Birds Clone"
        stage.isResizable = false
        stage.width = config.windowWidth
        stage.height = config.windowHeight
        stage.x = 0.0
        stage.y = 0.0
        stage.show()
    }

    private fun startGame() {
        val controller = GameController(spriteSource, config, onExitToMenu = {
            Platform.runLater { showMenu() }
        })
        stage.scene = controller.buildScene()
        stage.title = "Angry Birds Clone - Juego"
    }

    private fun startBasket() {
        val controller = BasketGameController(spriteSource, config, onExitToMenu = {
            Platform.runLater { showMenu() }
        })
        stage.scene = controller.buildScene()
        stage.title = "Angry Birds Clone - Tiro al Aro"
    }
}
