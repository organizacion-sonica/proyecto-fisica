package org.example

/**
 * Hitbox de un cuerpo: geometría pura (centro + consulta de punto), sin
 * conocimiento de JavaFX. Esto la hace testeable sin toolkit gráfico.
 *
 * Es una jerarquía cerrada ([sealed]): agregar una forma nueva implica
 * sumar una subclase en este módulo y es exhaustiva en [when].
 */
sealed class Hitbox {
    abstract val centerX: Double
    abstract val centerY: Double
    abstract fun recenter(cx: Double, cy: Double)
    abstract fun contains(px: Double, py: Double): Boolean
}
