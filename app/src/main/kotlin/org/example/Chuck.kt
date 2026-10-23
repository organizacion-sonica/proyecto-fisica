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
    showHitbox: Boolean = true,
    val mass: Float = 3.0f
) : AbstractEntity(
    spriteSource,
    assetName = "chuck .png",
    entitySize = size,
    showHitbox = showHitbox,
    hitboxFactory = { s -> CircularHitbox(radius = s * 0.42) }
), Bird {

    override var abilityUsed = false

    /** Aumenta la velocidad actual del pájaro (boost de Chuck). */
    fun boostSpeed(multiplier: Float, physics: Box2DWorld) {
        if (abilityUsed) return
        abilityUsed = true
        physics.boostBirdSpeed(this, multiplier)
    }

    override fun useAbility(physics: Box2DWorld, config: GameConfig): Boolean {
        if (abilityUsed) return false
        boostSpeed(config.chuckSpeedBoost, physics)
        return true
    }

    override val centerX: Double get() = x + width / 2
    override val centerY: Double get() = y + height / 2
}