package org.example

import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.shape.Shape

/**
 * Representación visual de una entidad: el sprite más el contorno de su
 * [Hitbox]. Responsabilidad única: renderizado/posicionamiento. No conoce
 * lógica de juego ni física.
 *
 * El contorno lo crea y lo mueve cada hitbox por sí misma (polimorfismo,
 * OCP): al agregar una forma nueva esta clase no se modifica.
 */
class EntityView(
    spriteSource: SpriteSource,
    assetName: String,
    size: Double,
    val hitbox: Hitbox,
    showHitbox: Boolean,
    private val hitboxOffsetX: Double = 0.0,
    private val hitboxOffsetY: Double = 0.0
) {
    private val imageView: ImageView
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

    init {
        imageView = ImageView(spriteSource.load(assetName)).apply {
            isPreserveRatio = true
            fitWidth = size
            fitHeight = size
        }
        overlay = hitbox.createOverlayNode()
        overlay.isVisible = showHitbox
    }

    /** Mueve la hitbox para seguir al sprite y actualiza el contorno. */
    fun syncHitbox() {
        hitbox.recenter(centerX + hitboxOffsetX, centerY + hitboxOffsetY)
        hitbox.syncOverlayNode(overlay)
    }

    /** Traslada el sprite. */
    fun moveBy(dx: Double, dy: Double) {
        imageView.layoutX = x + dx
        imageView.layoutY = y + dy
        syncHitbox()
    }

    /** Establece la posición exacta del sprite. */
    fun setPosition(sx: Double, sy: Double) {
        imageView.layoutX = sx
        imageView.layoutY = sy
        syncHitbox()
    }

    /** Establece la rotación del sprite en grados. */
    fun setRotation(degrees: Double) {
        imageView.rotate = degrees
    }

    /** Muestra u oculta sprite + contorno (p. ej. dianas al reaparecer). */
    fun setVisible(visible: Boolean) {
        imageView.isVisible = visible
        overlay.isVisible = visible
    }

    /** Muestra u oculta solo el contorno de depuración. */
    fun setHitboxVisible(visible: Boolean) {
        overlay.isVisible = visible
    }

    /** Ubica el sprite en [pane] en (x, y) y actualiza el contorno. */
    fun placeAt(pane: Pane, x: Double, y: Double) {
        imageView.layoutX = x
        imageView.layoutY = y
        // Solo agregar al pane la primera vez; agregar de nuevo lanza
        // IllegalArgumentException por hijos duplicados.
        if (imageView.parent == null) {
            pane.children.addAll(imageView, overlay)
        }
        syncHitbox()
    }

    /** Quita el sprite y su contorno del [pane]. */
    fun removeFrom(pane: Pane) {
        pane.children.removeAll(imageView, overlay)
        imageView.layoutX = 0.0
        imageView.layoutY = 0.0
    }
}
