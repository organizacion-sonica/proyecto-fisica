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
 * Abstracción de la construcción de los paneles del menú (DIP): el
 * [MenuController] depende de esta interfaz, no de [MenuFactory].
 */
interface MenuBuilder {
    fun mainMenu(): VBox
    fun instructionsMenu(): VBox
    fun gamesMenu(): VBox
}

/**
 * Construye los paneles del menú. Separado del [MenuController] (SRP): el
 * controlador navega y transiciona; la fábrica solo arma los nodos.
 */
class MenuFactory(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val navigator: MenuNavigator
) : MenuBuilder {
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

    /** Etiqueta de título estilizada. */
    private fun titleLabel(text: String, size: Double): Label = Label(text).apply {
        font = Font.font("Arial", FontWeight.BOLD, size * s)
        textFill = javafx.scene.paint.Color.WHITE
        isWrapText = true
        maxWidth = config.windowWidth * 0.9
        alignment = Pos.CENTER
    }

    /** Tarjeta de un minijuego (nombre + botón JUGAR). */
    private fun minigameCard(name: String, onPlay: () -> Unit): VBox = VBox(
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

    override fun mainMenu(): VBox {
        val title = titleLabel("MINIJUEGOS DE FÍSICA", 72.0).apply { style = MenuStyles.titleShadow }
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

    override fun instructionsMenu(): VBox {
        val title = titleLabel("INSTRUCCIONES", 48.0)
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

    override fun gamesMenu(): VBox {
        val title = titleLabel("SELECCIONÁ UN MINIJUEGO", 48.0)
        val minigames = listOf(
            Pair("Acertar en las dianas", { navigator.startGame() }),
            Pair("Tiro al aro", { navigator.startBasket() })
        )
        val row = HBox(
            30.0 * s,
            *minigames.map { (name, onPlay) -> minigameCard(name, onPlay) }.toTypedArray()
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

/**
 * Navegador que resuelve el destino recién cuando se usa: permite construir
 * los paneles del menú antes de que exista el [MenuController].
 */
class DeferredMenuNavigator(private val target: () -> MenuNavigator) : MenuNavigator {
    override fun toMain() { target().toMain() }
    override fun toInstructions() { target().toInstructions() }
    override fun toGames() { target().toGames() }
    override fun startGame() { target().startGame() }
    override fun startBasket() { target().startBasket() }
}
