package org.example

import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane

/**
 * Colaboradores de un nivel del minijuego de basket (DIP).
 */
data class BasketGameCollaborators(
    val aimAssist: AimAssist,
    val scoreLabel: Label,
    val rankView: ImageView,
    val timerLabel: Label,
    val hoop: BasketHoop,
    val rankScreen: RankDisplay,
    val scoreBoard: BasketScoreBoard,
    val timer: BasketTimer
)
