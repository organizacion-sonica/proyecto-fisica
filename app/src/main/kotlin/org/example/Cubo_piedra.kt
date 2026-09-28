package org.example

/**
 * Cubo de piedra: sprite + hitbox cuadrada que calza con el objeto real
 * (~0.22 del lado del sprite, centrado en 0.58/0.58 de la imagen).
 */
class Cubo_piedra(
    spriteSource: SpriteSource,
    size: Double = 140.0,
    showHitbox: Boolean = true
) : AbstractEntity(
    spriteSource,
    assetName = "cubo piedra.png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> BoxHitbox(halfWidth = s * 0.11, halfHeight = s * 0.11) },
    hitboxOffsetX = size * 0.08,
    hitboxOffsetY = size * 0.08
), Block
