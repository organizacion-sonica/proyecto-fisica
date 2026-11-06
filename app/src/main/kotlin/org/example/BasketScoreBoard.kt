package org.example

import javafx.scene.control.Label
import javafx.scene.image.ImageView

/**
 * Marcador (puntaje + rango) del minijuego de basket (SRP).
 */
class BasketScoreBoard(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    val scoreLabel: Label,
    val rankView: ImageView
) {
    var score: Int = 0
        private set

    fun addPoint() {
        score += 1
        scoreLabel.text = "Puntos: $score"
        refreshRank()
    }

    fun reset() {
        score = 0
        scoreLabel.text = "Puntos: 0"
        refreshRank()
    }

    fun refreshRank() {
        rankView.image = spriteSource.load("${RankEvaluator.rankForBasket(score)} rank.png")
    }
}
