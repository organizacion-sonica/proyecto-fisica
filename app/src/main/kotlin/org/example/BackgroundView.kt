package org.example

import javafx.scene.image.ImageView
import javafx.scene.layout.Pane

/**
 * Fondo del juego escalado para cubrir toda la pantalla.
 * Responsabilidad única: renderizar la imagen de fondo.
 */
class BackgroundView(
    spriteSource: SpriteSource,
    private val config: GameConfig
) {
    val imageView: ImageView = ImageView(spriteSource.load("bg.jpg")).apply {
        fitWidth = config.windowWidth
        fitHeight = config.windowHeight
        isPreserveRatio = false
    }

    /** Inserta el fondo como primer elemento del pane (detrás de todo). */
    fun placeIn(pane: Pane) {
        pane.children.add(0, imageView)
    }
}
