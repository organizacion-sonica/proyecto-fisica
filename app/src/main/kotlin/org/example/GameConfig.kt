package org.example

/**
 * Configuración central del juego (números mágicos agrupados en un solo lugar).
 * Facilita hacer tuning de física, pantalla y resortera sin tocar la lógica.
 *
 * El diseño base era 960x640: todos los tamaños en píxeles se escalan con
 * [scale] para que el juego se vea/juegue igual en cualquier ventana
 * (por defecto 1920x1080 → scale = 2).
 */
class GameConfig(
    val windowWidth: Double = 1920.0,
    val windowHeight: Double = 1080.0,
    val floorMargin: Double = 0.0,

    /** Factor de escala respecto del lienzo base de 960x640. */
    val scale: Double = windowWidth / 960.0,

    // -- Entidades --
    val birdSize: Double = 56.0 * scale,
    val blockSize: Double = 140.0 * scale,
    val tablaSize: Double = 140.0 * scale,

    // -- Física (Box2D) --
    val ppm: Float = 50f * scale.toFloat(), // píxeles por metro (escala Box2D)
    val gravity: Float = 9.81f,          // m/s² (eje Y positivo en Box2D)
    val physicsDt: Float = 1.0f / 60.0f, // paso fijo de integración
    val velocityIterations: Int = 8,
    val positionIterations: Int = 3,

    // -- Resortera --
    val slingshotHeight: Double = 175.0 * scale, // alto de la resortera (px)
    val slingshotGrabRadius: Double = 70.0 * scale,
    val maxStretch: Double = 145.0 * scale,      // px máximos de estiramiento
    val maxLaunchSpeed: Double = 1100.0 * scale, // px/s a estiramiento máximo

    // -- Zona de lanzamiento --
    val launchZoneSize: Double = 160.0 * scale,  // px del cuadrado invisible

    // -- Densidades de bloques (masa del material; madera liviana, piedra pesada) --
    val woodDensity: Float = 10.0f,
    val stoneDensity: Float = 40.0f,

    // -- Propiedades físicas de pájaros (masa en kg) --
    val redMass: Float = 5.0f,
    val chuckMass: Float = 3.0f,
    val bombMass: Float = 10.0f,

    // -- Habilidades especiales --
    val bombExplosionRadius: Float = 80.0f * scale.toFloat(),
    val bombExplosionForce: Float = 30000.0f * scale.toFloat(),
    val chuckSpeedBoost: Float = 2.5f,         // multiplicador de velocidad
)
