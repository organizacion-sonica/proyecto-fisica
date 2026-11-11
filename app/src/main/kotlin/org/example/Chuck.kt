package org.example

import javafx.scene.input.KeyCode

/**
 * Chuck (pájaro amarillo): sprite + hitbox circular.
 * Masa: 3 kg
 * Habilidad: al presionar G en vuelo, aumenta su velocidad.
 *
 * Nota: el archivo del sprite se llama "chuck .png" (con espacio antes de la
 * extensión), tal como está guardado en "elementos graficos/".
 */
class Chuck(
    spriteSource: SpriteSource,
    size: Double = 60.0,
    showHitbox: Boolean = false,
    override val mass: Float = 3.0f
) : AbstractEntity(
    spriteSource,
    assetName = "chuck .png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> HitboxFactory.circular(s * 0.42) }
), Bird {

    private var used = false

    /** El estado de la habilidad es interno: solo se consulta (encapsulación). */
    override val abilityUsed: Boolean get() = used

    /** Aumenta la velocidad actual del pájaro (boost de Chuck). */
    private fun boostSpeed(multiplier: Float, physics: PhysicsWorld) {
        if (used) return
        used = true
        physics.boostBirdSpeed(this, multiplier)
    }

    override fun useAbility(physics: PhysicsWorld, config: GameConfig, fx: AbilityEffects): Boolean {
        if (used) return false
        boostSpeed(config.chuckSpeedBoost, physics)
        return true
    }

    override val centerX: Double get() = x + width / 2
    override val centerY: Double get() = y + height / 2
}