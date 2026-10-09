package org.example

import javafx.scene.image.Image
import java.io.File

/**
 * Implementación de [SpriteSource] que carga imágenes desde una carpeta del
 * disco. Responsabilidad única: traducir un nombre a un [Image] leyendo un
 * archivo.
 */
class FileSpriteSource(private val baseDir: File) : SpriteSource {

    override fun load(name: String): Image {
        val file = File(baseDir, name)
        require(file.exists()) { "No se encontró el sprite '$name': ${file.absolutePath}" }
        return Image(file.toURI().toString())
    }

    /** Carga decodificando ya en el tamaño pedido (más rápido para cachés). */
    fun loadScaled(name: String, width: Double, height: Double): Image {
        val file = File(baseDir, name)
        require(file.exists()) { "No se encontró el sprite '$name': ${file.absolutePath}" }
        return Image(file.toURI().toString(), width, height, true, true)
    }
}
