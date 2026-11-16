# Graph Report - proyecto-fisica  (2026-11-16)

## Corpus Check
- 70 files · ~20,788 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 644 nodes · 1204 edges · 40 communities (10 shown, 30 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 42 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7e1d987f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AbstractEntity
- GameEntity
- SlingshotGameCallbacks
- BasketGameController
- GameController
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
- BasketTimer
- BasketScoreBoard
- GameLoop
- .rankForBasket
- Hoop
- HoopScorer
- BasketGameCollaborators
- BasketHoop
- SlingshotGame

## God Nodes (most connected - your core abstractions)
1. `GameConfig` - 39 edges
2. `GameEntity` - 37 edges
3. `Bird` - 35 edges
4. `PhysicsWorld` - 30 edges
5. `Box2DWorld` - 27 edges
6. `SlingshotGame` - 27 edges
7. `SpriteSource` - 25 edges
8. `BoxHitbox` - 24 edges
9. `Hitbox` - 24 edges
10. `OvalHitbox` - 23 edges

## Surprising Connections (you probably didn't know these)
- `GameController` --implements--> `AbilityEffects`  [EXTRACTED]
  app/src/main/kotlin/org/example/GameController.kt → app/src/main/kotlin/org/example/AbilityEffects.kt
- `AbstractEntity` --references--> `EntityView`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/EntityView.kt
- `AbstractEntity` --implements--> `GameEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/GameEntity.kt
- `AbstractEntity` --references--> `Hitbox`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/Hitbox.kt
- `BasketGameCollaborators` --references--> `AimAssist`  [EXTRACTED]
  app/src/main/kotlin/org/example/BasketGameCollaborators.kt → app/src/main/kotlin/org/example/AimAssist.kt

## Import Cycles
- None detected.

## Communities (40 total, 30 thin omitted)

### Community 0 - "AbstractEntity"
Cohesion: 0.11
Nodes (5): AbilityEffects, AbstractEntity, Bomb, Chuck, Red

### Community 4 - "GameController"
Cohesion: 0.06
Nodes (10): AimAssist, AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor (+2 more)

### Community 6 - "MenuController"
Cohesion: 0.07
Nodes (5): MenuController, DeferredMenuNavigator, MenuBuilder, MenuFactory, MenuNavigator

### Community 7 - "GameConfig"
Cohesion: 0.06
Nodes (14): BackgroundView, BasketHud, BirdSuppliers, DianaManager, EntityFactory, ExplosionPlayer, GameConfig, LaunchZone (+6 more)

### Community 8 - "ScoreBoard"
Cohesion: 0.08
Nodes (4): FileSpriteSource, Resoltera, ScoreBoard, SlingshotApp

### Community 18 - "Bird"
Cohesion: 0.08
Nodes (4): Bird, BirdPile, FlightTracker, SlingshotInput

### Community 23 - "4. Aspectos evaluados"
Cohesion: 0.07
Nodes (28): 10. Herencia profunda (DIT) (herencia) — sin señales, 11. Switch sobre tipos (OCP) (ocp) — sin señales, 12. Interfaz con demasiados métodos (ISP) (isp) — sin señales, 13. Override vacío o 'no implementado' (LSP) (lsp) — sin señales, 14. Dependencia de concretos y falta de abstracciones (DIP) (dip) — con señales, 15. Clase sin métodos (solo datos) (data) — sin señales, 16. Campos públicos (encapsulación) (encapsulacion) — con señales, 17. Clase solo con métodos estáticos (utilitaria) (static) — sin señales (+20 more)

### Community 25 - "ScreenMapper"
Cohesion: 0.15
Nodes (4): DefaultEntitySynchronizer, EntitySynchronizer, DefaultScreenMapper, ScreenMapper

### Community 27 - "BodyFactory"
Cohesion: 0.19
Nodes (3): BodyFactory, BoxSpec, DefaultBodyFactory

### Community 31 - "DefaultExplosionForce"
Cohesion: 0.38
Nodes (4): DefaultExplosionForce, Explosion, ExplosionForce, ExplosionSpec

## Knowledge Gaps
- **30 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameConfig` connect `GameConfig` to `BasketTimer`, `BasketScoreBoard`, `AbstractEntity`, `Hoop`, `HoopScorer`, `MenuController`, `ScoreBoard`, `SlingshotGame`, `SlingshotView`, `AngleIndicator`, `Bird`, `TrajectoryCalculator`, `ScreenMapper`, `BodyFactory`?**
  _High betweenness centrality (0.310) - this node is a cross-community bridge._
- **Should `AbstractEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.10507246376811594 - nodes in this community are weakly interconnected._
- **Why does `SlingshotGame` connect `SlingshotGame` to `GameEntity`, `GameLoop`, `BasketGameController`, `GameController`, `GameConfig`, `BoxHitbox`, `SlingshotView`, `Bird`?**
  _High betweenness centrality (0.178) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._
- **Why does `Bird` connect `Bird` to `AbstractEntity`, `GameEntity`, `SlingshotGameCallbacks`, `BasketGameController`, `GameController`, `HoopScorer`, `GameConfig`, `SlingshotGame`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Should `GameController` be split into smaller, more focused modules?**
  _Cohesion score 0.06290471785383904 - nodes in this community are weakly interconnected._
- **Should `MenuController` be split into smaller, more focused modules?**
  _Cohesion score 0.07428571428571429 - nodes in this community are weakly interconnected._