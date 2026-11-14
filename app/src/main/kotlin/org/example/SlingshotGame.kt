package org.example

import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane

/**
 * Base común de todos los minijuegos con resortera (DRY): maneja la pila
 * de pájaros, la carga en la resortera, el estiramiento, el lanzamiento, el
 * game loop a 60 fps y delega la entrada de mouse/teclado en [SlingshotInput]
 * (SRP).
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
) {
    protected var physics: PhysicsWorld = physicsFactory()
    private var gameLoop: GameLoop? = null

    // Colaboradores del nivel (creados por la fábrica).
    private lateinit var slingshot: SlingshotView
    protected lateinit var launchZone: LaunchZone
    private lateinit var pile: BirdPile
    protected val entityFactory: EntityFactory get() = levelComponents.entityFactory
    private lateinit var levelComponents: SlingshotLevelComponents

    // Hitbox al ras del piso (unos píxeles), con borde rojo para verla.
    private lateinit var floorHitbox: BoxHitbox

    // Seguimiento de pájaros en vuelo/asentados (SRP).
    private lateinit var flightTracker: FlightTracker

    // Pájaros ya lanzados que siguen en vuelo (delegado al tracker).
    protected val inFlight: MutableList<Bird> get() = flightTracker.inFlight

    // Último pájaro lanzado; la habilidad especial solo aplica a él.
    protected var lastLaunched: Bird? = null

    protected lateinit var pane: Pane

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

    fun resetLevel() {
        stopPhysicsLoop()
        physics.dispose()
        physics = physicsFactory()
        lastLaunched = null
        flightTracker.clear()
        input.clear()

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

        // Hitbox al ras del piso (invisible).
        floorHitbox = SlingshotDefaults.floorHitbox(config)

        flightTracker = FlightTracker(physics, pane, floorHitbox) { onBirdSettled(it) }

        input = SlingshotInput(
            config,
            pane,
            slingshot,
            launchZone,
            physics,
            pile,
            flightTracker,
            object : SlingshotGameCallbacks {
                override fun onBirdGrabbed() {
                    this@SlingshotGame.onBirdGrabbed()
                }
                override fun onBirdLoaded() {
                    this@SlingshotGame.onBirdLoadedOntoSlingshot()
                }
                override fun onBirdSettled(bird: Bird) {
                    this@SlingshotGame.onBirdSettled(bird)
                }
                override fun onLevelReset() {
                    this@SlingshotGame.onLevelReset()
                }
                override fun onLaunch() {
                    this@SlingshotGame.onLaunch()
                }
                override fun setLastLaunchedBird(bird: Bird?) {
                    lastLaunched = bird
                }
                override fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) {
                    this@SlingshotGame.onStretchUpdate(pull, stretchPos)
                }
                override fun clearAimLayer() {
                    this@SlingshotGame.clearAimLayer()
                }
                override fun onAbilityKey() {
                    this@SlingshotGame.onAbilityKey()
                }
                override fun resetLevel() {
                    this@SlingshotGame.resetLevel()
                }
            },
            { stopPhysicsLoop(); onExitToMenu() }
        )

        setupLevel()
    }

    /** Construye lo propio del nivel (dianas, aro, marcadores...). */
    protected abstract fun setupLevel()

    /** Ajustes al reiniciar el nivel (puntaje a cero, cronómetro...). */
    protected open fun onLevelReset() { Unit }

    /** Lógica propia del nivel en cada paso fijo de física. */
    protected abstract fun onPhysicsStep(dt: Double)

    /** Se sigue estirando: efectos de ayuda de tiro (ángulo, trayectoria...). */
    protected open fun onStretchUpdate(pull: Point2D, stretchPos: Point2D) { Unit }

    /** Se suelta la resortera (antes de decidir si el tiro es válido). */
    protected open fun clearAimLayer() { Unit }

    /** Tecla G: habilidad especial del último pájaro lanzado, si la hay. */
    protected open fun onAbilityKey() { Unit }

    /** Tras lanzar un pájaro (para que cada nivel limpie su estado). */
    protected open fun onLaunch() { Unit }

    /** Un pájaro lanzado quedó en reposo (puntos de basket, etc.). */
    protected open fun onBirdSettled(bird: Bird) { Unit }

    /** Se acaba de agarrar un pájaro (de la pila o para la resortera). */
    protected open fun onBirdGrabbed() { Unit }

    /** Un pájaro quedó fijado en la resortera (momento en que arranca el timer). */
    protected open fun onBirdLoadedOntoSlingshot() { Unit }

    /** Marca un pájaro como asentado (p. ej. salió de la pantalla) para que se desvanezca. */
    protected fun markBirdSettled(bird: Bird) = flightTracker.markSettled(bird)

    // ── Game loop (60 fps fijos) ──────────────────────────────────────

    private fun startPhysicsLoop() {
        gameLoop = GameLoop(
            physics = physics,
            onStep = { dt -> onPhysicsStep(dt) },
            onRemoveSettled = { flightTracker.removeSettled() },
            onTickSettled = { dt -> flightTracker.tick(dt) }
        )
        gameLoop?.start()
    }

    private fun stopPhysicsLoop() {
        gameLoop?.stop()
        gameLoop = null
    }
}
