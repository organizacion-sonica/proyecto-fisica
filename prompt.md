Contexto: proyecto Kotlin/JavaFX/Gradle en app/src/main/kotlin/org/example. La cátedra lo evalúa con docs/EVALUACION.md (autogenerado, NO lo edites; tampoco toques graphify-out/). Si AGENTS.md pide usar graphify y no está instalado, seguí sin él.

Objetivo: eliminar la señal "Acoplamiento alto (CBO)", que es lo único que impide aprobar. Clases marcadas: SlingshotDefaults, SlingshotGame, GameController, BasketGameController, Box2DWorld, BirdPile, MenuController (+ otras). Secundario: reducir la "clase demasiado grande" (SlingshotGame) y los métodos con muchos parámetros (ExplosionForce).
Refactorizá SIN cambiar el comportamiento del juego. Después de CADA tarea corré `./gradlew build` y arreglá lo que falle antes de seguir. No hagas commit.

TAREA 1 – SlingshotGame: usar la clase SlingshotInput que ya existe y hoy no se usa
SlingshotGame.kt (334 líneas) duplica casi todo lo de SlingshotInput.kt: handlePressed, handleDragged, stretchLoadedBird, handleReleased, releaseFromSlingshot, returnBirdToSlingshot, releaseDraggedBird, handleKeyPressed, isNearBird, loadBirdOntoSlingshot, launchBird, launchVelocity, grabRadius, y los campos dragBird y slingshotBird.
a) Borrá todo eso de SlingshotGame y delegá en SlingshotInput.
b) Creá la instancia de SlingshotInput en setupWorld() (después de crear slingshot, launchZone, pile y flightTracker). Usá un solo objeto `SlingshotGameCallbacks` anónimo privado que delegue en los hooks protegidos ya existentes: onBirdGrabbed, onBirdLoadedOntoSlingshot (para onBirdLoaded), onLaunch, onStretchUpdate, clearAimLayer, onAbilityKey, resetLevel, y que en setLastLaunchedBird asigne lastLaunched. Así no se rompe la visibilidad `protected` de los hooks.
c) En SlingshotGameCallbacks dejá solo los métodos que SlingshotInput realmente usa (quitá onBirdSettled y onLevelReset si nadie los llama desde SlingshotInput).
d) buildScene(): scene.setOnKeyPressed/MousePressed/MouseDragged/MouseReleased deben llamar a input.handleKeyPressed/handlePressed/handleDragged/handleReleased. Pasá a SlingshotInput como onExitToMenu un lambda que haga `stopPhysicsLoop(); onExitToMenu()` (el ESC actual detiene el loop antes de salir).
e) resetLevel(): reemplazá `dragBird = null; slingshotBird = null` por `input.clear()` (o recreá el input al volver a llamar setupWorld).
f) Mové MIN_STRETCH a SlingshotInput (companion) y actualizá TrajectoryCalculator, que hoy lo referencia como SlingshotGame.MIN_STRETCH. Reemplazá el `10.0` hardcodeado de SlingshotInput.handleReleased por esa constante. Borrá la constante SETTLE_FADE_START si nada la usa, y el método removeSettledBirds si solo reenvía a flightTracker.removeSettled() (llamalo directo).
g) Borrá imports que queden sin uso. Meta: SlingshotGame por debajo de ~180 líneas.

TAREA 2 – SlingshotDefaults: partir el "objeto Dios" (referencia ~35 tipos del proyecto)
Dividilo en objetos chicos por responsabilidad, manteniendo funcionando a SlingshotApp:
- PhysicsDefaults / LevelDefaults: physicsFactory, levelFactory, floorHitbox.
- TargetGameDefaults: gameCollaboratorFactory y gameController.
- BasketDefaults: basketCollaboratorFactory y basketController.
- MenuDefaults: menuBuilder.
La construcción de AimAssist (trajectoryLayer + TrajectoryCalculator + TrajectoryRenderer) está duplicada en gameCollaboratorFactory y basketCollaboratorFactory: extraela a una función privada o a un AimAssist.create(...) y usala en ambos sitios. Actualizá los llamadores (SlingshotApp, SlingshotGame y donde corresponda). Ningún objeto nuevo debería referenciar más de ~12 tipos del proyecto.

TAREA 3 – GameController y BasketGameController (13 tipos cada uno)
- Revisá imports y tipos que ya no se usen y borralos.
- En BasketGameController, mové la creación del poste/estático (createPole, BoxSpec) a BasketGameCollaborators o a su factory, para que el controlador no dependa de BoxSpec ni arme geometría.
- Si dependen de RankDisplay + RankEvaluator + ScoreBoard por separado, agrupalos en un único colaborador ya armado (en GameCollaborators / BasketGameCollaborators) y que el controlador reciba solo ese.

TAREA 4 – Box2DWorld (8 tipos)
Comprobá si BodyFactory, BodyRegistry, EntitySynchronizer, ExplosionForce y ScreenMapper se pueden agrupar sin romper la API: por ejemplo, que EntitySynchronizer reciba internamente el BodyRegistry y Box2DWorld solo dependa de BodyFactory, EntitySynchronizer y ExplosionForce. Hacelo solo si queda más simple y el build pasa.

TAREA 5 – Limpieza menor
- MenuController: borrá rebuildScene() (es idéntica a menuScene()) si nadie la usa, y los imports sin uso.
- BirdPile: no cambies la lógica; solo verificá imports. Si dependiera de tipos innecesarios, reducilos.

TAREA 6 – ExplosionForce.kt (métodos largos / muchos parámetros)
Creá `data class Explosion(val centerX: Double, val centerY: Double, val radiusM: Double, val force: Float, val pixelsPerMeter: Float)`.
- applyImpulseTo(body, explosion) en vez de 6 parámetros; applyImpulses(explosion, bird, bodies) en vez de 5.
- apply(bird, radiusPx, force, bodies) pasa a apply(bird, spec, bodies) con un `ExplosionSpec(radiusPx, force)` (o equivalente). Actualizá la interfaz ExplosionForce, DefaultExplosionForce, Box2DWorld.explodeAt y los llamadores (Bomb).
- Usá `Body` importado en vez de `org.dyn4j.dynamics.Body` completo en la interfaz.

TAREA 7 – SlingshotApp
Si todavía existe la ruta fija "D:\\downloads\\proyecto-fisica\\elementos graficos", cambiala por una ruta relativa al proyecto (por ejemplo File("elementos graficos")) o por una propiedad de sistema con ese valor por defecto, para que funcione en otra máquina.

NO hagas (no bloquean y arriesgan romper cosas): cambiar los `val` de las interfaces para "encapsulación", ni inventar interfaces para BirdSuppliers/HitboxFactory/TargetFactory.

Al terminar: corré `./gradlew build` y `./gradlew test`, abrí mentalmente los dos minijuegos (dianas y basket) comprobando que el flujo agarrar → cargar en la resortera → estirar → lanzar → G → R → ESC sigue igual, y listame qué archivos cambiaste, cuántas líneas quedó SlingshotGame y qué tareas quedaron sin hacer.