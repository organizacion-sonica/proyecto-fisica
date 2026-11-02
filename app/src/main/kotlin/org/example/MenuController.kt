package org.example

import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration

/**
 * Controlador del menú principal e instrucciones.
 * Responsabilidad única: navegar entre menús y transicionar; la construcción
 * de los paneles vive en [MenuFactory] (SRP).
 */
class MenuController(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val onStartGame: () -> Unit,
    private val onStartBasketGame: () -> Unit
) : MenuNavigator {

    private val factory = MenuFactory(spriteSource, config, this)

    private val mainMenu = factory.mainMenu()
    private val instructionsMenu = factory.instructionsMenu()
    private val gamesMenu = factory.gamesMenu()
    private val root = StackPane(mainMenu)

    private var currentMenu: VBox = mainMenu

    val menuScene: Scene = Scene(root, config.windowWidth, config.windowHeight)

    fun rebuildScene(): Scene = Scene(root, config.windowWidth, config.windowHeight)

    override fun toMain() { crossFade(currentMenu, mainMenu) }
    override fun toInstructions() { crossFade(currentMenu, instructionsMenu) }
    override fun toGames() { crossFade(currentMenu, gamesMenu) }
    override fun startGame() { fadeOutAndStart(onStartGame) }
    override fun startBasket() { fadeOutAndStart(onStartBasketGame) }

    private fun crossFade(from: VBox, to: VBox) {
        fadeOut(from, 200.0) {
            root.children.setAll(to)
            currentMenu = to
            FadeTransition(Duration.millis(200.0), to).apply {
                fromValue = 0.0
                toValue = 1.0
            }.play()
        }
    }

    private fun fadeOutAndStart(onStart: () -> Unit) {
        fadeOut(currentMenu, 300.0) { onStart() }
    }

    private fun fadeOut(menu: VBox, millis: Double, onDone: () -> Unit) {
        FadeTransition(Duration.millis(millis), menu).apply {
            fromValue = 1.0
            toValue = 0.0
            onFinished = { onDone() }
        }.play()
    }
}
