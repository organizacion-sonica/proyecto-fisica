package org.example

/** Evalúa el puntaje final y devuelve la letra de rango (S, A, B, C, D, E). Single Responsibility. */
object RankEvaluator {
    fun rankFor(score: Int): String = when {
        score > 100 -> "S"
        score >= 75 -> "A"
        score >= 50 -> "B"
        score >= 25 -> "C"
        score >= 10 -> "D"
        else -> "E"
    }
}
