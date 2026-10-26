package org.example

import javafx.application.Application

/**
 * Punto de entrada de la aplicación. Es un [object] (clase única): así no
 * queda ninguna función suelta fuera de una clase (POO 100% del archivo).
 * El montaje real de la app vive en [SlingshotApp] (raíz de composición).
 */
object Resoltera {

    @JvmStatic
    fun main(args: Array<String>) {
        Application.launch(SlingshotApp::class.java)
    }
}
