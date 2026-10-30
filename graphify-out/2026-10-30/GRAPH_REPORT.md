# Graph Report - proyecto-fisica  (2026-10-30)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 408 nodes · 756 edges · 22 communities (6 shown, 16 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 18 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e10561a3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Bird
- GameEntity
- SlingshotGame
- BasketGameController
- GameController
- ScoreBoard
- MenuController
- EntityView
- SlingshotApp
- OvalHitbox
- BoxHitbox
- SlingshotView
- CircularHitbox
- Hitbox
- .collides
- AbstractEntity
- AngleIndicator
- ScreenMapper

## God Nodes (most connected - your core abstractions)
1. `SlingshotGame` - 39 edges
2. `BasketGameController` - 32 edges
3. `GameEntity` - 28 edges
4. `Box2DWorld` - 24 edges
5. `BoxHitbox` - 24 edges
6. `Bird` - 23 edges
7. `CircularHitbox` - 23 edges
8. `Hitbox` - 23 edges
9. `GameConfig` - 22 edges
10. `PhysicsWorld` - 22 edges

## Surprising Connections (you probably didn't know these)
- `SlingshotGame` --calls--> `EntityFactory`  [INFERRED]
  app/src/main/kotlin/org/example/SlingshotGame.kt → app/src/main/kotlin/org/example/EntityFactory.kt
- `Box2DWorld` --calls--> `ScreenMapper`  [INFERRED]
  app/src/main/kotlin/org/example/Box2DWorld.kt → app/src/main/kotlin/org/example/ScreenMapper.kt
- `GameController` --implements--> `AbilityEffects`  [EXTRACTED]
  app/src/main/kotlin/org/example/GameController.kt → app/src/main/kotlin/org/example/AbilityEffects.kt
- `BasketGameController` --references--> `Bird`  [EXTRACTED]
  app/src/main/kotlin/org/example/BasketGameController.kt → app/src/main/kotlin/org/example/Bird.kt
- `Bird` --implements--> `GameEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/Bird.kt → app/src/main/kotlin/org/example/GameEntity.kt

## Import Cycles
- None detected.

## Communities (22 total, 16 thin omitted)

### Community 0 - "Bird"
Cohesion: 0.07
Nodes (10): AbilityEffects, Bird, BirdPile, Bomb, Chuck, DianaManager, EntityFactory, GameConfig (+2 more)

### Community 1 - "GameEntity"
Cohesion: 0.07
Nodes (3): Box2DWorld, GameEntity, PhysicsWorld

### Community 2 - "SlingshotGame"
Cohesion: 0.10
Nodes (3): LaunchZone, SlingshotGame, AnimationTimer

### Community 4 - "GameController"
Cohesion: 0.11
Nodes (3): ExplosionPlayer, GameController, TrajectoryCalculator

### Community 5 - "ScoreBoard"
Cohesion: 0.11
Nodes (4): BackgroundView, RankEvaluator, RankScreen, ScoreBoard

### Community 8 - "SlingshotApp"
Cohesion: 0.15
Nodes (3): FileSpriteSource, Resoltera, SlingshotApp

## Knowledge Gaps
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SlingshotGame` connect `SlingshotGame` to `Bird`, `GameEntity`, `BasketGameController`, `GameController`, `BoxHitbox`, `SlingshotView`?**
  _High betweenness centrality (0.342) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `BoxHitbox` (e.g. with `.`caja contiene su interior y no un punto fuera`()` and `.`colision caja-caja`()`) actually correct?**
  _`BoxHitbox` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Should `Bird` be split into smaller, more focused modules?**
  _Cohesion score 0.06857142857142857 - nodes in this community are weakly interconnected._
- **Why does `GameConfig` connect `Bird` to `GameEntity`, `SlingshotGame`, `GameController`, `ScoreBoard`, `MenuController`, `SlingshotApp`, `SlingshotView`, `AngleIndicator`, `ScreenMapper`?**
  _High betweenness centrality (0.289) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.0673758865248227 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.161) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.10121457489878542 - nodes in this community are weakly interconnected._