package org.example

import javafx.scene.layout.Pane

/**
 * Reproduce la animación de explosión (sprites + onda expansiva).
 * Responsabilidad única: efectos visuales de la explosión de Bomb.
 */
class ExplosionPlayer(
    private val pane: Pane,
    private val spriteSource: SpriteSource,
    private val config: GameConfig
) {
    /** Frames de explosión pre-cargados (se cargan una sola vez para que el
     * toque de la habilidad no congele el hilo de UI). */
    private val explosionFrames: List<javafx.scene.image.Image> =
        (0..15).map {
            (spriteSource as? FileSpriteSource)?.loadScaled(
                "explosion_%02d.png".format(it),
                160.0 * config.scale,
                160.0 * config.scale
            ) ?: spriteSource.load("explosion_%02d.png".format(it))
        }

    /** Reproduce los sprites de explosión (`explosion_XX.png`) en la posición. */
    fun playAt(cx: Double, cy: Double) {
        val frames = explosionFrames
        val size = 160.0 * config.scale
        val iv = javafx.scene.image.ImageView(frames.first()).apply {
            isPreserveRatio = true
            fitWidth = size
            fitHeight = size
            layoutX = cx - size / 2
            layoutY = cy - size / 2
        }
        pane.children.add(iv)

        val timeline = javafx.animation.Timeline()
        for ((index, frame) in frames.withIndex()) {
            timeline.keyFrames.add(
                javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(index * 55.0),
                    javafx.event.EventHandler<javafx.event.ActionEvent> { iv.image = frame }
                )
            )
        }
        timeline.setOnFinished { pane.children.remove(iv) }
        timeline.play()

        // --- Onda expansiva: dos anillos que crecen y se disipan ---
        createShockwave(cx, cy, 0.10)
        createShockwave(cx, cy, 0.25)
    }

    /** Dibuja un anillo expandible que hace efecto de onda expansiva. */
    private fun createShockwave(cx: Double, cy: Double, delaySec: Double) {
        val maxR = config.bombExplosionRadius.toDouble()
        val ring = javafx.scene.shape.Circle(cx, cy, 5.0 * config.scale).apply {
            // Hitbox de la onda expansiva: relleno translúcido + borde
            fill = javafx.scene.paint.Color.web("#FFA500", 0.25)
            stroke = javafx.scene.paint.Color.web("#FFA500")
            strokeWidth = 6.0 * config.scale
            opacity = 0.0
        }
        pane.children.add(ring)

        val from = javafx.animation.KeyFrame(
            javafx.util.Duration.ZERO,
            javafx.animation.KeyValue(ring.radiusProperty(), 8.0 * config.scale),
            javafx.animation.KeyValue(ring.opacityProperty(), 0.9)
        )
        val timeline = javafx.animation.Timeline()
        timeline.keyFrames.add(from)
        timeline.keyFrames.add(javafx.animation.KeyFrame(
            javafx.util.Duration.millis(650.0),
            javafx.animation.KeyValue(ring.radiusProperty(), maxR),
            javafx.animation.KeyValue(ring.opacityProperty(), 0.0)
        ))
        timeline.delay = javafx.util.Duration.millis(delaySec * 1000)
        timeline.setOnFinished { pane.children.remove(ring) }
        timeline.play()
    }
}
