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

    /** Indica si la habilidad especial ya fue usada en este vuelo (solo lectura). */
    val abilityUsed: Boolean

    /**
     * Ejecuta la habilidad especial. Devuelve true si se usó (y por lo tanto
     * queda consumida para este vuelo). Por defecto (Red) no tiene habilidad.
     */
    fun useAbility(physics: PhysicsWorld, config: GameConfig, fx: AbilityEffects): Boolean = false

    /** Indica si la habilidad especial aún está disponible para este vuelo. */
    fun canUseAbility(): Boolean = !abilityUsed

    /** Nombre legible del pájaro (comportamiento del concepto). */
    override fun displayName(): String = this::class.simpleName ?: "Bird"

    /** Posición del centro del pájaro (efectos visuales, explosión, etc.). */
    val centerX: Double
    val centerY: Double
}
