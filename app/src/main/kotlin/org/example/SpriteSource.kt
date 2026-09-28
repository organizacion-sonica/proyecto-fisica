package org.example

import javafx.scene.image.Image

/**
 * Fuente de donde se cargan los sprites (abstracción). Permite que el resto
 * del código dependa de esta interfaz y no de un sistema de archivos concreto
 * (Principio de Inversión de Dependencias). Se puede implementar con archivos,
 * recursos del classpath, etc.
 */
interface SpriteSource {
    fun load(name: String): Image
}
