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
    private val config: GameConfig,
    private val onStartGame: () -> Unit,
    private val onStartBasketGame: () -> Unit,
    private val menuBuilder: MenuBuilder
) : MenuNavigator {

    private val mainMenu = menuBuilder.mainMenu()
    private val instructionsMenu = menuBuilder.instructionsMenu()
    private val gamesMenu = menuBuilder.gamesMenu()
    private val root = StackPane(mainMenu)

    private var currentMenu: VBox = mainMenu

    fun menuScene(): Scene = Scene(root, config.windowWidth, config.windowHeight)

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
