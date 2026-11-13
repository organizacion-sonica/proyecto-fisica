# Graph Report - proyecto-fisica  (2026-11-13)

## Corpus Check
- 70 files · ~20,836 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 653 nodes · 1211 edges · 33 communities (11 shown, 22 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 38 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8ef8094c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AbstractEntity
- GameEntity
- SlingshotGame
- BasketGameController
- GameController
- BodyRegistry
- MenuController
- GameConfig
- ScoreBoard
- OvalHitbox
- BoxHitbox
- SlingshotView
- CircularHitbox
- Hitbox
- .collides
- Box2DWorld
- AngleIndicator
- SpriteSource
- Bird
- AGENTS.md
- 4. Aspectos evaluados
- TrajectoryCalculator
- ScreenMapper
- HitboxFactory
- BodyFactory
- InstructionsContent.kt
- MenuStyles.kt
- DefaultExplosionForce
- GameLoop

## God Nodes (most connected - your core abstractions)
1. `GameConfig` - 41 edges
2. `GameEntity` - 41 edges
3. `SlingshotGame` - 37 edges
4. `Bird` - 35 edges
5. `PhysicsWorld` - 31 edges
6. `Box2DWorld` - 29 edges
7. `SpriteSource` - 26 edges
8. `BasketGameController` - 25 edges
9. `BoxHitbox` - 24 edges
10. `Hitbox` - 24 edges

## Surprising Connections (you probably didn't know these)
- `Box2DWorld` --calls--> `BodyRegistry`  [INFERRED]
  app/src/main/kotlin/org/example/Box2DWorld.kt → app/src/main/kotlin/org/example/BodyRegistry.kt
- `GameController` --implements--> `AbilityEffects`  [EXTRACTED]
  app/src/main/kotlin/org/example/GameController.kt → app/src/main/kotlin/org/example/AbilityEffects.kt
- `AbstractEntity` --references--> `EntityView`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/EntityView.kt
- `AbstractEntity` --implements--> `GameEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/GameEntity.kt
- `AbstractEntity` --references--> `Hitbox`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/Hitbox.kt

## Import Cycles
- None detected.

## Communities (33 total, 22 thin omitted)

### Community 0 - "AbstractEntity"
Cohesion: 0.11
Nodes (5): AbilityEffects, AbstractEntity, Bomb, Chuck, Red

### Community 3 - "BasketGameController"
Cohesion: 0.06
Nodes (6): BasketGameController, BasketHoop, BasketScoreBoard, BasketTimer, HoopScorer, RankEvaluator

### Community 4 - "GameController"
Cohesion: 0.07
Nodes (10): BasketGameCollaborators, AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor (+2 more)

### Community 6 - "MenuController"
Cohesion: 0.08
Nodes (5): MenuController, DeferredMenuNavigator, MenuBuilder, MenuFactory, MenuNavigator

### Community 7 - "GameConfig"
Cohesion: 0.07
Nodes (11): BasketHud, BirdSuppliers, DianaManager, EntityFactory, ExplosionPlayer, GameConfig, Hoop, RankScreen (+3 more)

### Community 8 - "ScoreBoard"
Cohesion: 0.08
Nodes (4): FileSpriteSource, Resoltera, ScoreBoard, SlingshotApp

### Community 11 - "SlingshotView"
Cohesion: 0.10
Nodes (4): BackgroundView, LaunchZone, SlingshotLevelComponents, SlingshotView

### Community 18 - "Bird"
Cohesion: 0.06
Nodes (5): Bird, BirdPile, FlightTracker, SlingshotGameCallbacks, SlingshotInput

### Community 23 - "4. Aspectos evaluados"
Cohesion: 0.07
Nodes (28): 10. Herencia profunda (DIT) (herencia) — sin señales, 11. Switch sobre tipos (OCP) (ocp) — sin señales, 12. Interfaz con demasiados métodos (ISP) (isp) — sin señales, 13. Override vacío o 'no implementado' (LSP) (lsp) — con señales, 14. Dependencia de concretos y falta de abstracciones (DIP) (dip) — con señales, 15. Clase sin métodos (solo datos) (data) — con señales, 16. Campos públicos (encapsulación) (encapsulacion) — con señales, 17. Clase solo con métodos estáticos (utilitaria) (static) — sin señales (+20 more)

### Community 25 - "ScreenMapper"
Cohesion: 0.15
Nodes (4): DefaultEntitySynchronizer, EntitySynchronizer, DefaultScreenMapper, ScreenMapper

### Community 27 - "BodyFactory"
Cohesion: 0.23
Nodes (3): BodyFactory, BoxSpec, DefaultBodyFactory

## Knowledge Gaps
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameConfig` connect `GameConfig` to `AbstractEntity`, `SlingshotGame`, `BasketGameController`, `MenuController`, `ScoreBoard`, `SlingshotView`, `Box2DWorld`, `AngleIndicator`, `Bird`, `TrajectoryCalculator`, `ScreenMapper`, `BodyFactory`?**
  _High betweenness centrality (0.342) - this node is a cross-community bridge._
- **Should `AbstractEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.10507246376811594 - nodes in this community are weakly interconnected._
- **Why does `SlingshotGame` connect `SlingshotGame` to `GameEntity`, `GameLoop`, `BasketGameController`, `GameController`, `GameConfig`, `BoxHitbox`, `SlingshotView`, `Bird`?**
  _High betweenness centrality (0.199) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.10461538461538461 - nodes in this community are weakly interconnected._
- **Why does `GameEntity` connect `GameEntity` to `AbstractEntity`, `BodyRegistry`, `Hitbox`, `Box2DWorld`, `SpriteSource`, `Bird`, `ScreenMapper`, `BodyFactory`, `DefaultExplosionForce`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.13405797101449277 - nodes in this community are weakly interconnected._
- **Should `BasketGameController` be split into smaller, more focused modules?**
  _Cohesion score 0.05612244897959184 - nodes in this community are weakly interconnected._