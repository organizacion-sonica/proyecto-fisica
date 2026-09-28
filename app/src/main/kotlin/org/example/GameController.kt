package org.example

import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import kotlin.math.min

/**
 * Orquesta el juego: crea la escena, dispone bloques y pájaros, conecta la
 * entrada del mouse/teclado con la [SlingshotView] y traduce el estiramiento en el
 * lanzamiento del pájaro activo dentro de [Box2DWorld].
 *
 * Responsabilidades:
 *  - Composición de la escena (fondo, resortera, entidades)
 *  - Pájaros infinitos: los 3 tipos viven en la esquina superior izquierda.
 *    Se arrastran directamente a la resortera. Al soltar sobre la zona se
 *    cargan, al soltar fuera vuelven a su posición original.
 *  - Resortera: el pájaro cargado se mueve con el pull (estiramiento visual).
 *    La animación de la resortera avanza al estirar y se revuelve al soltar.
 *  - Input del mouse (arrastre de pájaros, estiramiento de resortera) y
 *    teclado (habilidades G, reset R) a nivel de escena
 *  - Game loop (AnimationTimer → Box2DWorld.step) a paso fijo de 60fps
 *  - Reset del nivel con la tecla R
 */
class GameController(
    private val spriteSource: SpriteSource,
    private val config: GameConfig
) {
    private val floorY: Double = config.windowHeight - config.floorMargin
    private var physics = Box2DWorld(config)
    private val entityFactory = EntityFactory(spriteSource, config)

    private val slotTypes = listOf("red", "chuck", "bomb")

    // Pájaros en la pila de la esquina superior izquierda (suministro infinito).
    private val slotBirds = mutableListOf<Bird>()
    private val birdType = mutableMapOf<Bird, String>()

    // Pájaro que se está arrastrando desde la pila hacia la resortera
    private var dragBird: Bird? = null
    private var dragOrigin = Point2D.ZERO  // posición original del pájaro en la pila

    // Pájaro fijado en la resortera (listo para estirar)
    private var slingshotBird: Bird? = null
    // Pájaros ya lanzados que siguen en vuelo
    private val inFlight = mutableListOf<Bird>()
    private var lastLaunched: Bird? = null

    private lateinit var slingshot: SlingshotView
    private lateinit var launchZone: LaunchZone
    private lateinit var pane: Pane

    private var physicsTimer: AnimationTimer? = null
    private val grabRadius = 45.0

    fun buildScene(): Scene {
        pane = Pane()
        pane.prefWidth = config.windowWidth
        pane.prefHeight = config.windowHeight
        pane.isFocusTraversable = true

        setupWorld()
        startPhysicsLoop()

        val scene = Scene(pane, config.windowWidth, config.windowHeight)
        scene.setOnKeyPressed { e -> handleKeyPressed(e) }
        scene.setOnMousePressed { e -> handlePressed(e) }
        scene.setOnMouseDragged { e -> handleDragged(e) }
        scene.setOnMouseReleased { e -> handleReleased(e) }
        return scene
    }

    fun resetLevel() {
        physics.dispose()
        physics = Box2DWorld(config)
        slotBirds.clear()
        birdType.clear()
        dragBird = null
        inFlight.clear()
        lastLaunched = null
        slingshotBird = null

        pane.children.clear()
        setupWorld()
    }

    // ── Construcción de la escena ───────────────────────────────────────

    private fun setupWorld() {
        BackgroundView(spriteSource, config).placeIn(pane)

        slingshot = SlingshotView(spriteSource, config)
        slingshot.placeIn(pane)

        launchZone = LaunchZone(config, slingshot.anchor.x, slingshot.anchor.y)
        launchZone.placeIn(pane)

        createBlocks().forEach { block ->
            physics.registerBlock(block, dynamic = true)
        }

        createSlotBirds()
    }

    private fun createSlotBirds() {
        slotTypes.forEach { type -> spawnSlotBird(type) }
    }

    /** Coordenada (esq. sup. izq.) del cubito para el tipo [type]. */
    private fun slotCoord(index: Int): Pair<Double, Double> {
        val gap = 8.0
        return (20.0 to 20.0 + index * (config.birdSize + gap))
    }

    /** Crea un pájaro del [type] como visual puro en la pila (sin física). */
    private fun spawnSlotBird(type: String) {
        val index = slotTypes.indexOf(type)
        val bird = entityFactory.createBird(type)
        val (sx, sy) = slotCoord(index)
        bird.placeAt(pane, sx, sy)
        slotBirds += bird
        birdType[bird] = type
    }

    private fun massOf(bird: Bird): Float = when (bird) {
        is Red -> config.redMass
        is Chuck -> config.chuckMass
        is Bomb -> config.bombMass
        else -> 5.0f
    }

    private fun createBlocks(): List<Block> {
        val blocks = mutableListOf<Block>()
        val cubeSize = config.blockSize
        val tablaSize = config.tablaSize
        val baseX = config.windowWidth * 0.62
        val spacing = cubeSize * 1.1

        val cuboBaseIzq = entityFactory.createBlock("cubo", "madera")
        cuboBaseIzq.placeAt(pane, baseX, floorY - cubeSize)
        blocks += cuboBaseIzq

        val cuboBaseDer = entityFactory.createBlock("cubo", "madera")
        cuboBaseDer.placeAt(pane, baseX + spacing, floorY - cubeSize)
        blocks += cuboBaseDer

        val paredIzq = entityFactory.createBlock("tabla", "madera")
        paredIzq.placeAt(pane, baseX + cubeSize * 0.05, floorY - cubeSize - tablaSize)
        blocks += paredIzq

        val paredDer = entityFactory.createBlock("tabla", "madera")
        paredDer.placeAt(pane, baseX + spacing + cubeSize * 0.05, floorY - cubeSize - tablaSize)
        blocks += paredDer

        val techoIzq = entityFactory.createBlock("tabla", "piedra")
        techoIzq.placeAt(pane, baseX + cubeSize * 0.0, floorY - cubeSize - tablaSize - cubeSize * 0.3)
        blocks += techoIzq

        val techoDer = entityFactory.createBlock("tabla", "piedra")
        techoDer.placeAt(pane, baseX + spacing + cubeSize * 0.0, floorY - cubeSize - tablaSize - cubeSize * 0.3)
        blocks += techoDer

        val punta = entityFactory.createBlock("cubo", "piedra")
        punta.placeAt(pane, baseX + spacing * 0.5 - cubeSize * 0.5, floorY - cubeSize - tablaSize - tablaSize + cubeSize * 0.3)
        blocks += punta

        return blocks
    }

    // ── Gestión de pájaros ──────────────────────────────────────────────

    private fun isNearBird(bird: Bird, x: Double, y: Double): Boolean {
        val cx = bird.x + bird.width / 2
        val cy = bird.y + bird.height / 2
        return Point2D(x, y).distance(cx, cy) < grabRadius
    }

    /**
     * Devuelve el pájaro de la pila a su posición original.
     */
    private fun returnBirdToSlot(bird: Bird) {
        val type = birdType[bird] ?: return
        val index = slotTypes.indexOf(type)
        val (sx, sy) = slotCoord(index)
        bird.placeAt(pane, sx, sy)
    }

    // ── Input ───────────────────────────────────────────────────────────

    private fun handlePressed(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // 1) Si hay un pájaro fijado en la resortera → activar estiramiento
        //    (sin verificar distancia: cualquier clic activa la resortera)
        if (slingshotBird != null) {
            slingshot.forceStartDrag()
            return
        }

        // 2) Agarrar un pájaro de la pilar → empezar arrastre directo
        val bird = slotBirds.firstOrNull { isNearBird(it, mx, my) }
        if (bird != null) {
            dragBird = bird
            dragOrigin = Point2D(bird.x, bird.y)
            bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
        }
    }

    private fun handleDragged(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // Estiramiento de la resortera: el pájaro va con el pull
        if (slingshot.isDragging) {
            val loaded = slingshotBird ?: return
            slingshot.drag(mx, my)
            loaded.placeAt(pane, mx - loaded.width / 2, my - loaded.height / 2)
            physics.setTransform(loaded, mx, my)
            return
        }

        // Arrastre directo del pájaro desde la pila
        val bird = dragBird ?: return
        bird.placeAt(pane, mx - bird.width / 2, my - bird.height / 2)
    }

    private fun handleReleased(e: MouseEvent) {
        val mx = e.sceneX
        val my = e.sceneY

        // Soltar la resortera → lanzar si hay estiramiento suficiente
        if (slingshot.isDragging) {
            val loaded = slingshotBird ?: return
            val pull = slingshot.release() ?: return
            if (pull.magnitude() > 10.0) {
                launchBird(loaded, pull)
            } else {
                // Estiramiento insuficiente, devolver el pájaro a la resortera
                loaded.placeAt(pane, slingshot.anchor.x - loaded.width / 2, slingshot.anchor.y - loaded.height / 2)
                physics.setTransform(loaded, slingshot.anchor.x, slingshot.anchor.y)
            }
            return
        }

        // Soltar pájaro arrastrado desde la pila
        val bird = dragBird
        if (bird != null) {
            dragBird = null
            if (launchZone.contains(mx, my)) {
                // Soltado sobre la zona → cargar en la resortera
                loadBirdOntoSlingshot(bird)
            } else {
                // Soltado fuera → devolver a la pila
                returnBirdToSlot(bird)
            }
        }
    }

    private fun handleKeyPressed(e: KeyEvent) {
        when (e.code) {
            KeyCode.G -> useSpecialAbility()
            KeyCode.R -> resetLevel()
            else -> {}
        }
    }

    private fun useSpecialAbility() {
        val bird = lastLaunched ?: return
        when (bird) {
            is Bomb -> bird.explode(physics, config)
            is Chuck -> bird.boostSpeed(config.chuckSpeedBoost, physics)
            else -> {}
        }
    }

    // ── Lanzamiento ─────────────────────────────────────────────────────

    /**
     * Carga el pájaro en la resortera: lo posiciona en el ancla, lo registra
     * como cuerpo kinemático y lo prepara para estirar. El slot se repone
     * al instante con un pájaro del mismo tipo (suministro infinito).
     */
    private fun loadBirdOntoSlingshot(bird: Bird) {
        val type = birdType[bird] ?: slotTypes[0]

        // Si ya había un pájaro fijado, se descarta
        val old = slingshotBird
        if (old != null) {
            physics.remove(old)
            old.view.removeFrom(pane)
            slingshotBird = null
        }

        // Mover el pájaro al ancla de la resortera y registrar física
        bird.placeAt(pane, slingshot.anchor.x - bird.width / 2, slingshot.anchor.y - bird.height / 2)
        physics.registerBird(bird, massOf(bird))
        physics.setKinematic(bird)
        physics.setTransform(bird, slingshot.anchor.x, slingshot.anchor.y)
        slingshotBird = bird

        // Reponer el slot con un pájaro fresco del mismo tipo
        slotBirds.remove(bird)
        spawnSlotBird(type)
    }

    /**
     * Lanza el pájaro desde su posición actual (donde el usuario lo dejó
     * al estirar). El impulso es proporcional a la dirección del pull (el
     * pájaro sale en dirección contraria a donde se estiró la resortera).
     */
    private fun launchBird(bird: Bird, pull: Point2D) {
        slingshotBird = null
        inFlight += bird
        lastLaunched = bird

        val mag = min(pull.magnitude(), config.maxStretch)
        val ratio = mag / config.maxStretch
        val speed = ratio * config.maxLaunchSpeed

        val vx = pull.x / mag * speed
        val vy = pull.y / mag * speed

        physics.setDynamic(bird)
        physics.launchBird(bird, vx, vy)
    }

    // ── Game Loop (60 fps fijos) ────────────────────────────────────────

    private fun startPhysicsLoop() {
        var lastTime = 0L
        var accumulator = 0.0
        val fixedDt = 1.0 / 60.0

        physicsTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (lastTime == 0L) {
                    lastTime = now
                    return
                }
                val elapsed = (now - lastTime) / 1_000_000_000.0
                lastTime = now
                accumulator += elapsed.coerceAtMost(0.1)

                while (accumulator >= fixedDt) {
                    physics.step(fixedDt)
                    accumulator -= fixedDt
                }

                // Limpiar pájaros lanzados que ya se detuvieron
                val resting = mutableListOf<Bird>()
                for (f in inFlight) {
                    if (physics.isResting(f)) {
                        physics.remove(f)
                        resting += f
                    }
                }
                inFlight.removeAll(resting)
                if (lastLaunched in resting) lastLaunched = null
            }
        }.also { it.start() }
    }

    fun stopPhysicsLoop() {
        physicsTimer?.stop()
    }
}
