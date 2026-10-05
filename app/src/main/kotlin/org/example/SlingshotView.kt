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

    /** Factor de escala respecto del lienzo base (1920x1080 → 2.0). */
    val uiScale: Double = config.windowWidth / 960.0

    /** Punto donde reposa la badana sin estirar (ancla de la resortera). */
    val anchor: Point2D

    /** Distancia máxima de estiramiento en píxeles. */
    val maxStretch: Double = config.maxStretch

    private var dragging = false

    /** True mientras el usuario está estirando la badana. */
    val isDragging: Boolean get() = dragging

    /** Vector de estiramiento actual (dirección de lanzamiento y fuerza). */
    val pull: Point2D get() = currentPull
    private var currentPull = Point2D.ZERO

    /** Punto donde se empezó a arrastrar (para medir el estiramiento relativo). */
    private var dragStart: Point2D? = null

    /** Posición del pájaro estirado (ancla + desplazamiento de drag, cap 200px). */
    var birdStretchPos: Point2D
        private set

    // Líneas visuales de la badana (elásticos)
    private var bandLeft: Line? = null
    private var bandRight: Line? = null
    private var bandsPane: Pane? = null

    init {
        // Posicionar en la esquina inferior izquierda
        val floorY = config.windowHeight - config.floorMargin
        val x = 60.0 * uiScale  // margen desde el borde izquierdo
        imageView.layoutX = x
        imageView.layoutY = floorY - imageView.fitHeight
        // Ancla: centro superior de la resortera (donde se carga el pájaro)
        anchor = Point2D(x + drawWidth / 2, imageView.layoutY + imageView.fitHeight * 0.28)
        birdStretchPos = anchor
    }

    fun placeIn(pane: Pane) {
        pane.children.add(imageView)
        bandsPane = pane
    }

    fun tryStartDrag(px: Double, py: Double): Boolean {
        if (!dragging && Point2D(px, py).distance(anchor) < grabRadius) {
            dragging = true
            dragStart = Point2D(px, py)
            currentPull = Point2D.ZERO
            birdStretchPos = anchor
        }
        return dragging
    }

    /** Activa el drag sin verificar distancia (para cuando ya hay pájaro cargado). */
    fun forceStartDrag(px: Double, py: Double) {
        if (!dragging) {
            dragging = true
            dragStart = Point2D(px, py)
            currentPull = Point2D.ZERO
            birdStretchPos = anchor
        }
    }

    /**
     * Actualiza el estiramiento según el desplazamiento desde donde se empezó
     * a arrastrar: estiramiento chico → lanzamiento suave; estiramiento
     * grande → lanzamiento fuerte. Así la velocidad SIEMPRE es proporcional
     * al estiramiento (máx. 200 px), no al punto donde se hizo clic.
     */
    fun drag(px: Double, py: Double) {
        if (!dragging) return
        val start = dragStart ?: return
        val delta = Point2D(px - start.x, py - start.y)
        val clamped = if (delta.magnitude() > maxStretch)
            delta.multiply(maxStretch / delta.magnitude()) else delta
        currentPull = Point2D(-clamped.x, -clamped.y) // dirección = opuesta a la tracción
        birdStretchPos = Point2D(anchor.x + clamped.x, anchor.y + clamped.y)

        val ratio = clamped.magnitude() / maxStretch
        val index = (ratio * (frames.size - 1)).toInt().coerceIn(0, frames.size - 1)
        imageView.image = frames[index]
        drawBands(birdStretchPos.x, birdStretchPos.y)
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
        birdStretchPos = anchor
        clearBands()
        animateRelease()
        return pull
    }

    /** Dibuja las líneas elásticas de la resortera desde los dos extremos delantera hasta la punta. */
    private fun drawBands(birdX: Double, birdY: Double) {
        val pane = bandsPane ?: return
        clearBands()

        // Dos puntos de anclaje en la horquilla de la resortera
        val forkLeftX = anchor.x - 12 * uiScale
        val forkLeftY = anchor.y + 20 * uiScale
        val forkRightX = anchor.x + 12 * uiScale
        val forkRightY = anchor.y + 20 * uiScale

        bandLeft = Line(forkLeftX, forkLeftY, birdX, birdY).apply {
            stroke = Color.web("#4A2800")
            strokeWidth = 4.0 * uiScale
        }
        bandRight = Line(forkRightX, forkRightY, birdX, birdY).apply {
            stroke = Color.web("#4A2800")
            strokeWidth = 4.0 * uiScale
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