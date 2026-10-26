package org.example

/** Evalúa el puntaje final y devuelve la letra de rango (S, A, B, C, D, E). Single Responsibility. */
object RankEvaluator {
    /** Rangos del minijuego de dianas (puntajes altos). */
    fun rankFor(score: Int): String = when {
        score > 100 -> "S"
        score >= 75 -> "A"
        score >= 50 -> "B"
        score >= 25 -> "C"
        score >= 10 -> "D"
        else -> "E"
    }

    /** Rangos del minijuego de basket: S a partir de 18, A 15-17, B 12-14, C 9-11, D 6-8, E 0-5. */
    fun rankForBasket(score: Int): String = when {
        score >= 18 -> "S"
        score >= 15 -> "A"
        score >= 12 -> "B"
        score >= 9 -> "C"
        score >= 6 -> "D"
        else -> "E"
    }
}
