package org.example

import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.scene.control.Label
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
    private lateinit var angleLabel: javafx.scene.text.Text
    private lateinit var trajectoryLayer: javafx.scene.layout.Pane

    private var physicsTimer: AnimationTimer? = null
    private val grabRadius = 45.0 * config.scale

    // ── Díaz de práctica: puntos, tiempo y objetivos ─────────────────────
    private var score = 0
    private var timeLeft = 105.0          // segundos restantes
    private var timeOver = false
    private var globalTime = 0.0          // segundos que pasaron (para la sinusoide de las dianas)
    private val targets = mutableListOf<Target>()
    private lateinit var scoreLabel: Label
    private lateinit var timerLabel: Label
    private lateinit var explosionPlayer: ExplosionPlayer

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
        physicsTimer?.stop()
        startPhysicsLoop()
    }

    // ── Construcción de la escena ───────────────────────────────────────

    private fun setupWorld() {
        BackgroundView(spriteSource, config).placeIn(pane)

        slingshot = SlingshotView(spriteSource, config)
        slingshot.placeIn(pane)

        launchZone = LaunchZone(config, slingshot.anchor.x, slingshot.anchor.y)
        launchZone.placeIn(pane)

        angleLabel = javafx.scene.text.Text(config.windowWidth - 340.0 * config.scale, 60.0 * config.scale, "Ángulo de tiro: —")
        angleLabel.style = "-fx-font-size: ${18 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
        pane.children.add(angleLabel)

        trajectoryLayer = javafx.scene.layout.Pane()
        pane.children.add(trajectoryLayer)

        explosionPlayer = ExplosionPlayer(pane, spriteSource, config)

        // Rótulos de puntos y tiempo
        scoreLabel = Label("Puntos: 0").apply {
            style = "-fx-font-size: ${20 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
            layoutX = windowCenter()
            layoutY = 40.0 * config.scale
        }
        timerLabel = Label("Tiempo: 01:45").apply {
            style = "-fx-font-size: ${20 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
            layoutY = 72.0 * config.scale
        }
        pane.children.addAll(scoreLabel, timerLabel)

        setupTargets()
        createSlotBirds()
    }

    private fun windowCenter(): Double = config.windowWidth / 2 - 60 * config.scale

    /** Crea la diana fija con su barrote y las dianas móviles. */
    private fun setupTargets() {
        targets.clear()
        score = 0
        timeLeft = 105.0
        timeOver = false
        globalTime = 0.0

        // --- Diana fija grande con barrote de hierro, a la derecha ---
        val ddSize = 150.0 * config.scale
        val ddX = config.windowWidth * 0.85
        val ddY = config.windowHeight * 0.52
        val fixed = Target(
            view = EntityView(spriteSource, "diana.png", ddSize, CircularHitbox(ddSize * 0.42), true),
            cx = ddX,
            baseY = ddY,
            amp = 0.0,
            speed = 0.0,
            radius = ddSize * 0.42,
            size = ddSize,
            hit = false,
            alive = true
        )
        fixed.view.placeAt(pane, ddX - ddSize / 2, ddY - ddSize / 2)
        targets += fixed
        // Barrote de hierro: poste desde la diana hasta el suelo
        pane.children.add(javafx.scene.shape.Rectangle(ddX - 6 * config.scale, ddY + ddSize / 2, 12 * config.scale, config.windowHeight - (ddY + ddSize / 2)).apply {
            fill = javafx.scene.paint.Color.DARKSLATEGRAY
        })

        // --- Dianas móviles que suben y bajan ---
        val positions = listOf(
            listOf(config.windowWidth * 0.38, config.windowHeight * 0.35, 60.0, 0.9),
            listOf(config.windowWidth * 0.50, config.windowHeight * 0.55, 80.0, 1.2),
            listOf(config.windowWidth * 0.63, config.windowHeight * 0.30, 70.0, 1.6),
            listOf(config.windowWidth * 0.72, config.windowHeight * 0.60, 55.0, 1.0),
            listOf(config.windowWidth * 0.44, config.windowHeight * 0.72, 65.0, 1.3)
        )
        for (p in positions) {
            val cx = p[0] as Double
            val cy = p[1] as Double
            val amp = p[2] as Double
            val spd = p[3] as Double
            val size = 90.0 * config.scale
            val t = Target(
                view = EntityView(spriteSource, "diana.png", size, CircularHitbox(size * 0.42), true),
                cx = cx,
                baseY = cy,
                amp = amp * config.scale,
                speed = spd,
                radius = size * 0.42,
                size = size,
                hit = false,
                alive = true
            )
            t.view.placeAt(pane, cx - size / 2, cy - size / 2)
            targets += t
        }
    }

    /** Actualiza las dianas (sinusoide), las colisiones y el tiempo. */
    private fun updateTargets(dtStep: Double) {
        globalTime += dtStep
        if (!timeOver) {
            timeLeft -= dtStep
            val secs = kotlin.math.max(0.0, timeLeft).toInt()
            timerLabel.text = "Tiempo: %02d:%02d".format(secs / 60, secs % 60)
            if (timeLeft <= 0.0) {
                timeOver = true
                physicsTimer?.stop()
                showRankScreen()
            }
        }

        // Movimiento de las dianas móviles y respawn de las derrotadas
        for (t in targets) {
            if (!t.alive) {
                if (globalTime >= t.respawnAt) {
                    t.alive = true
                    t.hit = false
                    val newCx = t.cx
                    val newCy = t.cy(globalTime)
                    t.view.moveBy(newCx - t.size / 2 - t.view.x, newCy - t.size / 2 - t.view.y)
                    t.view.imageView.isVisible = true
                }
                continue
            }
            val cy = t.cy(globalTime)
            t.view.moveBy(t.cx - t.size / 2 - t.view.x, cy - t.size / 2 - t.view.y)
        }

        checkBirdTargetCollisions()
    }

    /** Pantalla final: fondo, los 3 pájaros en horizontal, el rango y el puntaje. */
    private fun showRankScreen() {
        pane.children.clear()
        BackgroundView(spriteSource, config).placeIn(pane)

        // Imagen de rango del assets con el score a bajo...
        val rankImg = spriteSource.load("${RankEvaluator.rankFor(score)} rank.png")
        val rankSize = 200.0 * config.scale
        val rankView = javafx.scene.image.ImageView(rankImg).apply {
            isPreserveRatio = true
            fitHeight = rankSize
            layoutX = config.windowWidth / 2 - rankSize / 2
            layoutY = config.windowHeight * 0.12
        }
        pane.children.add(rankView)

        val title = Label("Rango obtenido").apply {
            style = "-fx-font-size: ${28 * config.scale}px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
            layoutY = config.windowHeight * 0.10
            layoutX = config.windowWidth / 2 - 100 * config.scale
        }
        pane.children.add(title)

        val scoreText = Label("Puntaje: $score").apply {
            style = "-fx-font-size: ${32 * config.scale}px; -fx-fill: gold; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
            layoutY = config.windowHeight * 0.12 + rankSize + 20 * config.scale
            layoutX = config.windowWidth / 2 - 120 * config.scale
        }
        pane.children.add(scoreText)

        // Los 3 pájaros en fila horizontal, un poco a la izquierda de pantalla
        // y del mismo tamaño que en el juego (solo sprites, sin hitbox).
        val birdPng = mapOf("red" to "Red.png", "chuck" to "chuck .png", "bomb" to "bomb.png")
        val size = config.birdSize
        val gapBetween = 20.0 * config.scale
        val totalW = slotTypes.size * size + (slotTypes.size - 1) * gapBetween
        val startX = 40.0 * config.scale
        val yPos = config.windowHeight * 0.62
        slotTypes.forEachIndexed { i, type ->
            val iv = javafx.scene.image.ImageView(spriteSource.load(birdPng[type]!!)).apply {
                isPreserveRatio = true
                fitWidth = size
                fitHeight = size
                layoutX = startX + i * (size + gapBetween)
                layoutY = yPos
            }
            pane.children.add(iv)
        }
    }

    /** Si algún pájaro en vuelo choca a una diana viva, suma un punto. */
    private fun checkBirdTargetCollisions() {
        if (timeOver) return
        // Solo pájaros que vuelan (cuerpo dinámico fuera de la resortera)
        val birdsColliding = mutableListOf<Bird>()
        for (bird in inFlight) {
            val birdRadius = when (val h = bird.hitbox) {
                is CircularHitbox -> h.radius
                else -> bird.width / 2
            }
            for (t in targets) {
                if (!t.alive || t.hit) continue
                val bx = bird.hitbox.centerX
                val by = bird.hitbox.centerY
                val cy = t.cy(globalTime)
                val dx = bx - t.cx
                val dy = by - cy
                if (dx * dx + dy * dy < (birdRadius + t.radius) * (birdRadius + t.radius)) {
                    score++
                    scoreLabel.text = "Puntos: $score"
                    t.hit = true
                    t.alive = false
                    t.respawnAt = globalTime + 2.0
                    t.view.imageView.isVisible = false
                }
            }
        }
    }

    /** Mueve la vista y el rectángulo overlay de la hitbox. */
    private fun Target.viewCenterY() = baseY + amp * kotlin.math.sin(speed * globalTime)

    private fun createSlotBirds() {
        slotTypes.forEach { type -> spawnSlotBird(type) }
    }

    /** Coordenada (esq. sup. izq.) del cubito para el tipo [type]. */
    private fun slotCoord(index: Int): Pair<Double, Double> {
        val gap = 8.0 * config.scale
        return (20.0 * config.scale to 20.0 * config.scale + index * (config.birdSize + gap))
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

    /**
     * Pone el bloque de modo que el CENTRO DE SU HITBOX (la parte visible
     * del sprite, no el rectángulo transparente) quede en ([cx], [cy]).
     * Esto apila los cubos/tablas respetando la física real.
     */
    private fun placeByHitboxCenter(block: Block, cx: Double, cy: Double) {
        block.placeAt(pane, 0.0, 0.0)
        block.placeAt(pane, cx - block.hitbox.centerX, cy - block.hitbox.centerY)
    }

    /** Construye la pequeña casita: dos columnas de cubo + tabla + cubo. */
    private fun createBlocks(): List<Block> {
        val blocks = mutableListOf<Block>()
        val s = config.blockSize
        val t = config.tablaSize
        val baseX = config.windowWidth * 0.62

        // Medidas de las partes visibles (mitad de la hitbox, en px):
        val cubeDieHalf = 0.11 * s   // cubo: hitbox ≈ 0.22 * s de lado
        val wallHalfW = 0.06 * t     // tabla: hitbox ≈ 0.12 * t de ancho
        val wallHalfH = 0.20 * t     // tabla: hitbox ≈ 0.40 * t de alto
        val gapBetweenColumns = 90.0 * config.scale // espacio entre columnas

        // Centros X de las dos columnas: la distancia entre dados es
        // 2*cubeDieHalf + gapBetweenColumns.
        val columnGap = 2 * cubeDieHalf + gapBetweenColumns
        val col1X = baseX
        val col2X = baseX + columnGap

        // Nivel 0: cubos de madera en la base (apoyados en el piso).
        val cubeCenterY = floorY - cubeDieHalf
        val cuboBaseIzq = entityFactory.createBlock("cubo", "madera")
        placeByHitboxCenter(cuboBaseIzq, col1X, cubeCenterY)
        blocks += cuboBaseIzq

        val cuboBaseDer = entityFactory.createBlock("cubo", "madera")
        placeByHitboxCenter(cuboBaseDer, col2X, cubeCenterY)
        blocks += cuboBaseDer

        // Nivel 1: tablas de madera verticales como paredes, sobre los cubos.
        val wallCenterY = floorY - 2 * cubeDieHalf - wallHalfH
        val paredIzq = entityFactory.createBlock("tabla", "madera")
        placeByHitboxCenter(paredIzq, col1X, wallCenterY)
        blocks += paredIzq

        val paredDer = entityFactory.createBlock("tabla", "madera")
        placeByHitboxCenter(paredDer, col2X, wallCenterY)
        blocks += paredDer

        // Nivel 2: cubos de piedra como techo/techo, sobre cada tabla.
        val roofCenterY = floorY - 2 * cubeDieHalf - 2 * wallHalfH - cubeDieHalf
        val techoIzq = entityFactory.createBlock("cubo", "piedra")
        placeByHitboxCenter(techoIzq, col1X, roofCenterY)
        blocks += techoIzq

        val techoDer = entityFactory.createBlock("cubo", "piedra")
        placeByHitboxCenter(techoDer, col2X, roofCenterY)
        blocks += techoDer

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
            slingshot.forceStartDrag(mx, my)
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
            val pos = slingshot.birdStretchPos
            loaded.placeAt(pane, pos.x - loaded.width / 2, pos.y - loaded.height / 2)
            physics.setTransform(loaded, pos.x, pos.y)

            // Mostrar el ángulo de tiro mientras se estira
            val pull = slingshot.pull
            angleLabel.text = if (pull.magnitude() > 10.0) {
                val angleDeg = Math.toDegrees(kotlin.math.atan2(-pull.y, pull.x))
                "Ángulo de tiro: %.0f°".format(angleDeg)
            } else {
                "Ángulo de tiro: —"
            }

            updateTrajectory(slingshot.birdStretchPos, pull)
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
            angleLabel.text = "Ángulo de tiro: —"
            trajectoryLayer.children.clear()
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
            is Bomb -> {
                if (!bird.abilityUsed) {
                    bird.explode(physics, config)
                    val bx = bird.x + bird.width / 2
                    val by = bird.y + bird.height / 2
                    explosionPlayer.playAt(bx, by)
                    // La onda expansiva rompe las dianas dentro de su radio
                    breakTargetsWithin(bx, by, config.bombExplosionRadius.toDouble())
                }
            }
            is Chuck -> bird.boostSpeed(config.chuckSpeedBoost, physics)
            else -> {}
        }
    }

    /** Rompe (suma punto y desactiva) las dianas vivas dentro del radio. */
    private fun breakTargetsWithin(cx: Double, cy: Double, radius: Double) {
        if (timeOver) return
        for (t in targets) {
            if (!t.alive || t.hit) continue
            val tcy = t.cy(globalTime)
            val dx = t.cx - cx
            val dy = tcy - cy
            if (dx * dx + dy * dy <= (radius + t.radius) * (radius + t.radius)) {
                score++
                scoreLabel.text = "Puntos: $score"
                t.hit = true
                t.alive = false
                t.respawnAt = globalTime + 2.0
                t.view.imageView.isVisible = false
            }
        }
    }

     // ── Habilidades y trayectoria ────────────────────────────────────

    /**
     * Dibuja una línea de puntos con la trayectoria balística esperada del
     * pájaro si se soltara con el estiramiento actual (misma física que
     * [launchBird]: velocidad proporcional al pull + gravedad g = 9.81).
     */
    private fun updateTrajectory(start: Point2D, pull: Point2D) {
        trajectoryLayer.children.clear()
        if (pull.magnitude() <= 10.0) return

        val mag = min(pull.magnitude(), config.maxStretch)
        val speed = mag / config.maxStretch * config.maxLaunchSpeed
        var vx = pull.x / mag * speed
        var vy = pull.y / mag * speed
        val gPx = config.gravity * config.ppm  // px/s² hacia abajo en pantalla

        // Integrar con el mismo paso y el mismo amortecimiento (linearDamping)
        // que usa dyn4j sobre el cuerpo del pájaro; si no, la predicción
        // siempre resulta más larga que la trayectoria real.
        var x = start.x
        var y = start.y
        var stepCount = 0
        val step = 1.0 / 60.0
        var t = 0.0
        while (t < 2.5) {
            val damp = 1.0 / (1.0 + 0.1 * step)
            vx *= damp
            vy *= damp
            vy += gPx * step
            x += vx * step
            y += vy * step
            t += step
            stepCount++

            // Punto cada 3 pasos (~0.05 s)
            if (stepCount % 3 == 0) {
                if (y > config.windowHeight || x < 0 || x > config.windowWidth) break
                trajectoryLayer.children.add(
                    javafx.scene.shape.Circle(x, y, 4.5 * config.scale).apply {
                        fill = javafx.scene.paint.Color.web("#FFFFFF", 0.85)
                        stroke = javafx.scene.paint.Color.web("#222222", 0.6)
                        strokeWidth = 1.0
                    }
                )
            }
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
                    updateTargets(fixedDt)
                    accumulator -= fixedDt
                }

                // Limpiar pájaros lanzados que ya se detuvieron. NO limpiamos
        // lastLaunched: la bomba debe poder explotar con G aunque ya haya
        // caído al piso (solo se reemplaza al lanzar otro pájaro).
                val resting = mutableListOf<Bird>()
                for (f in inFlight) {
                    if (physics.isResting(f)) {
                        physics.remove(f)
                        resting += f
                    }
                }
                inFlight.removeAll(resting)
            }
        }.also { it.start() }
    }

    fun stopPhysicsLoop() {
        physicsTimer?.stop()
    }
}
