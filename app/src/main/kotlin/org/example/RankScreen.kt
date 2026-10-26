package org.example

import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane

/**
 * Pantalla final del nivel: fondo, rango obtenido, puntaje y la fila de
 * pájaros. Responsabilidad única: dibujar la pantalla de resultado.
 */
class RankScreen(
    private val spriteSource: SpriteSource,
    private val config: GameConfig
) {
    /** Sprites de la fila inferior, en el orden del nivel. */
    private val birdSpriteFiles = listOf("Red.png", "chuck .png", "bomb.png")

    /** Dibuja la pantalla final sobre [pane] con el [score] obtenido. */
    fun show(pane: Pane, score: Int) {
        pane.children.clear()
        BackgroundView(spriteSource, config).placeIn(pane)

        val s = config.scale
        val rankSize = 200.0 * s

        pane.children.add(ImageView(spriteSource.load("${RankEvaluator.rankFor(score)} rank.png")).apply {
            isPreserveRatio = true
            fitHeight = rankSize
            layoutX = config.windowWidth / 2 - rankSize / 2
            layoutY = config.windowHeight * 0.12
        })

        pane.children.add(Label("Rango obtenido").apply {
            style = "-fx-font-size: ${28 * s}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
            layoutY = config.windowHeight * 0.10
            layoutX = config.windowWidth / 2 - 100 * s
        })

        pane.children.add(Label("Puntaje: $score").apply {
            style = "-fx-font-size: ${32 * s}px; -fx-fill: gold; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
            layoutY = config.windowHeight * 0.12 + rankSize + 20 * s
            layoutX = config.windowWidth / 2 - 120 * s
        })

        showBirdsRow(pane)
    }

    /**
     * Los tres pájaros en fila horizontal, un poco a la izquierda de
     * pantalla y con el mismo tamaño que en el juego (solo sprites, sin
     * hitbox). Simulan la pantalla del minijuego tras terminar.
     */
    private fun showBirdsRow(pane: Pane) {
        val s = config.scale
        val size = config.birdSize
        val gapBetween = 20.0 * s
        val total = birdSpriteFiles.size
        val startX = 40.0 * s
        val yPos = config.windowHeight * 0.62
        birdSpriteFiles.forEachIndexed { i, file ->
            pane.children.add(ImageView(spriteSource.load(file)).apply {
                isPreserveRatio = true
                fitWidth = size
                fitHeight = size
                layoutX = startX + i * (size + gapBetween)
                layoutY = yPos
            })
        }
    }
}
