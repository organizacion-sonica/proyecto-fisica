package org.example

/**
 * Ave roja: sprite + hitbox circular.
 * Masa: 5 kg
 */
class Red(
    spriteSource: SpriteSource,
    size: Double = 60.0,
    showHitbox: Boolean = true,
    val mass: Float = 5.0f
) : AbstractEntity(
    spriteSource,
    assetName = "Red.png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> CircularHitbox(radius = s * 0.34) },
    hitboxOffsetX = size * 0.01,
    hitboxOffsetY = size * 0.08
), Bird