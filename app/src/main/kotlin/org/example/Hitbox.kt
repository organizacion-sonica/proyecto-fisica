package org.example

import javafx.scene.paint.Color
import javafx.scene.shape.Shape

/**
 * Hitbox de un cuerpo: geometría con dos responsabilidades acotadas:
 *  1. Consultas de geometría (centro, punto interior, colisión con otra) —
 *     resolvidas por POLIMORFISMO con doble dispatch, sin `when` por tipos.
 *  2. Dibujar/sincronizar su propio contorno de depuración (cada forma sabe
 *     pintarse: Open-Closed: agregar una forma nueva no toca EntityView).
 *
 * Es una jerarquía cerrada ([sealed]): agregar una forma nueva implica
 * sumar una subclase acá y programar sus dos combinaciones de colisión.
 */
sealed class Hitbox {
    abstract val centerX: Double
    abstract val centerY: Double
    abstract fun recenter(cx: Double, cy: Double)
    abstract fun contains(px: Double, py: Double): Boolean

    // ── Colisiones (doble dispatch: sin switch por tipos) ────────────────

    /** True si esta y la otra hitbox se superponen. */
    abstract fun collides(other: Hitbox): Boolean

    /** Visita de una colisión provista desde un círculo. */
    abstract fun collidesWithCircle(circle: CircularHitbox): Boolean

    /** Visita de una colisión provista desde una caja. */
    abstract fun collidesWithBox(box: BoxHitbox): Boolean

    /** Visita de una colisión provista desde un óvalo. */
    abstract fun collidesWithOval(oval: OvalHitbox): Boolean

    // ── Aparamiento físico (sin checks de tipos desde la física) ─────────

    /**
     * Círculo de colisión: centro (px) y radio. Lo usa la física para
     * construir el cuerpo de los cuerpos percutibles.
     */
    abstract fun circleSpec(): Triple<Double, Double, Double>

    /**
     * Rectángulo de colisión: centro (px) y semiancho/semialto. Lo usa la
     * física para construir el cuerpo de los bloques.
     */
    abstract fun rectangleSpec(): Pair<Pair<Double, Double>, Pair<Double, Double>>

    // ── Contorno de depuración ────────────────────────────────────────────

    /** Crea el nodo JavaFX que dibuja el contorno de la forma. */
    abstract fun createOverlayNode(): Shape

    /** Mueve el contorno para que coincida con la geometría actual. */
    abstract fun syncOverlayNode(node: Shape)

    companion object {
        /** Apariencia roja común de todo contorno de depuración. */
        internal fun paintRed(node: Shape) {
            node.fill = null
            node.stroke = Color.RED
            node.strokeWidth = 2.0
        }
    }
}
