package org.example

/**
 * Efectos que un pájaro pide al juego cuando usa su habilidad (Inversión de
 * Dependencias): el pájaro decide CUÁNDO dispara su habilidad y el juego
 * (que implementa [AbilityEffects]) decide cómo mostrarla.
 *
 * Así el controlador no necesita preguntar por el tipo del pájaro (OCP):
 * Red pide nada, Chuck solo acelera y Bomb pide una explosión.
 */
interface AbilityEffects {

    /**
     * Pide mostrar una explosión en (cx, cy) con radio de acción
     * [radiusPx] (en píxeles de pantalla) y sus consecuencias de juego.
     */
    fun explosionAt(cx: Double, cy: Double, radiusPx: Double)
}
