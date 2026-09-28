package org.example

import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

/**
 * Representación visual de una entidad: el sprite más un contorno rojo que
 * dibuja la [Hitbox]. Responsabilidad única: renderizado/posicionamiento.
 * No conoce lógica de juego ni física.
 */
class EntityView(
    spriteSource: SpriteSource,
    assetName: String,
    size: Double,
    private val hitbox: Hitbox,
    showHitbox: Boolean
) {
    val imageView: ImageView
    private val overlay: Shape

    /** Centro del sprite en coordenadas del mundo. */
    val centerX: Double get() = imageView.layoutX + imageView.fitWidth / 2
    val centerY: Double get() = imageView.layoutY + imageView.fitHeight / 2

    /** Posición y tamaño lógicos (esquina superior izquierda). */
    val x: Double get() = imageView.layoutX
    val y: Double get() = imageView.layoutY
    val width: Double get() = imageView.fitWidth
    val height: Double get() = imageView.fitHeight
    val bottom: Double get() = y + height

    /** Traslada el sprite. */
    fun moveBy(dx: Double, dy: Double) {
        imageView.layoutX = x + dx
        imageView.layoutY = y + dy
    }

    /** Establece la posición exacta del sprite. */
    fun setPosition(sx: Double, sy: Double) {
        imageView.layoutX = sx
        imageView.layoutY = sy
    }

    /** Establece la rotación del sprite en grados. */
    fun setRotation(degrees: Double) {
        imageView.rotate = degrees
    }

    init {
        imageView = ImageView(spriteSource.load(assetName))
        imageView.isPreserveRatio = true
        imageView.fitWidth = size
        imageView.fitHeight = size

        overlay = createOverlay()
        overlay.isVisible = showHitbox
    }

    /** Crea el contorno rojo correspondiente al tipo de hitbox. */
    private fun createOverlay(): Shape = when (hitbox) {
        is CircularHitbox -> Circle().apply(paintRed())
        is BoxHitbox -> Rectangle().apply(paintRed())
    }

    private fun paintRed(): Shape.() -> Unit = {
        fill = null
        stroke = Color.RED
        strokeWidth = 2.0
    }

    /** Ubica el sprite en [pane] en (x, y) y actualiza el contorno rojo. */
    fun placeAt(pane: Pane, x: Double, y: Double) {
        imageView.layoutX = x
        imageView.layoutY = y
        pane.children.addAll(imageView, overlay)
        syncOverlay()
    }

    /** Mueve el contorno rojo para que coincida con la geometría actual de la hitbox. */
    fun syncOverlay() {
        when (val h = hitbox) {
            is CircularHitbox -> (overlay as Circle).apply {
                centerX = h.centerX
                centerY = h.centerY
                radius = h.radius
            }
            is BoxHitbox -> (overlay as Rectangle).apply {
                x = h.centerX - h.halfWidth
                y = h.centerY - h.halfHeight
                width = h.halfWidth * 2
                height = h.halfHeight * 2
            }
        }
    }

    fun setHitboxVisible(visible: Boolean) {
        overlay.isVisible = visible
    }

    /** Quita el sprite y su contorno del [pane]. */
    fun removeFrom(pane: Pane) {
        pane.children.removeAll(imageView, overlay)
        imageView.layoutX = 0.0
        imageView.layoutY = 0.0
    }
}
