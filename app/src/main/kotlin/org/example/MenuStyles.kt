package org.example

/**
 * Estilos compartidos del menú (SRP: la apariencia vive acá y no en el
 * controlador de navegación).
 */
object MenuStyles {
    val buttonNormal: String = """
        -fx-background-color: linear-gradient(#e74c3c, #c0392b);
        -fx-background-radius: 10;
        -fx-border-color: #ffffff;
        -fx-border-width: 2;
        -fx-border-radius: 10;
        -fx-cursor: hand;
    """

    val buttonHover: String = """
        -fx-background-color: linear-gradient(#ff6b5b, #e74c3c);
        -fx-background-radius: 10;
        -fx-border-color: #fff;
        -fx-border-width: 3;
        -fx-border-radius: 10;
        -fx-cursor: hand;
    """

    val menuBackground: String =
        "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #2c3e50, #1a1a2e);"

    val titleShadow: String =
        "-fx-effect: dropshadow(gaussian, black, 10, 0.5, 2, 2);"

    /** Estilo del botón según el estado del puntero (comportamiento del concepto). */
    fun buttonStyle(hover: Boolean): String {
        return if (hover) buttonHover else buttonNormal
    }

    fun describe(): String {
        return "MenuStyles"
    }
}
