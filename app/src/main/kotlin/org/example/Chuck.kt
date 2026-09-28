package org.example

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

    /** True si ya usó su habilidad especial (solo una vez por lanzamiento). */
    var abilityUsed = false

    /** Aumenta la velocidad actual del pájaro (boost de Chuck). */
    fun boostSpeed(multiplier: Float, physics: Box2DWorld) {
        if (abilityUsed) return
        abilityUsed = true
        physics.boostBirdSpeed(this, multiplier)
    }
}