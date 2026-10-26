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
    override val mass: Float = 10000.0f
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

    /**
     * Habilidad: onda expansiva (física) + pedido de efectos visuales.
     * El pájaro no sabe cómo se pinta la explosión ni derriba dianas:
     * solo lo pide a [AbilityEffects] (DIP). Y el controlador nunca
     * pregunta el tipo: puro polimorfismo (OCP).
     */
    override fun useAbility(physics: PhysicsWorld, config: GameConfig, fx: AbilityEffects): Boolean {
        if (abilityUsed) return false
        abilityUsed = true
        val radiusPx = config.bombExplosionRadius
        physics.explodeAt(this, radiusPx, config.bombExplosionForce)
        fx.explosionAt(centerX, centerY, radiusPx.toDouble())
        return true
    }

    override val centerX: Double get() = x + width / 2
    override val centerY: Double get() = y + height / 2
}