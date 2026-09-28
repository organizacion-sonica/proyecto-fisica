package org.example

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.geometry.Point2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.util.Duration
import kotlin.math.min

/**
 * Encapsula toda la resortera: carga sus frames, se posiciona en la esquina
 * inferior izquierda, expone su punto de ancla y maneja el estiramiento/rebote.
 * Responsabilidad única: comportamiento visual de la resortera.
 */
class SlingshotView(
    spriteSource: SpriteSource,
    private val config: GameConfig,
    frameNames: List<String> = (0..17).map { "sprite_%02d.png".format(it) }
) {
    private val frames: List<Image> = frameNames.map { spriteSource.load(it) }

    private val fitHeight: Double = config.slingshotHeight
    val grabRadius: Double = config.slingshotGrabRadius

    val imageView: ImageView = ImageView(frames.first()).apply {
        isPreserveRatio = true
        this.fitHeight = this@SlingshotView.fitHeight
    }

    private val drawWidth: Double = frames.first().width / frames.first().height * fitHeight

    /** Punto donde reposa la badana sin estirar (ancla de la resortera). */
    val anchor: Point2D

    /** Distancia máxima de estiramiento en píxeles. */
    val maxStretch: Double = config.maxStretch

    private var dragging = false

    /** True mientras el usuario está estirando la badana. */
    val isDragging: Boolean get() = dragging

    /** Vector de estiramiento actual: ancla → punta (cord) durante el drag. */
    private var currentPull = Point2D.ZERO

    // Líneas visuales de la badana (elásticos)
    private var bandLeft: Line? = null
    private var bandRight: Line? = null
    private var bandsPane: Pane? = null

    init {
        // Posicionar en la esquina inferior izquierda
        val floorY = config.windowHeight - config.floorMargin
        val x = 60.0  // margen desde el borde izquierdo
        imageView.layoutX = x
        imageView.layoutY = floorY - imageView.fitHeight
        // Ancla: centro superior de la resortera (donde se carga el pájaro)
        anchor = Point2D(x + drawWidth / 2, imageView.layoutY + imageView.fitHeight * 0.28)
    }

    fun placeIn(pane: Pane) {
        pane.children.add(imageView)
        bandsPane = pane
    }

    fun tryStartDrag(px: Double, py: Double): Boolean {
        if (!dragging && Point2D(px, py).distance(anchor) < grabRadius) {
            dragging = true
            currentPull = Point2D.ZERO
        }
        return dragging
    }

    /** Activa el drag sin verificar distancia (para cuando ya hay pájaro cargado). */
    fun forceStartDrag() {
        if (!dragging) {
            dragging = true
            currentPull = Point2D.ZERO
        }
    }

    /** Actualiza el frame según cuánto se estiró desde el ancla y dibuja bandas. */
    fun drag(px: Double, py: Double) {
        if (!dragging) return
        val distance = min(Point2D(px, py).distance(anchor), maxStretch)
        val ratio = distance / maxStretch
        val index = (ratio * (frames.size - 1)).toInt().coerceIn(0, frames.size - 1)
        imageView.image = frames[index]
        currentPull = Point2D(anchor.x - px, anchor.y - py) // dirección = opuesta a la tracción
        drawBands(px, py)
    }

    /**
     * Suelta la badana y la anima de vuelta al reposo.
     * @return vector de estiramiento (dirección y fuerza) o null si no había
     *         estiramiento activo. La magnitud es la distancia del estiramiento.
     */
    fun release(): Point2D? {
        if (!dragging) return null
        dragging = false
        val pull = currentPull
        currentPull = Point2D.ZERO
        clearBands()
        animateRelease()
        return pull
    }

    /** Dibuja las líneas elásticas de la resortera desde los dos extremos delantera hasta la punta. */
    private fun drawBands(birdX: Double, birdY: Double) {
        val pane = bandsPane ?: return
        clearBands()

        // Dos puntos de anclaje en la horquilla de la resortera
        val forkLeftX = anchor.x - 12
        val forkLeftY = anchor.y + 20
        val forkRightX = anchor.x + 12
        val forkRightY = anchor.y + 20

        bandLeft = Line(forkLeftX, forkLeftY, birdX, birdY).apply {
            stroke = Color.web("#4A2800")
            strokeWidth = 4.0
        }
        bandRight = Line(forkRightX, forkRightY, birdX, birdY).apply {
            stroke = Color.web("#4A2800")
            strokeWidth = 4.0
        }

        pane.children.addAll(bandLeft, bandRight)
    }

    /** Elimina las líneas elásticas. */
    private fun clearBands() {
        bandLeft?.let { bandsPane?.children?.remove(it) }
        bandRight?.let { bandsPane?.children?.remove(it) }
        bandLeft = null
        bandRight = null
    }

    private fun animateRelease() {
        val currentIndex = frames.indexOf(imageView.image).coerceAtLeast(0)
        val timeline = Timeline()
        for (i in currentIndex downTo 0) {
            val delay = Duration.millis((currentIndex - i) * 15.0)
            timeline.keyFrames.add(KeyFrame(delay, { _ -> imageView.image = frames[i] }))
        }
        timeline.play()
    }
}