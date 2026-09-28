package org.example

import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration

/**
 * Controlador del menú principal e instrucciones.
 * Responsabilidad única: manejar la navegación entre menús antes de iniciar el juego.
 */
class MenuController(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val onStartGame: () -> Unit
) {

    private val mainMenu = createMainMenu()
    private val instructionsMenu = createInstructionsMenu()
    private val root = StackPane(mainMenu)

    /** Escena del menú. */
    val menuScene: Scene = Scene(root, config.windowWidth, config.windowHeight)

    /** Crea el menú principal. */
    private fun createMainMenu(): VBox {
        val title = Label("ANGRY BIRDS CLONE").apply {
            font = Font.font("Arial", FontWeight.BOLD, 72.0)
            textFill = Color.WHITE
            style = "-fx-effect: dropshadow(gaussian, black, 10, 0.5, 2, 2);"
        }

        val playButton = createMenuButton("JUGAR") {
            // Transición de fade out del menú y iniciar juego
            fadeOutAndStart()
        }

        val instructionsButton = createMenuButton("INSTRUCCIONES") {
            switchToInstructions()
        }

        val exitButton = createMenuButton("SALIR") {
            Platform.exit()
        }

        return VBox(30.0, title, playButton, instructionsButton, exitButton).apply {
            alignment = Pos.CENTER
            padding = Insets(50.0)
            style = "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #2c3e50, #1a1a2e);"
        }
    }

    /** Crea el menú de instrucciones. */
    private fun createInstructionsMenu(): VBox {
        val title = Label("INSTRUCCIONES").apply {
            font = Font.font("Arial", FontWeight.BOLD, 48.0)
            textFill = Color.WHITE
        }

        val instructionsText = Label("""
            CÓMO JUGAR:

            1. SELECCIÓN DE PÁJAROS:
               - En la esquina superior IZQUIERDA hay 3 pájaros apilados:
                 ROJO, AMARILLO y NEGRO
               - Los pájaros son INFINITOS: siempre podés usar uno nuevo
               - Agarrá un pájaro y arrastralo: se crea una COPIA visual
                 que sigue al mouse (el original queda en la pila)
               - Soltala sobre la resortera para cargar un pájaro nuevo
               - Si la soltás a mitad de camino, la copia desaparece
                 y el suministro queda intacto

            2. LANZAR:
               - Con el pájaro fijado, estirá la resortera hacia atrás
               - Cuanto más la estires, más lejos y rápido volará
               - Soltá el clic para lanzar

            3. HABILIDADES ESPECIALES (Tecla G, en pleno vuelo):
               - RED (Rojo): Sin habilidad especial
               - CHUCK (Amarillo): Presioná G en vuelo para BOOST de velocidad
               - BOMB (Negro): Presioná G en vuelo para EXPLOSIÓN que derriba bloques

            4. REINICIAR NIVEL (Tecla R):
               - Presioná R en cualquier momento para reiniciar todo de cero

            5. OBJETIVO:
               - Derribá la casita de bloques con los pájaros
            
        """.trimIndent()).apply {
            font = Font.font("Arial", 20.0)
            textFill = Color.WHITE
            isWrapText = true
            maxWidth = config.windowWidth * 0.75
            alignment = Pos.CENTER_LEFT
        }

        // El texto va dentro de un ScrollPane para que nunca se recorte aunque
        // la ventana sea chica: así se ven todas las descripciones, incl. Bomb.
        val scroll = ScrollPane(instructionsText).apply {
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            isFitToWidth = true
            isPannable = true
            style = "-fx-background: transparent; -fx-background-color: transparent;"
            maxHeight = config.windowHeight * 0.5
        }

        val backButton = createMenuButton("VOLVER") {
            switchToMain()
        }

        return VBox(20.0, title, scroll, backButton).apply {
            alignment = Pos.CENTER
            padding = Insets(30.0)
            style = "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #2c3e50, #1a1a2e);"
        }
    }

    /** Crea un botón estilizado del menú. */
    private fun createMenuButton(text: String, onClick: () -> Unit): Button {
        return Button(text).apply {
            font = Font.font("Arial", FontWeight.BOLD, 28.0)
            textFill = Color.WHITE
            prefWidth = 300.0
            prefHeight = 60.0
            style = """
                -fx-background-color: linear-gradient(#e74c3c, #c0392b);
                -fx-background-radius: 10;
                -fx-border-color: #ffffff;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                -fx-cursor: hand;
            """
            setOnMouseEntered { style = """
                -fx-background-color: linear-gradient(#ff6b5b, #e74c3c);
                -fx-background-radius: 10;
                -fx-border-color: #fff;
                -fx-border-width: 3;
                -fx-border-radius: 10;
                -fx-cursor: hand;
            """ }
            setOnMouseExited { style = """
                -fx-background-color: linear-gradient(#e74c3c, #c0392b);
                -fx-background-radius: 10;
                -fx-border-color: #fff;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                -fx-cursor: hand;
            """ }
            setOnAction { onClick() }
        }
    }

    /** Cambia al menú de instrucciones con animación. */
    private fun switchToInstructions() {
        crossFade(mainMenu, instructionsMenu)
    }

    /** Vuelve al menú principal con animación. */
    private fun switchToMain() {
        crossFade(instructionsMenu, mainMenu)
    }

    /** Transición cruzada entre dos paneles. */
    private fun crossFade(from: VBox, to: VBox) {
        val fadeOut = FadeTransition(Duration.millis(200.0), from).apply {
            fromValue = 1.0
            toValue = 0.0
            onFinished = { _ ->
                root.children.setAll(to)
                val fadeIn = FadeTransition(Duration.millis(200.0), to).apply {
                    fromValue = 0.0
                    toValue = 1.0
                }
                fadeIn.play()
            }
        }
        fadeOut.play()
    }

    /** Desvanece el menú y lanza el juego. */
    private fun fadeOutAndStart() {
        val fadeOut = FadeTransition(Duration.millis(300.0), mainMenu).apply {
            fromValue = 1.0
            toValue = 0.0
            onFinished = { _ -> onStartGame() }
        }
        fadeOut.play()
    }
}