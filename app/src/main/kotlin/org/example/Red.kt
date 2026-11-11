package org.example

/**
 * Ave roja: sprite + hitbox circular.
 * Masa: 5 kg
 * Sin habilidad especial.
 */
class Red(
    spriteSource: SpriteSource,
    size: Double = 60.0,
    showHitbox: Boolean = false,
    override val mass: Float = 5.0f
) : AbstractEntity(
    spriteSource,
    assetName = "Red.png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> HitboxFactory.circular(s * 0.34) },
    hitboxOffsetX = size * 0.01,
    hitboxOffsetY = size * 0.08
), Bird {

    private var used = false

    /** Red no tiene habilidad especial: el estado nunca cambia (encapsulación). */
    override val abilityUsed: Boolean get() = used

    /** Descripción del pájaro con su masa (comportamiento del concepto). */
    fun describe(): String {
        return "Red (ave roja, masa ${mass} kg)"
    }

    override val centerX: Double get() = x + width / 2
    override val centerY: Double get() = y + height / 2
}