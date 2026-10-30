package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World
import kotlin.math.sqrt

/**
 * Adaptador entre dyn4j y el juego JavaFX. Responsabilidad única: simular
 * física y sincronizar resultados con las entidades. Coordina conversiones
 * de coordenadas: dyn4j (Y-up) ↔ JavaFX (Y-down).
 *
 * Implementa la abstracción [PhysicsWorld]: el resto del juego depende de
 * la interfaz, no de este adaptador (Inversión de Dependencias).
 */
class Box2DWorld(private val config: GameConfig) : PhysicsWorld {

    private val mapper = ScreenMapper(config)
    private val world: World<Body> = World()

    private val bodies = mutableMapOf<GameEntity, Body>()
    private val namedBodies = mutableMapOf<String, Body>()

    init {
        world.setGravity(0.0, -config.gravity.toDouble())
        createGround()
    }

    // ── Conversiones de coordenadas (delegadas a ScreenMapper) ─────────

    /** Pantalla (px, Y-down) → dyn4j (m, Y-up). */
    fun screenToWorld(sx: Double, sy: Double): Vector2 = mapper.toWorld(sx, sy)

    /** dyn4j (m, Y-up) → Pantalla (px, Y-down). */
    fun worldToScreen(wx: Double, wy: Double): Pair<Double, Double> = mapper.toScreen(wx, wy)

    // ── Cuerpos ────────────────────────────────────────────────────────

    /** Crea el piso estático en el borde inferior de la pantalla. */
    private fun createGround() {
        val ground = Body()
        ground.setMass(MassType.INFINITE) // estático

        val groundWidth = config.windowWidth / mapper.pixelsPerMeter * 2
        val groundShape = Rectangle(groundWidth, 1.0)
        ground.addFixture(groundShape, 0.0, 0.0, 0.0)
        // La superficie del piso queda a 'floorMargin' px del borde inferior.
        ground.translate(config.windowWidth / 2.0 / mapper.pixelsPerMeter, config.floorMargin / mapper.pixelsPerMeter - 0.5)

        world.addBody(ground)
    }

    /** Registra un pájaro como cuerpo dinámico con su masa específica. */
    override fun registerBird(bird: GameEntity, mass: Float) {
        val (bx, by, radius) = bird.hitbox.circleSpec()
        val pos = screenToWorld(bx, by)

        val body = Body()
        val shape = Circle(radius / mapper.pixelsPerMeter)
        // Calcular densidad para que la masa coincida: masa = densidad * área
        // Área del círculo = π * r²
        val density = mass / (Math.PI * (radius / mapper.pixelsPerMeter) * (radius / mapper.pixelsPerMeter))
        body.addFixture(shape, density, 0.4, 0.3) // densidad, fricción, rebote
        // Use por sí: setMass tras addFixture (dyn4j 5 se corrompe si se hace antes)
        body.setMass(MassType.NORMAL) // dinámico
        body.translate(pos.x, pos.y)
        body.linearDamping = 0.1

        world.addBody(body)
        bodies[bird] = body
    }

    /** Caja estática (en px de pantalla) para que los cuerpos reboten. */
    override fun addStaticBox(
        centerXPx: Double,
        centerYPx: Double,
        widthPx: Double,
        heightPx: Double,
        friction: Double,
        restitution: Double
    ) {
        val body = Body()
        val shape = Rectangle(widthPx / mapper.pixelsPerMeter, heightPx / mapper.pixelsPerMeter)
        body.addFixture(shape, 0.0, friction, restitution)
        // Use por sí: setMass tras addFixture (dyn4j 5 se corrompe si se hace antes)
        body.setMass(MassType.INFINITE)
        val pos = screenToWorld(centerXPx, centerYPx)
        body.translate(pos.x, pos.y)
        world.addBody(body)
    }

    /** Caja estática con nombre, para poder moverla después (backboard del aro). */
    override fun addNamedStaticBox(
        name: String,
        centerXPx: Double,
        centerYPx: Double,
        widthPx: Double,
        heightPx: Double,
        friction: Double,
        restitution: Double
    ) {
        val body = Body()
        val shape = Rectangle(widthPx / mapper.pixelsPerMeter, heightPx / mapper.pixelsPerMeter)
        body.addFixture(shape, 0.0, friction, restitution)
        body.setMass(MassType.INFINITE)
        val pos = screenToWorld(centerXPx, centerYPx)
        body.translate(pos.x, pos.y)
        world.addBody(body)
        namedBodies[name] = body
    }

