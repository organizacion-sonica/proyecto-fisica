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
        // La superficie del piso queda a 'floorMargin' px del borde inferior.
        ground.translate(config.windowWidth / 2.0 / ppm, config.floorMargin / ppm - 0.5)

        world.addBody(ground)
    }

        /** Registra un pájaro como cuerpo dinámico con su masa específica. */
    fun registerBird(bird: GameEntity, mass: Float) {
        val (bx, by, radius) = when (val h = bird.hitbox) {
            is CircularHitbox -> Triple(h.centerX, h.centerY, h.radius)
            else -> {
                val cx = bird.x + bird.width / 2
                val cy = bird.y + bird.height / 2
                Triple(cx, cy, bird.width / 2.0)
            }
        }
        val pos = screenToWorld(bx, by)

        val body = Body()

        val r = radius / ppm
        val shape = Circle(r)
        // Calcular densidad para que la masa coincida: masa = densidad * área
        // Área del círculo = π * r²
        val area = Math.PI * r * r
        val density = mass / area
        body.addFixture(shape, density.toDouble(), 0.4, 0.3) // densidad, fricción, rebote
        // Use por sí: setMass tras addFixture (dyn4j 5 se corrompe si se hace antes)
        body.setMass(MassType.NORMAL) // dinámico
        body.translate(pos.x, pos.y)
        body.linearDamping = 0.1

        world.addBody(body)
        bodies[bird] = body
    }

    /** Registra un bloque como cuerpo dinámico (se puede mover por colisiones). */
    fun registerBlock(block: Block, dynamic: Boolean = true) {
        val h = block.hitbox
        var cx = block.x + block.width / 2
        var cy = block.y + block.height / 2
        var halfW = block.width / 2
        var halfH = block.height / 2
        if (h is BoxHitbox) {
            cx = h.centerX
            cy = h.centerY
            halfW = h.halfWidth
            halfH = h.halfHeight
        }
        val pos = screenToWorld(cx, cy)

        val body = Body()

        // Densidad según el material: la madera es liviana y la piedra pesada,
        // así el peso del pájaro lanzado importa al impactar (un Bomb de 10 kg
        // empuja mucho más que un Chuck de 3 kg).
        val shape = Rectangle(halfW / ppm * 2, halfH / ppm * 2)
        val density = if (dynamic) when (block.material) {
            "madera" -> config.woodDensity
            "piedra" -> config.stoneDensity
            else -> 0.8f
        } else 0.0f
        body.addFixture(shape, density.toDouble(), 0.4, 0.1)
        // OJO: setMass debe ir después de addFixture; de lo contrario dyn4j 5
        // reemplaza el body por uno estático (íntegra infinitas).
        body.setMass(if (dynamic) MassType.NORMAL else MassType.INFINITE)
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
        // El cuerpo está centrado en el centro de la hitbox, no del sprite:
        // convertir la posición de sprite a posición de hitbox.
        val spriteCx = entity.x + entity.width / 2
        val spriteCy = entity.y + entity.height / 2
        val ox = entity.hitbox.centerX - spriteCx
        val oy = entity.hitbox.centerY - spriteCy
        val pos = screenToWorld(sx + ox, sy + oy)
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
        // Si el cuerpo del pájaro todavía existe usamos su centro real; si ya
        // se frenó en el piso y se sacó del mundo, usamos su hitbox visual,
        // para que igualmente explote en el lugar donde quedó.
        val body = bodies[bird]
        val centerWorld = if (body != null) {
            Vector2(body.transform.translationX, body.transform.translationY)
        } else {
            screenToWorld(bird.hitbox.centerX, bird.hitbox.centerY)
        }
        val cx = centerWorld.x
        val cy = centerWorld.y
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
        world.step(1, dt)
        syncAll()
    }

    /** Sincroniza la posición y rotación de cada entidad con su Body de dyn4j. */
    private fun syncAll() {
        for ((entity, body) in bodies) {

            val t = body.transform
            val (sx, sy) = worldToScreen(t.translationX, t.translationY)
            // El centro del cuerpo dinámico coincide con el centro de la hitbox
            // (puede estar corrido respecto del sprite): mover el sprite para
            // que la hitbox siga al cuerpo.
            val dx = sx - entity.hitbox.centerX
            val dy = sy - entity.hitbox.centerY
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