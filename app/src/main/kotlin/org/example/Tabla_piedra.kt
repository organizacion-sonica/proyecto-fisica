package org.example

/**
 * Tabla de piedra: sprite + hitbox rectangular vertical que calza con el
 * objeto real (~0.12 x 0.41 del sprite, centrado en 0.25/0.48 de la imagen).
 */
class Tabla_piedra(
    spriteSource: SpriteSource,
    size: Double = 160.0,
    showHitbox: Boolean = true
) : AbstractEntity(
    spriteSource,
    assetName = "tabla piedra.png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> BoxHitbox(halfWidth = s * 0.06, halfHeight = s * 0.20) },
    hitboxOffsetX = size * (-0.25),
    hitboxOffsetY = size * (-0.02)
), Block {
    override val material: String = "piedra"
}
