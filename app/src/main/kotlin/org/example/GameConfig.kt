package org.example

/**
 * Configuración central del juego (números mágicos agrupados en un solo lugar).
 * Facilita hacer tuning de física, pantalla y resortera sin tocar la lógica.
 */
class GameConfig(
    val windowWidth: Double = 960.0,
    val windowHeight: Double = 640.0,
    val floorMargin: Double = 0.0,

    // -- Entidades --
    val birdSize: Double = 80.0,
    val blockSize: Double = 200.0,
    val tablaSize: Double = 200.0,

    // -- Física (Box2D) --
    val ppm: Float = 50f,                // píxeles por metro (escala Box2D)
    val gravity: Float = 9.81f,          // m/s² (eje Y positivo en Box2D)
    val physicsDt: Float = 1.0f / 60.0f, // paso fijo de integración
    val velocityIterations: Int = 8,
    val positionIterations: Int = 3,

    // -- Resortera --
    val slingshotHeight: Double = 240.0, // alto de la resortera (px)
    val slingshotGrabRadius: Double = 90.0,
    val maxStretch: Double = 200.0,      // px máximos de estiramiento
    val maxLaunchSpeed: Double = 1100.0, // px/s a estiramiento máximo

    // -- Zona de lanzamiento --
    val launchZoneSize: Double = 160.0,  // px del cuadrado invisible (amplio para no fallar)

    // -- Propiedades físicas de pájaros (masa en kg) --
    val redMass: Float = 5.0f,
    val chuckMass: Float = 3.0f,
    val bombMass: Float = 10.0f,

    // -- Habilidades especiales --
    val bombExplosionRadius: Float = 150.0f,   // px
    val bombExplosionForce: Float = 8000.0f,   // fuerza de la onda expansiva
    val chuckSpeedBoost: Float = 1.5f,         // multiplicador de velocidad
)