package org.example

import javafx.scene.layout.Pane

/**
 * Objeto del juego. Expone solo lo mínimo que otros necesitan: su [Hitbox]
 * para colisiones y su [EntityView] para pintarse (Segregación de interfaces).
 * Quienes consumen entidades dependen de esta abstracción (Inversión).
 */
interface GameEntity {
    val hitbox: Hitbox
    val view: EntityView

    /** Posición y tamaño lógicos (esquina superior izquierda del sprite). */
    val x: Double
    val y: Double
    val width: Double
    val height: Double
    val bottom: Double

    fun placeAt(pane: Pane, x: Double, y: Double)

    /** Traslada el sprite y sincroniza la hitbox. */
    fun moveBy(dx: Double, dy: Double)

    fun contains(x: Double, y: Double): Boolean
}
