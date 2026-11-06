package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane

/**
 * Orquesta el minijuego 1 (dianas). Responsabilidades propias del nivel:
 *  - Construye el marcador, las dianas, el ángulo de tiro, la trayectoria
 *    esperada y la pantalla final cuando el tiempo se agota.
 *  - Pide la habilidad especial (G) al último pájaro lanzado; el efecto
 *    visual de la explosión lo pinta [ExplosionPlayer].
 *
 * La mecánica de resortera/pila/lanzamiento vive en [SlingshotGame] (DRY).
 * Los colaboradores llegan por una fábrica (DIP): el controlador no
 * instancia concretos.
 */
class GameController(
    spriteSource: SpriteSource,
    config: GameConfig,
    onExitToMenu: () -> Unit = { Unit },
    private val collaboratorFactory: (Pane, PhysicsWorld) -> GameCollaborators
) :
    SlingshotGame(spriteSource, config, onExitToMenu = onExitToMenu), AbilityEffects {

    private lateinit var collaborators: GameCollaborators

    private val angleIndicator: AngleDisplay get() = collaborators.angleIndicator
    private val trajectoryLayer: Pane get() = collaborators.trajectoryLayer
    private val trajectoryCalculator: TrajectoryPredictor get() = collaborators.trajectoryCalculator
    private val trajectoryRenderer: TrajectoryRenderer get() = collaborators.trajectoryRenderer
    private val explosionPlayer: ExplosionVisuals get() = collaborators.explosionPlayer
    private val scoreBoard: ScoreKeeper get() = collaborators.scoreBoard
    private val dianaManager: TargetScene get() = collaborators.dianaManager
    private val rankScreen: RankDisplay get() = collaborators.rankScreen

    private var timerStarted = false

    override fun setupLevel() {
        collaborators = collaboratorFactory(pane, physics)
        angleIndicator.placeIn(pane)
        pane.children.add(trajectoryLayer)
        scoreBoard.placeIn(pane)
        scoreBoard.reset()
        scoreBoard.onTimeOver = { rankScreen.show(pane, scoreBoard.score(), RankEvaluator.rankFor(scoreBoard.score())) }
        dianaManager.onTargetHit = { points -> scoreBoard.addPoints(points) }
        dianaManager.setup()
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

    override fun onBirdLoadedOntoSlingshot() {
        if (!timerStarted) {
            scoreBoard.reset()
            timerStarted = true
        }
    }

    override fun onLevelReset() {
        timerStarted = false
    }

    override fun onAbilityKey() {
        lastLaunched?.useAbility(physics, config, fx = this)
    }

    override fun explosionAt(cx: Double, cy: Double, radiusPx: Double) {
        explosionPlayer.playAt(cx, cy)
        dianaManager.breakTargetsWithin(cx, cy, radiusPx)
    }

    private fun showTrajectory(start: Point2D, pull: Point2D) {
        trajectoryRenderer.render(start, pull) { dot ->
            javafx.scene.shape.Circle(dot.x, dot.y, 4.5 * config.scale).apply {
                fill = javafx.scene.paint.Color.web("#FFFFFF", 0.85)
                stroke = javafx.scene.paint.Color.web("#222222", 0.6)
                strokeWidth = 1.0
            }
        }
    }
}
