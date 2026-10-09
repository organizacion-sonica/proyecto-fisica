package org.example

import javafx.application.Application
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

    override fun start(stage: Stage) {
        val spriteSource: SpriteSource = FileSpriteSource(File(assetsDir))
        // Usar el área visible de la pantalla (sin la barra del sistema) para
        // que el contenido se adapte siempre, en 1920x1080 o en cualquier otra.
        val bounds = javafx.stage.Screen.getPrimary().visualBounds
        val config = GameConfig(windowWidth = bounds.width, windowHeight = bounds.height)

        // Primero mostramos el menú
        val menuController = MenuController(spriteSource, config) {
            // Callback cuando se presiona JUGAR: crear y mostrar el juego
            val controller = GameController(spriteSource, config)
            stage.scene = controller.buildScene()
            stage.title = "Angry Birds Clone - Juego"
        }

        stage.scene = menuController.menuScene
        stage.title = "Angry Birds Clone"
        stage.isResizable = false
        stage.width = config.windowWidth
        stage.height = config.windowHeight
        stage.x = 0.0
        stage.y = 0.0
        stage.show()
    }
}