    /** Mueve una caja estática nombrada a una nueva posición. */
    override fun moveNamedStaticBox(name: String, centerXPx: Double, centerYPx: Double) {
        val body = namedBodies[name] ?: return
        val pos = screenToWorld(centerXPx, centerYPx)
        val t = body.transform
        t.setTranslation(pos.x, pos.y)
        t.setRotation(0.0)
    }

    /** Aplica un impulso de lanzamiento a un pájaro registrado. */
    override fun launchBird(bird: GameEntity, vx: Double, vy: Double) {
        val body = bodies[bird] ?: return
        // Impulso = masa * velocidad deseada (dyn4j usa kg·m/s).
        // dyn4j usa Y-up, JavaFX usa Y-down → invertir vy.
        val mass = body.mass.mass
        val impulse = Vector2(mass * vx / mapper.pixelsPerMeter, mass * -vy / mapper.pixelsPerMeter)
        body.applyImpulse(impulse)
    }

    /** Marca un cuerpo como kinemático (fijo, no responde a fuerzas). */
    override fun setKinematic(entity: GameEntity) {
        val body = bodies[entity] ?: return
        body.setMass(MassType.FIXED_LINEAR_VELOCITY)
    }

    /** Marca un cuerpo como dinámico. */
    override fun setDynamic(entity: GameEntity) {
        val body = bodies[entity] ?: return
        body.setMass(MassType.NORMAL)
    }

    /** Mueve un cuerpo a una posición específica (para drag del pájaro). */
    override fun setTransform(entity: GameEntity, sx: Double, sy: Double) {
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

    /** Verifica si un cuerpo está en reposo (velocidad < umbral). */
    override fun isResting(entity: GameEntity): Boolean {
        val body = bodies[entity] ?: return true
        val v = body.linearVelocity
        return v.magnitudeSquared < 0.01
    }

    /** Aumenta la velocidad del pájaro (habilidad de Chuck). */
    override fun boostBirdSpeed(bird: GameEntity, multiplier: Float) {
        val body = bodies[bird] ?: return
        val v = body.linearVelocity
        body.linearVelocity = Vector2(v.x * multiplier, v.y * multiplier)
    }

    /** Genera onda expansiva en la posición del pájaro (habilidad de Bomb). */
    override fun explodeAt(bird: GameEntity, radiusPx: Float, force: Float) {
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
        val radiusM = radiusPx / mapper.pixelsPerMeter

        // Aplicar fuerza radial a todos los cuerpos dinámicos cercanos
        for ((entity, otherBody) in bodies) {
            if (entity === bird) continue // no afectar al propio pájaro
            if (otherBody.mass.type != MassType.NORMAL) continue // solo dinámicos

            val otherT = otherBody.transform
            val dx = otherT.translationX - cx
            val dy = otherT.translationY - cy
            val distSq = dx * dx + dy * dy
            val dist = sqrt(distSq)

            if (dist > 0 && dist < radiusM) {
                // Fuerza inversamente proporcional a la distancia
                val falloff = 1.0 - dist / radiusM
                val fx = (dx / dist) * force * falloff / mapper.pixelsPerMeter
                val fy = (dy / dist) * force * falloff / mapper.pixelsPerMeter
                otherBody.applyImpulse(Vector2(fx, fy))
            }
        }
    }

    /** Elimina un cuerpo del mundo físico. */
    override fun remove(entity: GameEntity) {
        val body = bodies.remove(entity) ?: return
        world.removeBody(body)
    }

    // ── Simulación ────────────────────────────────────────────────────

    /** Avanza la simulación con un paso fijo de tiempo y sincroniza. */
    override fun step(dt: Double) {
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
                entity.setRotation(angleDeg)
            }
        }
    }

    /** Libera todo el mundo dyn4j. */
    override fun dispose() {
        for (body in bodies.values) {
            world.removeBody(body)
        }
        for (body in namedBodies.values) {
            world.removeBody(body)
        }
        bodies.clear()
        namedBodies.clear()
    }
}
