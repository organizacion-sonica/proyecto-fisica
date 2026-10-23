package org.example

import javafx.scene.input.KeyCode

/**
 * Marca a las entidades lanzables (proyectiles): Red, Chuck, Bomb.
 * Tienen física dinámica (gravedad + integración) cuando se disparan.
 * Define el contrato para habilidades especiales (OCP: agregar pájaros nuevos
 * sin tocar GameController).
 */
interface Bird : GameEntity {
    /** Indica si la habilidad especial ya fue usada en este vuelo. */
    var abilityUsed: Boolean

    /** Ejecuta la habilidad especial del pájaro. */
    fun useAbility(physics: Box2DWorld, config: GameConfig): Boolean

    /** Posición X del centro del pájaro para efectos visuales (explosión, etc.). */
    val centerX: Double
    val centerY: Double
}
