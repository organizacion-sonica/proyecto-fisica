package org.example

/**
 * Texto de instrucciones del menú (contenido, no lógica). Separado del
 * controlador para reducir su tamaño y responsabilidad (SRP).
 */
object InstructionsContent {
    val text: String = """
        CÓMO JUGAR:

        1. SELECCIÓN DE PÁJAROS:
           - En la esquina superior IZQUIERDA hay 3 pájaros apilados:
             Red, Chuck y Bomb
           - Los pájaros son INFINITOS: siempre podés usar uno nuevo
           - Agarrá un pájaro y arrastralo: se crea una COPIA visual
             que sigue al mouse (el original queda en la pila)
           - Soltala sobre la resortera para cargar un pájaro nuevo
           - Si la soltás a mitad de camino, la copia desaparece
             y el suministro queda intacto

        2. LANZAR:
           - Con el pájaro fijado, estirá la resortera hacia atrás
           - Cuanto más la estires, más lejos y rápido volará
           - Soltá el clic para lanzar

        3. HABILIDADES ESPECIALES (Tecla G, en pleno vuelo):
           - Red: Sin habilidad especial
           - Chuck: Presioná G en vuelo para BOOST de velocidad
           - Bomb: Presioná G en vuelo para EXPLOSIÓN con onda
             expansiva que derriba destruye dianas

        4. CONTROLES ADICIONALES:
           - Tecla R: reinicia el nivel en cualquier momento
           - Tecla ESC: vuelve al menú principal (en cualquier minijuego)
           - Al tocar el piso el pájaro desaparece

        5. PUNTUACIÓN Y TIEMPO:
           - Cada diana que derribas suma puntos y vuelve a aparecer
           - En basket: encestar suma 2 puntos
           - El contador de tiempo empieza al iniciar el nivel
           - Al terminar el minuto se muestra la pantalla de rango
    """.trimIndent()
}
