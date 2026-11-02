package org.example

import javafx.geometry.Point2D
import javafx.scene.layout.Pane

/**
 * Abstracciones de los colaboradores del juego (Principio de Inversión de
 * Dependencias): los controladores dependen de estas interfaces y no de las
 * implementaciones concretas.
 */
interface TrajectoryPredictor {
    fun predictedDots(start: Point2D, pull: Point2D): List<Point2D>
}

interface RankDisplay {
    fun show(pane: Pane, score: Int, rankLetter: String? = null)
}

interface ScoreKeeper {
    fun score(): Int
    fun addPoint()
    fun addPoints(n: Int)
    fun tick(dt: Double)
    fun reset()
    fun placeIn(pane: Pane)
    var onTimeOver: (() -> Unit)?
}

interface AngleDisplay {
    fun placeIn(pane: Pane)
    fun showAngle(pull: Point2D)
    fun reset()
}

interface TargetScene {
    var onTargetHit: ((Int) -> Unit)?
    fun setup()
    fun update(dt: Double, inFlight: List<Bird>)
    fun breakTargetsWithin(cx: Double, cy: Double, radius: Double)
}

interface ExplosionVisuals {
    fun playAt(cx: Double, cy: Double)
}

/**
 * Colaboradores de un nivel del minijuego de dianas, creados por una fábrica
 * (DIP): el controlador no instancia concretos; los recibe de la fábrica.
 */
data class GameCollaborators(
    val angleIndicator: AngleDisplay,
    val trajectoryLayer: Pane,
    val trajectoryCalculator: TrajectoryPredictor,
    val explosionPlayer: ExplosionVisuals,
    val scoreBoard: ScoreKeeper,
    val dianaManager: TargetScene,
    val rankScreen: RankDisplay
)
