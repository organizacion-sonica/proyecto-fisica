package org.example

/**
 * Abstracción del mundo físico (Principio de Inversión de Dependencias).
 * Los controladores, pájaros y efectos dependen de esta interfaz y NO de la
 * implementación concreta ([Box2DWorld]). Así el motor de física se puede
 * reemplazar (o simular en tests) sin tocar la lógica del juego.
 */
interface PhysicsWorld {

    /** Registra un pájaro como cuerpo dinámico con su masa específica (kg). */
    fun registerBird(bird: GameEntity, mass: Float)

    /** Caja estática (centro y tamaño en px de pantalla) para rebotes. */
    fun addStaticBox(
        centerXPx: Double,
        centerYPx: Double,
        widthPx: Double,
        heightPx: Double,
        friction: Double = 0.4,
        restitution: Double = 0.4
    )

    /** Caja estática con nombre, para poder moverla después (p. ej. backboard del aro). */
    fun addNamedStaticBox(
        name: String,
        centerXPx: Double,
        centerYPx: Double,
        widthPx: Double,
        heightPx: Double,
        friction: Double = 0.4,
        restitution: Double = 0.4
    )

    /** Mueve una caja estática nombrada a una nueva posición. */
    fun moveNamedStaticBox(name: String, centerXPx: Double, centerYPx: Double)

    /** Aplica un impulso de lanzamiento a un cuerpo registrado. */
    fun launchBird(bird: GameEntity, vx: Double, vy: Double)

    /** Marca un cuerpo como kinemático (fijo, no responde a fuerzas). */
    fun setKinematic(entity: GameEntity)

    /** Marca un cuerpo como dinámico. */
    fun setDynamic(entity: GameEntity)

    /** Mueve un cuerpo a una posición de pantalla específica. */
    fun setTransform(entity: GameEntity, sx: Double, sy: Double)

    /** Verifica si un cuerpo está en reposo (velocidad < umbral). */
    fun isResting(entity: GameEntity): Boolean

    /** Aumenta la velocidad del pájaro (habilidad de Chuck). */
    fun boostBirdSpeed(bird: GameEntity, multiplier: Float)

    /** Genera onda expansiva en la posición del cuerpo (habilidad de Bomb). */
    fun explodeAt(bird: GameEntity, radiusPx: Float, force: Float)

    /** Elimina un cuerpo del mundo físico. */
    fun remove(entity: GameEntity)

    /** Avanza la simulación con un paso fijo de tiempo y sincroniza. */
    fun step(dt: Double = 1.0 / 60.0)

    /** Libera todo el mundo físico. */
    fun dispose()
}
