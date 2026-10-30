# Graph Report - proyecto-fisica  (2026-10-30)

## Corpus Check
- 44 files · ~16,080 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 411 nodes · 757 edges · 24 communities (6 shown, 18 thin omitted)
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
- BackgroundView
- AGENTS.md

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
10. `OvalHitbox` - 22 edges

## Surprising Connections (you probably didn't know these)
- `SlingshotGame` --calls--> `EntityFactory`  [INFERRED]
  app/src/main/kotlin/org/example/SlingshotGame.kt → app/src/main/kotlin/org/example/EntityFactory.kt
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

## Communities (24 total, 18 thin omitted)

### Community 0 - "Bird"
Cohesion: 0.08
Nodes (10): AbilityEffects, Bird, BirdPile, Bomb, Chuck, DianaManager, EntityFactory, GameConfig (+2 more)

### Community 1 - "GameEntity"
Cohesion: 0.06
Nodes (4): Box2DWorld, GameEntity, PhysicsWorld, ScreenMapper

### Community 2 - "SlingshotGame"
Cohesion: 0.10
Nodes (3): LaunchZone, SlingshotGame, AnimationTimer

### Community 3 - "BasketGameController"
Cohesion: 0.09
Nodes (3): BasketGameController, RankEvaluator, RankScreen

### Community 4 - "GameController"
Cohesion: 0.11
Nodes (3): ExplosionPlayer, GameController, TrajectoryCalculator

### Community 8 - "SlingshotApp"
Cohesion: 0.15
Nodes (3): FileSpriteSource, Resoltera, SlingshotApp

## Knowledge Gaps
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SlingshotGame` connect `SlingshotGame` to `Bird`, `GameEntity`, `BasketGameController`, `GameController`, `BoxHitbox`, `SlingshotView`?**
  _High betweenness centrality (0.337) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `BoxHitbox` (e.g. with `.`caja contiene su interior y no un punto fuera`()` and `.`colision caja-caja`()`) actually correct?**
  _`BoxHitbox` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Should `Bird` be split into smaller, more focused modules?**
  _Cohesion score 0.07632850241545894 - nodes in this community are weakly interconnected._
- **Why does `GameConfig` connect `Bird` to `GameEntity`, `SlingshotGame`, `BasketGameController`, `GameController`, `ScoreBoard`, `MenuController`, `SlingshotApp`, `SlingshotView`, `AngleIndicator`, `BackgroundView`?**
  _High betweenness centrality (0.285) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.059506531204644414 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.09743589743589744 - nodes in this community are weakly interconnected._