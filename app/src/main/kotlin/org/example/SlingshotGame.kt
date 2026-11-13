package org.example

import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.layout.Pane
import kotlin.math.min

/**
 * Base común de todos los minijuegos con resortera (DRY): maneja la pila
 * de pájaros, la carga en la resortera, el estiramiento, el lanzamiento, la
 * entrada de mouse/teclado y el game loop a 60 fps.
 *
 * Las subclases solo implementan lo propio del nivel (SRP):
 *  - [setupLevel]: qué aparece en la escena, además del fondo y la resortera.
 *  - [onPhysicsStep]: qué pasa en cada paso fijo de física.
 *  - hooks opcionales (habilidad especial, avisos de pájaro asentado, etc.)
 */
abstract class SlingshotGame(
    protected val spriteSource: SpriteSource,
    protected val config: GameConfig,
    /** Callback para volver al menú principal (tecla ESC). */
    private val onExitToMenu: () -> Unit = { Unit },
    /** Fábrica del mundo físico (Inversión de Dependencias: un test podría simularlo). */
    private val physicsFactory: () -> PhysicsWorld = SlingshotDefaults.physicsFactory(config),
    /** Fábrica de los colaboradores del nivel con resortera (DIP). */
    private val levelFactory: (Pane, PhysicsWorld) -> SlingshotLevelComponents = SlingshotDefaults.levelFactory(spriteSource, config)
) : SlingshotGameCallbacks {
    protected var physics: PhysicsWorld = physicsFactory()
    private var gameLoop: GameLoop? = null

    // Colaboradores del nivel (creados por la fábrica).
    private lateinit var slingshot: SlingshotView
    protected lateinit var launchZone: LaunchZone
    private lateinit var pile: BirdPile
    protected val entityFactory: EntityFactory get() = levelComponents.entityFactory
    private lateinit var levelComponents: SlingshotLevelComponents

    // Hitbox al raz del piso (unos píxeles), con borde rojo para verla.
    private lateinit var floorHitbox: BoxHitbox
    // Seguimiento de pájaros en vuelo/asentados (SRP).
    private lateinit var flightTracker: FlightTracker

    // Pájaros ya lanzados que siguen en vuelo (delegado al tracker).
    protected val inFlight: MutableList<Bird> get() = flightTracker.inFlight

    // Último pájaro lanzado; la habilidad especial solo aplica a él.
    protected var lastLaunched: Bird? = null

    protected lateinit var pane: Pane

    /** Manejador de entrada de mouse/teclado (SRP). */
    private lateinit var input: SlingshotInput

    // ── Construcción de la escena ─────────────────────────────────────

    fun buildScene(): Scene {
        pane = Pane()
        pane.prefWidth = config.windowWidth
        pane.prefHeight = config.windowHeight
        pane.isFocusTraversable = true

        setupWorld()
        startPhysicsLoop()

        val scene = Scene(pane, config.windowWidth, config.windowHeight)
        scene.setOnKeyPressed { e -> input.handleKeyPressed(e) }
        scene.setOnMousePressed { e -> input.handlePressed(e) }
        scene.setOnMouseDragged { e -> input.handleDragged(e) }
        scene.setOnMouseReleased { e -> input.handleReleased(e) }
        return scene
    }

    override fun resetLevel() {
        stopPhysicsLoop()
        physics.dispose()
        physics = physicsFactory()
        lastLaunched = null
        flightTracker.clear()

        pane.children.clear()
        setupWorld()
        onLevelReset()
        startPhysicsLoop()
    }

    /** Fondo + resortera + zona de lanzamiento, luego lo propio del nivel. */
    private fun setupWorld() {
        levelComponents = levelFactory(pane, physics)
        slingshot = levelComponents.slingshot
        launchZone = levelComponents.launchZone
        pile = levelComponents.pile

        // Hitbox al raz del piso (invisible).
        floorHitbox = SlingshotDefaults.floorHitbox(config)

        flightTracker = FlightTracker(physics, pane, floorHitbox) { onBirdSettled(it) }

        setupLevel()

        input = SlingshotInput(
            config = config,
            pane = pane,
            slingshot = slingshot,
            launchZone = launchZone,
            physics = physics,
            pile = pile,
            flightTracker = flightTracker,
            callbacks = this,
            onExitToMenu = onExitToMenu
        )
    }

    /** Construye lo propio del nivel (dianas, aro, marcadores...). */
    protected abstract fun setupLevel()

    /** Ajustes al reiniciar el nivel (puntaje a cero, cronómetro...). */
    override fun onLevelReset() { Unit }

    /** Lógica propia del nivel en cada paso fijo de física. */
    protected abstract fun onPhysicsStep(dt: Double)

    /** Se sigue estirando: efectos de ayuda de tiro (ángulo, trayectoria...). */
    override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) { Unit }

    /** Se suelta la resortera (antes de decidir si el tiro es válido). */
    override fun clearAimLayer() { Unit }

    /** Tecla G: habilidad especial del último pájaro lanzado, si la hay. */
    override fun onAbilityKey() { Unit }

    /** Tras lanzar un pájaro (para que cada nivel limpie su estado). */
    override fun onLaunch() { Unit }

    /** Un pájaro lanzado quedó en reposo (puntos de basket, etc.). */
    override fun onBirdSettled(bird: Bird) { Unit }

    /** Se acaba de agarrar un pájaro (de la pila o para la resortera). */
    override fun onBirdGrabbed() { Unit }

    /** Un pájaro quedó fijado en la resortera (momento en que arranca el timer). */
    override fun onBirdLoaded() { Unit }

    override fun setLastLaunchedBird(bird: Bird?) {
        lastLaunched = bird
    }

    // ── Hooks para subclases ──────────────────────────────────────

    private fun startPhysicsLoop() {
        gameLoop = GameLoop(
            physics = physics,
            onStep = { dt -> onPhysicsStep(dt) },
            onRemoveSettled = { removeSettledBirds() },
            onTickSettled = { dt -> flightTracker.tick(dt) }
        )
        gameLoop?.start()
    }

    private fun stopPhysicsLoop() {
        gameLoop?.stop()
        gameLoop = null
    }

    private fun removeSettledBirds() {
        flightTracker.removeSettled()
    }

    /** Marca un pájaro como asentado (p. ej. salió de la pantalla) para que se desvanezca. */
    protected fun markBirdSettled(bird: Bird) = flightTracker.markSettled(bird)

    protected companion object {
        /** Estiramiento mínimo para considerarlo un tiro válido. */
        const val MIN_STRETCH = 10.0

        /** Segundos que un pájaro queda en el piso antes de desvanecerse. */
        const val SETTLE_FADE_START = 0.0
    }
}
