package org.example

import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane

/**
 * HUD del minijuego de basket: puntaje, rango e icono de tiempo.
 * Responsabilidad única: crear y posicionar los nodos del HUD (SRP).
 */
class BasketHud(private val spriteSource: SpriteSource, private val config: GameConfig) {
    val scoreLabel: Label = Label("Puntos: 0").apply {
        style = "-fx-font-size: ${20 * config.scale}px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
        textFill = javafx.scene.paint.Color.WHITE
        layoutX = config.windowWidth / 2 - 50.0 * config.scale
        layoutY = 40.0 * config.scale
    }

    val rankView: ImageView = ImageView(spriteSource.load("${RankEvaluator.rankForBasket(0)} rank.png")).apply {
        isPreserveRatio = true
        fitWidth = 40.0 * config.scale
        fitHeight = 40.0 * config.scale
        layoutX = config.windowWidth / 2 - 110.0 * config.scale
        layoutY = 34.0 * config.scale
    }

    val timerLabel: Label = Label("Tiempo: 01:00").apply {
        style = "-fx-font-size: ${16 * config.scale}px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
        textFill = javafx.scene.paint.Color.WHITE
        layoutX = config.windowWidth / 2 - 110.0 * config.scale
        layoutY = 78.0 * config.scale
    }

    fun addTo(pane: Pane) {
        pane.children.addAll(scoreLabel, rankView, timerLabel)
    }
}
