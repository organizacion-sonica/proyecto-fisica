package org.example

/**
 * Bomb (pájaro negro): sprite + hitbox circular.
 * Masa: 10 kg
 * Habilidad: al presionar G en vuelo, genera onda expansiva.
 */
class Bomb(
    spriteSource: SpriteSource,
    size: Double = 60.0,
    showHitbox: Boolean = true,
    val mass: Float = 10.0f
) : AbstractEntity(
    spriteSource,
    assetName = "bomb.png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> CircularHitbox(radius = s * 0.33) },
    hitboxOffsetX = size * (-0.02),
    hitboxOffsetY = size * 0.04
), Bird {

    override var abilityUsed = false

    /** Genera onda expansiva en la posición actual. */
    fun explode(physics: Box2DWorld, config: GameConfig) {
        if (abilityUsed) return
        abilityUsed = true
        physics.explodeAt(this, config.bombExplosionRadius, config.bombExplosionForce)
    }

    override fun useAbility(physics: Box2DWorld, config: GameConfig): Boolean {
        if (abilityUsed) return false
        explode(physics, config)
        return true
    }

    override val centerX: Double get() = x + width / 2
    override val centerY: Double get() = y + height / 2
}