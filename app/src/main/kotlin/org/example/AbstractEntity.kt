package org.example

import javafx.scene.layout.Pane

/**
 * Implementación común de una [GameEntity]. Cada subclase entrega su sprite,
 * tamaño, fábrica de hitbox y (si hace falta) el desplazamiento del centro de
 * la hitbox dentro del sprite. La ubicación y el sincronizado de la hitbox son
 * compartidos (patrón template / Open-Closed: agregar entidades sin tocar esto).
 */
abstract class AbstractEntity(
    spriteSource: SpriteSource,
    protected val assetName: String,
    protected val entitySize: Double,
    protected val showHitbox: Boolean,
    protected val hitboxFactory: (Double) -> Hitbox,
    protected val hitboxOffsetX: Double = 0.0,
    protected val hitboxOffsetY: Double = 0.0
) : GameEntity {

    final override val hitbox: Hitbox = hitboxFactory(entitySize)

    final override val view: EntityView =
        EntityView(spriteSource, assetName, entitySize, hitbox, showHitbox)

    final override val x: Double get() = view.x
    final override val y: Double get() = view.y
    final override val width: Double get() = view.width
    final override val height: Double get() = view.height
    final override val bottom: Double get() = view.bottom

    override fun placeAt(pane: Pane, x: Double, y: Double) {
        view.placeAt(pane, x, y)
        syncHitbox()
    }

    /** Mueve el sprite y resincroniza la hitbox para que la siga. */
    override fun moveBy(dx: Double, dy: Double) {
        view.moveBy(dx, dy)
        syncHitbox()
    }

    override fun contains(x: Double, y: Double): Boolean = hitbox.contains(x, y)

    /** Establece la rotación del sprite en grados. */
    fun setRotation(degrees: Double) {
        view.setRotation(degrees)
    }

    /** Establece la posición exacta del sprite (sin offsets). */
    fun setPosition(sx: Double, sy: Double) {
        view.setPosition(sx, sy)
        syncHitbox()
    }

    private fun syncHitbox() {
        hitbox.recenter(view.centerX + hitboxOffsetX, view.centerY + hitboxOffsetY)
        view.syncOverlay()
    }
}
