package org.example

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World

/**
 * Adaptador entre dyn4j y el juego JavaFX. Responsabilidad única: simular
 * física y sincronizar resultados con las entidades. Coordina conversiones
 * de coordenadas: dyn4j (Y-up) ↔ JavaFX (Y-down).
 *
 * Implementa la abstracción [PhysicsWorld]: el resto del juego depende de
 * la interfaz, no de este adaptador (Inversión de Dependencias).
 */
class Box2DWorld(
    private val config: GameConfig,
    private val mapper: ScreenMapper,
    private val bodyFactory: BodyFactory,
    private val synchronizer: EntitySynchronizer,
    private val explosionForce: ExplosionForce
) : PhysicsWorld {

    private val world: World<Body> = World()

    private val registry = BodyRegistry()

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
        val ground = bodyFactory.createGroundBody()
        world.addBody(ground)
    }

    /** Registra un pájaro como cuerpo dinámico con su masa específica. */
    override fun registerBird(bird: GameEntity, mass: Float) {
        val body = bodyFactory.createBirdBody(bird, mass)
        world.addBody(body)
        registry.registerBird(bird, body)
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
        val body = bodyFactory.createStaticBoxBody(BoxSpec(centerXPx, centerYPx, widthPx, heightPx, friction, restitution))
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
        val body = bodyFactory.createStaticBoxBody(BoxSpec(centerXPx, centerYPx, widthPx, heightPx, friction, restitution))
        world.addBody(body)
        registry.registerStatic(name, body)
    }

    /** Mueve una caja estática nombrada a una nueva posición. */
    override fun moveNamedStaticBox(name: String, centerXPx: Double, centerYPx: Double) {
        val body = registry.staticBody(name) ?: return
        val pos = screenToWorld(centerXPx, centerYPx)
        val t = body.transform
        t.setTranslation(pos.x, pos.y)
        t.setRotation(0.0)
    }

    /** Aplica un impulso de lanzamiento a un pájaro registrado. */
    override fun launchBird(bird: GameEntity, vx: Double, vy: Double) {
        val body = registry.birdBody(bird) ?: return
        // Impulso = masa * velocidad deseada (dyn4j usa kg·m/s).
        // dyn4j usa Y-up, JavaFX usa Y-down → invertir vy.
        val mass = body.mass.mass
        val impulse = Vector2(mass * vx / mapper.pixelsPerMeter, mass * -vy / mapper.pixelsPerMeter)
        body.applyImpulse(impulse)
    }

    /** Marca un cuerpo como kinemático (fijo, no responde a fuerzas). */
    override fun setKinematic(entity: GameEntity) {
        val body = registry.birdBody(entity) ?: return
        body.setMass(MassType.FIXED_LINEAR_VELOCITY)
    }

    /** Marca un cuerpo como dinámico. */
    override fun setDynamic(entity: GameEntity) {
        val body = registry.birdBody(entity) ?: return
        body.setMass(MassType.NORMAL)
    }

    /** Mueve un cuerpo a una posición específica (para drag del pájaro). */
    override fun setTransform(entity: GameEntity, sx: Double, sy: Double) {
        val body = registry.birdBody(entity) ?: return
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
        val body = registry.birdBody(entity) ?: return true
        val v = body.linearVelocity
        return v.magnitudeSquared < 0.01
    }

    /** Aumenta la velocidad del pájaro (habilidad de Chuck). */
    override fun boostBirdSpeed(bird: GameEntity, multiplier: Float) {
        val body = registry.birdBody(bird) ?: return
        val v = body.linearVelocity
        body.linearVelocity = Vector2(v.x * multiplier, v.y * multiplier)
    }

    /** Genera onda expansiva en la posición del pájaro (habilidad de Bomb). */
    override fun explodeAt(bird: GameEntity, radiusPx: Float, force: Float) {
        explosionForce.apply(bird, radiusPx, force, registry.birdBodies())
    }

    /** Elimina un cuerpo del mundo físico. */
    override fun remove(entity: GameEntity) {
        val body = registry.removeBird(entity) ?: return
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
        synchronizer.sync(registry.birdBodies())
    }

    /** Libera todo el mundo dyn4j. */
    override fun dispose() {
        for (body in registry.birdBodies().values) {
            world.removeBody(body)
        }
        for (body in registry.staticBodies().values) {
            world.removeBody(body)
        }
        registry.clear()
    }
}
