package org.example

/**
 * Marca a los obstáculos (cubos y tablas). Participan como superficie de
 * colisión para los [Bird] y declaran su propio material y densidad:
 * la física no necesita consultar por el material (OCP): cada bloque
 * dice cuán denso es y un material nuevo solo agrega una subclase.
 */
interface Block : GameEntity {
    /** "madera" o "piedra": etiqueta de material del bloque. */
    val material: String

    /** Densidad (kg por unidad de área) que la física usa para su cuerpo. */
    val density: Float
}
