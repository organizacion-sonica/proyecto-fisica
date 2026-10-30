package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane

/**
 * Orquesta el minijuego 1 (dianas). Responsibilities propias del nivel:
 *  - Construye el marcador ([ScoreBoard]), las dianas ([DianaManager]),
 *    el ángulo de tiro, la trayectoria esperada y la pantalla final
 *    ([RankScreen]) cuando el tiempo se agota.
 *  - Pide la habilidad especial (G) al último pájaro lanzado; el efecto
 *    visual de la explosión lo pinta [ExplosionPlayer].
 *
 * La mecánica de resortera/pila/lanzamiento vive en [SlingshotGame] (DRY).
 */
class GameController(spriteSource: SpriteSource, config: GameConfig, onExitToMenu: () -> Unit = {}) :
    SlingshotGame(spriteSource, config, onExitToMenu = onExitToMenu), AbilityEffects {

    private lateinit var angleIndicator: AngleIndicator
    private lateinit var trajectoryLayer: Pane
    private lateinit var trajectoryCalculator: TrajectoryCalculator
    private lateinit var scoreBoard: ScoreBoard
    private lateinit var dianaManager: DianaManager
    private lateinit var explosionPlayer: ExplosionPlayer
    private lateinit var rankScreen: RankScreen

    private var timerStarted = false

    override fun setupLevel() {
        angleIndicator = AngleIndicator(config).also { it.placeIn(pane) }
        trajectoryLayer = Pane()
        pane.children.add(trajectoryLayer)
        trajectoryCalculator = TrajectoryCalculator(config)

        explosionPlayer = ExplosionPlayer(pane, spriteSource, config)

        scoreBoard = ScoreBoard(config).also {
            it.placeIn(pane)
            // Al agotar el tiempo se muestra la pantalla final con el rango.
            it.onTimeOver = { rankScreen.show(pane, scoreBoard.score()) }
        }
        dianaManager = DianaManager(spriteSource, config, pane).also {
            it.onTargetHit = { points -> scoreBoard.addPoints(points) }
            it.setup()
        }
        rankScreen = RankScreen(spriteSource, config)
    }

    override fun onPhysicsStep(dt: Double) {
        if (timerStarted) scoreBoard.tick(dt)
        dianaManager.update(dt, inFlight)
    }

    override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {
        angleIndicator.showAngle(pull)
        showTrajectory(stretchPos, pull)
    }

    override fun clearAimLayer() {
        angleIndicator.reset()
        trajectoryLayer.children.clear()
    }

    /**
     * Habilidad del pájaro lanzado (G). El pájaro decide su efecto por
     * polimorfismo y pide los visuales vía [explosionAt]; el controlador
     * nunca pregunta el tipo del pájaro (OCP).
     */
    override fun onBirdLoadedOntoSlingshot() {
        if (!timerStarted) {
            scoreBoard.reset()
            timerStarted = true
        }
    }

    override fun onLevelReset() {
        timerStarted = false
    }

    /**
     * Habilidad del pájaro lanzado (G). El pájaro decide su efecto por
     * polimorfismo y pide los visuales vía [explosionAt]; el controlador
     * nunca pregunta el tipo del pájaro (OCP).
     */
    override fun onAbilityKey() {
        lastLaunched?.useAbility(physics, config, fx = this)
    }

    /** Efecto de explosión pedido por un pájaro (p. ej. Bomb). */
    override fun explosionAt(cx: Double, cy: Double, radiusPx: Double) {
        explosionPlayer.playAt(cx, cy)
        // La onda expansiva derriba dianas dentro de su radio
        dianaManager.breakTargetsWithin(cx, cy, radiusPx)
    }

    /** Dibuja los puntos de la trayectoria esperada sobre la capa de la escena. */
    private fun showTrajectory(start: Point2D, pull: Point2D) {
        trajectoryLayer.children.clear()
        val dots = trajectoryCalculator.predictedDots(start, pull)
        for (dot in dots) {
            trajectoryLayer.children.add(
                javafx.scene.shape.Circle(dot.x, dot.y, 4.5 * config.scale).apply {
                    fill = javafx.scene.paint.Color.web("#FFFFFF", 0.85)
                    stroke = javafx.scene.paint.Color.web("#222222", 0.6)
                    strokeWidth = 1.0
                }
            )
        }
    }
}
