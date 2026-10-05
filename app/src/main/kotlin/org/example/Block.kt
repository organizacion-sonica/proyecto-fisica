package org.example

/**
 * Marca a los obstáculos estáticos (cubos y tablas). No se mueven; solo
 * participan como superficie de colisión para los [Bird].
 */
interface Block : GameEntity {
    /** "madera" o "piedra": define la densidad (peso) del cuerpo. */
    val material: String
}
