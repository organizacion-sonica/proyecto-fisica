package org.example

import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import kotlin.random.Random

/**
 * El aro de basket y su pared trasera (backboard): posición, movimiento,
 * zona de verificación (círculo verde) y hitbox invisible que impide tocar
 * la red "gratis". Responsabilidad única: el aro y su geometría; el juego
 * decide cuándo moverlo y cómo puntuar.
 */
class Hoop(
    private val spriteSource: SpriteSource,
    private val config: GameConfig,
    private val pane: Pane,
    private val physics: PhysicsWorld
) : BasketHoop {
    private val width: Double = 140.0 * config.scale
    private val height: Double = 140.0 * config.scale

    /** Centro X del aro (fijo, no cambia al moverse). */
    override val centerX: Double
        get() = view.layoutX + width * (315.0 / 554.0)

    /** Centro Y del aro (rim center), cambia al moverse. */
    override var centerY: Double = 0.0
        private set

    /** Radio de la zona verde (verificación) como fracción del ancho del aro. */
    override val scoreRadius: Double = width * 0.18

    /** Radio de la zona verde al cuadrado (para comparación rápida). */
    override val scoreRadiusSq: Double get() = scoreRadius * scoreRadius

    override val view: ImageView = ImageView(spriteSource.load("aro_de_basket.png")).apply {
        isPreserveRatio = true
        fitWidth = width
        fitHeight = height
    }

    // Barrita (backboard) invisible.
    private val backboardWidth: Double = 8.0 * config.scale
    private val backboardHeight: Double = 0.3 * 140.0 * config.scale

    private val backboardName = "backboard"

    init {
        // Posición X: el borde derecho visible del tablero (x=482 de 554) queda en X = 1875
        val targetRightX = 1875.0 * (config.windowWidth / 1920.0)
        val hoopX = targetRightX - width * (482.0 / 554.0)
        view.layoutX = hoopX

        // Posición Y inicial: aleatoria entre 20% y 70% de la altura
        centerY = config.windowHeight * (0.2 + Random.nextDouble() * 0.5)
        view.layoutY = centerY - height / 2

        pane.children.add(view)
        view.toFront()

        val wallX = hoopX + width * 0.30
        val wallY = centerY + 15.0 * config.scale
        physics.addNamedStaticBox(backboardName, wallX, wallY, backboardWidth, backboardHeight)
    }

    /** Mueve el aro (y su backboard) a una nueva posición Y aleatoria. */
    override fun moveToNewPosition() {
        centerY = config.windowHeight * (0.2 + Random.nextDouble() * 0.5)
        view.layoutY = centerY - height / 2

        val targetRightX = 1875.0 * (config.windowWidth / 1920.0)
        val hoopX = targetRightX - width * (482.0 / 554.0)
        val wallX = hoopX + width * 0.30
        val wallY = centerY + 15.0 * config.scale
        physics.moveNamedStaticBox(backboardName, wallX, wallY)
    }
}
