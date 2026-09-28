package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.world.World
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Vector2
import kotlin.math.sqrt

/**
 * Adaptador entre dyn4j y el juego JavaFX.
 * Responsabilidad única: simular física y sincronizar resultados con las entidades.
 * Coordina conversiones de coordenadas: dyn4j (Y-up) ↔ JavaFX (Y-down).
 */
class Box2DWorld(private val config: GameConfig) {

    val world: World<Body> = World()

    private val ppm: Float = config.ppm
    private val bodies = mutableMapOf<GameEntity, Body>()

    init {
        world.setGravity(0.0, -config.gravity.toDouble())
        createGround()
    }

    // ── Conversiones de coordenadas ────────────────────────────────────

    /** Pantalla (px, Y-down) → dyn4j (m, Y-up) */
    fun screenToWorld(sx: Double, sy: Double): Vector2 {
        val wx = sx / ppm
        val wy = (config.windowHeight - sy) / ppm
        return Vector2(wx.toDouble(), wy.toDouble())
    }

    /** dyn4j (m, Y-up) → Pantalla (px, Y-down) */
    fun worldToScreen(wx: Double, wy: Double): Pair<Double, Double> {
        val sx = wx * ppm
        val sy = config.windowHeight - wy * ppm
        return sx to sy
    }

    // ── Cuerpos ────────────────────────────────────────────────────────

    /** Crea el piso estático en el borde inferior de la pantalla. */
    private fun createGround() {
        val ground = Body()
        ground.setMass(MassType.INFINITE) // estático

        val groundWidth = config.windowWidth / ppm * 2
        val groundShape = Rectangle(groundWidth, 1.0)
        ground.addFixture(groundShape, 0.0, 0.0, 0.0)
        ground.translate(config.windowWidth / 2.0 / ppm, 0.5)

        world.addBody(ground)
    }

    /** Registra un pájaro como cuerpo dinámico con su masa específica. */
    fun registerBird(bird: GameEntity, mass: Float) {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        val pos = screenToWorld(cx, cy)

        val body = Body()
        body.setMass(MassType.NORMAL) // dinámico

        val radius = bird.width / 2.0 / ppm
        val shape = Circle(radius)
        // Calcular densidad para que la masa coincida: masa = densidad * área
        // Área del círculo = π * r²
        val area = Math.PI * radius * radius
        val density = mass / area
        body.addFixture(shape, density.toDouble(), 0.4, 0.3) // densidad, fricción, rebote
        body.translate(pos.x, pos.y)
        body.linearDamping = 0.1

        world.addBody(body)
        bodies[bird] = body
    }

    /** Registra un bloque como cuerpo dinámico (se puede mover por colisiones). */
    fun registerBlock(block: GameEntity, dynamic: Boolean = true) {
        val cx = block.x + block.width / 2
        val cy = block.y + block.height / 2
        val pos = screenToWorld(cx, cy)

        val body = Body()
        if (dynamic) {
            body.setMass(MassType.NORMAL)
        } else {
            body.setMass(MassType.INFINITE)
        }

        val halfW = block.width / 2.0 / ppm
        val halfH = block.height / 2.0 / ppm
        val shape = Rectangle(halfW * 2, halfH * 2)
        // Densidad baja para que las tablas/cubos se muevan cuando un pájaro
        // les pega (si fueran muy densos, quedarían casi inmóviles).
        val density = if (dynamic) 0.8 else 0.0
        body.addFixture(shape, density, 0.4, 0.1)
        body.translate(pos.x, pos.y)

        world.addBody(body)
        bodies[block] = body
    }

    /** Aplica un impulso de lanzamiento a un pájaro registrado. */
    fun launchBird(bird: GameEntity, vx: Double, vy: Double) {
        val body = bodies[bird] ?: return
        // Impulso = masa * velocidad deseada (dyn4j usa kg·m/s).
        // dyn4j usa Y-up, JavaFX usa Y-down → invertir vy.
        val mass = body.mass.mass
        val impulse = Vector2(mass * vx / ppm, mass * -vy / ppm)
        body.applyImpulse(impulse)
    }

    /** Marca un cuerpo como kinemático (fijo, no responde a fuerzas). */
    fun setKinematic(entity: GameEntity) {
        val body = bodies[entity] ?: return
        body.setMass(MassType.FIXED_LINEAR_VELOCITY)
    }

