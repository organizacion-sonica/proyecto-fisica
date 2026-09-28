package org.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests de la capa de física (Hitbox + CollisionDetector).
 * Son lógica pura: no requieren el toolkit de JavaFX.
 */
class AppTest {

    // ---- Hitbox.contains ----

    @Test
    fun `circulo contiene su centro y no un punto fuera`() {
        val c = CircularHitbox(radius = 5.0).apply { recenter(10.0, 10.0) }
        assertTrue(c.contains(10.0, 10.0))
        assertTrue(c.contains(13.5, 10.0)) // dentro (dist 3.5 < 5)
        assertFalse(c.contains(16.0, 10.0)) // fuera (dist 6 > 5)
    }

    @Test
    fun `caja contiene su interior y no un punto fuera`() {
        val b = BoxHitbox(halfWidth = 4.0, halfHeight = 2.0).apply { recenter(0.0, 0.0) }
        assertTrue(b.contains(3.9, 1.9))
        assertFalse(b.contains(4.1, 0.0))
        assertFalse(b.contains(0.0, 2.1))
    }

    // ---- CollisionDetector ----

    @Test
    fun `colision circulo-circulo`() {
        val a = CircularHitbox(1.0).apply { recenter(0.0, 0.0) }
        val b = CircularHitbox(1.0).apply { recenter(1.5, 0.0) } // dist 1.5 < 2 -> chocan
        assertTrue(CollisionDetector.collides(a, b))

        val c = CircularHitbox(1.0).apply { recenter(5.0, 0.0) } // dist 5 > 2 -> no
        assertFalse(CollisionDetector.collides(a, c))
    }

    @Test
    fun `colision caja-caja`() {
        val a = BoxHitbox(2.0, 1.0).apply { recenter(0.0, 0.0) }
        val b = BoxHitbox(2.0, 1.0).apply { recenter(3.0, 0.0) } // separación 3 < 4 -> chocan
        assertTrue(CollisionDetector.collides(a, b))

        val c = BoxHitbox(2.0, 1.0).apply { recenter(10.0, 0.0) }
        assertFalse(CollisionDetector.collides(a, c))
    }

    @Test
    fun `colision circulo-caja`() {
        val box = BoxHitbox(4.0, 4.0).apply { recenter(0.0, 0.0) }
        val within = CircularHitbox(1.0).apply { recenter(3.0, 3.0) } // toca la esquina
        assertTrue(CollisionDetector.collides(box, within))

        val far = CircularHitbox(1.0).apply { recenter(10.0, 0.0) }
        assertFalse(CollisionDetector.collides(box, far))
    }
}
