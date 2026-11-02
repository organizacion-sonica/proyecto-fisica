package org.example

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration

/**
 * Construye los paneles del menú. Separado del [MenuController] (SRP): el
 * controlador navega y transiciona; la fábrica solo arma los nodos.
 */
class MenuFactory(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val navigator: MenuNavigator
) {
    private val s = config.scale

    private fun button(text: String, onClick: () -> Unit): Button = Button(text).apply {
        font = Font.font("Arial", FontWeight.BOLD, 28.0 * s)
        textFill = javafx.scene.paint.Color.WHITE
        prefWidth = 300.0 * s
        prefHeight = 60.0 * s
        minWidth = 300.0 * s
        style = MenuStyles.buttonNormal
        setOnMouseEntered { style = MenuStyles.buttonHover }
        setOnMouseExited { style = MenuStyles.buttonNormal }
        setOnAction { onClick() }
    }

    fun mainMenu(): VBox {
        val title = Label("MINIJUEGOS DE FÍSICA").apply {
            font = Font.font("Arial", FontWeight.BOLD, 72.0 * s)
            textFill = javafx.scene.paint.Color.WHITE
            style = MenuStyles.titleShadow
            isWrapText = true
            maxWidth = config.windowWidth * 0.9
            alignment = Pos.CENTER
        }
        return VBox(30.0 * s, title,
            button("JUGAR") { navigator.toGames() },
            button("INSTRUCCIONES") { navigator.toInstructions() },
            button("SALIR") { javafx.application.Platform.exit() }
        ).apply {
            alignment = Pos.CENTER
            padding = Insets(50.0 * s)
            style = MenuStyles.menuBackground
        }
    }

    fun instructionsMenu(): VBox {
        val title = Label("INSTRUCCIONES").apply {
            font = Font.font("Arial", FontWeight.BOLD, 48.0 * s)
            textFill = javafx.scene.paint.Color.WHITE
            isWrapText = true
            maxWidth = config.windowWidth * 0.9
            alignment = Pos.CENTER
        }
        val instructionsText = Label(InstructionsContent.text).apply {
            font = Font.font("Arial", 20.0 * s)
            textFill = javafx.scene.paint.Color.WHITE
            isWrapText = true
            maxWidth = config.windowWidth * 0.75
            alignment = Pos.CENTER_LEFT
        }
        val scroll = ScrollPane(instructionsText).apply {
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            isFitToWidth = true
            isPannable = true
            style = "-fx-background: transparent; -fx-background-color: transparent;"
            maxHeight = config.windowHeight * 0.5
        }
        return VBox(20.0 * s, title, scroll, button("VOLVER") { navigator.toMain() }).apply {
            alignment = Pos.CENTER
            padding = Insets(30.0 * s)
            style = MenuStyles.menuBackground
        }
    }

    fun gamesMenu(): VBox {
        val title = Label("SELECCIONÁ UN MINIJUEGO").apply {
            font = Font.font("Arial", FontWeight.BOLD, 48.0 * s)
            textFill = javafx.scene.paint.Color.WHITE
            isWrapText = true
            maxWidth = config.windowWidth * 0.9
            alignment = Pos.CENTER
        }
        val minigames = listOf(
            Pair("Acertar en las dianas", { navigator.startGame() }),
            Pair("Tiro al aro", { navigator.startBasket() })
        )
        val row = HBox(
            30.0 * s,
            *minigames.map { (name, onPlay) ->
                VBox(
                    10.0 * s,
                    Label(name).apply {
                        font = Font.font("Arial", FontWeight.BOLD, 22.0 * s)
                        textFill = javafx.scene.paint.Color.WHITE
                        maxWidth = 300.0 * s
                        prefHeight = 60.0 * s
                        isWrapText = true
                        alignment = Pos.CENTER
                    },
                    button("JUGAR") { onPlay() }.apply {
                        prefWidth = 180.0 * s
                        prefHeight = 90.0 * s
                    }
                ).apply { alignment = Pos.CENTER }
            }.toTypedArray()
        ).apply { alignment = Pos.CENTER }

        return VBox(40.0 * s, title, row, button("VOLVER") { navigator.toMain() }).apply {
            alignment = Pos.CENTER
            padding = Insets(50.0 * s)
            style = MenuStyles.menuBackground
        }
    }
}

/** Navegación entre menús y arranque de juegos (implementada por MenuController). */
interface MenuNavigator {
    fun toMain()
    fun toInstructions()
    fun toGames()
    fun startGame()
    fun startBasket()
}