    /** Marca un cuerpo como dinámico. */
    fun setDynamic(entity: GameEntity) {
        val body = bodies[entity] ?: return
        body.setMass(MassType.NORMAL)
    }

    /** Mueve un cuerpo a una posición específica (para drag del pájaro). */
    fun setTransform(entity: GameEntity, sx: Double, sy: Double) {
        val body = bodies[entity] ?: return
        val pos = screenToWorld(sx, sy)
        val t = body.transform
        t.setTranslation(pos.x, pos.y)
        t.setRotation(0.0)
    }

    /** Obtiene la posición en pantalla de una entidad registrada. */
    fun getPosition(entity: GameEntity): Pair<Double, Double>? {
        val body = bodies[entity] ?: return null
        val t = body.transform
        return worldToScreen(t.translationX, t.translationY)
    }

    /** Obtiene el ángulo de rotación de una entidad (en grados). */
    fun getAngle(entity: GameEntity): Double {
        val body = bodies[entity] ?: return 0.0
        return Math.toDegrees(body.transform.rotationAngle)
    }

    /** Verifica si un cuerpo está en reposo (velocidad < umbral). */
    fun isResting(entity: GameEntity): Boolean {
        val body = bodies[entity] ?: return true
        val v = body.linearVelocity
        return v.magnitudeSquared < 0.01
    }

    /** Aumenta la velocidad del pájaro (habilidad de Chuck). */
    fun boostBirdSpeed(bird: GameEntity, multiplier: Float) {
        val body = bodies[bird] ?: return
        val v = body.linearVelocity
        val newVx = v.x * multiplier
        val newVy = v.y * multiplier
        body.linearVelocity = Vector2(newVx, newVy)
    }

    /** Genera onda expansiva en la posición del pájaro (habilidad de Bomb). */
    fun explodeAt(bird: GameEntity, radiusPx: Float, force: Float) {
        val body = bodies[bird] ?: return
        val t = body.transform
        val cx = t.translationX
        val cy = t.translationY
        val radiusM = radiusPx / ppm

        // Aplicar fuerza radial a todos los cuerpos dinámicos cercanos
        for ((entity, otherBody) in bodies) {
            if (entity === bird) continue // no afectar al propio pájaro
            if (otherBody.mass.type != MassType.NORMAL) continue // solo dinámicos

            val otherT = otherBody.transform
            val dx = otherT.translationX - cx
            val dy = otherT.translationY - cy
            val distSq = dx * dx + dy * dy
            val dist = sqrt(distSq).toDouble()

            if (dist > 0 && dist < radiusM) {
                // Fuerza inversamente proporcional a la distancia
                val falloff = 1.0 - dist / radiusM
                val fx = (dx / dist) * force * falloff / ppm
                val fy = (dy / dist) * force * falloff / ppm
                otherBody.applyImpulse(Vector2(fx, fy))
            }
        }
    }

    // ── Simulación ────────────────────────────────────────────────────

    /** Avanza la simulación con un paso fijo de tiempo y sincroniza. */
    fun step(dt: Double = 1.0 / 60.0) {
        world.step(config.velocityIterations, dt)
        syncAll()
    }

    /** Sincroniza la posición y rotación de cada entidad con su Body de dyn4j. */
    private fun syncAll() {
        for ((entity, body) in bodies) {

            val t = body.transform
            val (sx, sy) = worldToScreen(t.translationX, t.translationY)
            val drawX = sx - entity.width / 2
            val drawY = sy - entity.height / 2
            val dx = drawX - entity.x
            val dy = drawY - entity.y
            if (kotlin.math.abs(dx) > 0.01 || kotlin.math.abs(dy) > 0.01) {
                entity.moveBy(dx, dy)
            }
            // Sincronizar rotación
            val angleDeg = Math.toDegrees(t.rotationAngle)
            if (kotlin.math.abs(angleDeg) > 0.1) {
                if (entity is AbstractEntity) {
                    entity.setRotation(angleDeg)
                }
            }
        }
    }

    /** Elimina un cuerpo del mundo físico. */
    fun remove(entity: GameEntity) {
        val body = bodies.remove(entity) ?: return
        world.removeBody(body)
    }

    /** Libera todo el mundo dyn4j. */
    fun dispose() {
        for (body in bodies.values) {
            world.removeBody(body)
        }
        bodies.clear()
    }
}