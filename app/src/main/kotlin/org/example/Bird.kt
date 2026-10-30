package org.example

/**
 * Marca a las entidades lanzables (proyectiles): Red, Chuck, Bomb.
 * Tienen física dinámica (gravedad + integración) cuando se disparan.
 * Define el contrato para habilidades especiales (OCP: agregar pájaros nuevos
 * sin tocar el controlador, cada pájaro implementa su propia habilidad).
 */
interface Bird : GameEntity {

    /** Masa (kg) del pájaro: la física la usa para que colisione como real. */
    val mass: Float

    /** Indica si la habilidad especial ya fue usada en este vuelo. */
    var abilityUsed: Boolean

    /**
     * Ejecuta la habilidad especial. Devuelve true si se usó (y por lo tanto
     * queda consumida para este vuelo). Si la habilidad genera efectos
     * visuales, los pide vía [AbilityEffects]: nunca pregunta el tipo de
     * pájaro hacia afuera (OCP) ni conoce la escena (DIP).
     */
    fun useAbility(physics: PhysicsWorld, config: GameConfig, fx: AbilityEffects): Boolean

    /** Indica si la habilidad especial aún está disponible para este vuelo. */
    fun canUseAbility(): Boolean = !abilityUsed

    /** Posición del centro del pájaro (efectos visuales, explosión, etc.). */
    val centerX: Double
    val centerY: Double
}
