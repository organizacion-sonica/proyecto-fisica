# Graph Report - proyecto-fisica  (2026-11-06)

## Corpus Check
- 61 files · ~16,887 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 532 nodes · 988 edges · 37 communities (7 shown, 30 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 34 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `72a3c790`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- GameConfig
- GameEntity
- SlingshotGame
- BasketGameController
- GameController
- BackgroundView
- MenuController
- EntityView
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
- AGENTS.md
- Bird
- ScreenMapper
- BirdPile
- BodyFactory
- InstructionsContent.kt
- MenuStyles.kt
- BasketTimer
- ExplosionForce
- FlightTracker
- ExplosionPlayer
- GameLoop
- BackgroundView
- EntitySynchronizer

## God Nodes (most connected - your core abstractions)
1. `SlingshotGame` - 43 edges
2. `GameEntity` - 34 edges
3. `BasketGameController` - 28 edges
4. `Bird` - 27 edges
5. `Box2DWorld` - 27 edges
6. `GameConfig` - 27 edges
7. `BoxHitbox` - 24 edges
8. `Hitbox` - 24 edges
9. `PhysicsWorld` - 24 edges
10. `CircularHitbox` - 23 edges

## Surprising Connections (you probably didn't know these)
- `GameController` --implements--> `AbilityEffects`  [EXTRACTED]
  app/src/main/kotlin/org/example/GameController.kt → app/src/main/kotlin/org/example/AbilityEffects.kt
- `AbstractEntity` --implements--> `GameEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/GameEntity.kt
- `AbstractEntity` --references--> `Hitbox`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/Hitbox.kt
- `Bomb` --inherits--> `AbstractEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/Bomb.kt → app/src/main/kotlin/org/example/AbstractEntity.kt
- `Chuck` --inherits--> `AbstractEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/Chuck.kt → app/src/main/kotlin/org/example/AbstractEntity.kt

## Import Cycles
- None detected.

## Communities (37 total, 30 thin omitted)

### Community 0 - "GameConfig"
Cohesion: 0.18
Nodes (4): AbilityEffects, Bomb, Chuck, GameConfig

### Community 3 - "BasketGameController"
Cohesion: 0.07
Nodes (5): BasketGameController, BasketHoop, BasketScoreBoard, Hoop, RankEvaluator

### Community 4 - "GameController"
Cohesion: 0.07
Nodes (10): BasketGameCollaborators, AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor (+2 more)

### Community 5 - "BackgroundView"
Cohesion: 0.22
Nodes (3): EntityFactory, LaunchZone, SlingshotLevelComponents

### Community 6 - "MenuController"
Cohesion: 0.10
Nodes (3): MenuController, MenuFactory, MenuNavigator

### Community 8 - "ScoreBoard"
Cohesion: 0.07
Nodes (6): FileSpriteSource, RankScreen, Resoltera, ScoreBoard, SlingshotApp, TrajectoryCalculator

### Community 17 - "SpriteSource"
Cohesion: 0.11
Nodes (4): BasketHud, DianaManager, SpriteSource, Target

## Knowledge Gaps
- **30 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameConfig` connect `GameConfig` to `ExplosionPlayer`, `SlingshotGame`, `BackgroundView`, `BasketGameController`, `BackgroundView`, `MenuController`, `ScoreBoard`, `SlingshotView`, `Box2DWorld`, `AngleIndicator`, `SpriteSource`, `ScreenMapper`, `BirdPile`, `BodyFactory`, `BasketTimer`?**
  _High betweenness centrality (0.331) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.09523809523809523 - nodes in this community are weakly interconnected._
- **Why does `SlingshotGame` connect `SlingshotGame` to `FlightTracker`, `GameConfig`, `GameLoop`, `BackgroundView`, `BasketGameController`, `GameController`, `BackgroundView`, `GameEntity`, `BoxHitbox`, `SlingshotView`, `SpriteSource`, `Bird`, `BirdPile`?**
  _High betweenness centrality (0.316) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.12299465240641712 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Should `BasketGameController` be split into smaller, more focused modules?**
  _Cohesion score 0.06659619450317125 - nodes in this community are weakly interconnected._
- **Should `GameController` be split into smaller, more focused modules?**
  _Cohesion score 0.06717687074829932 - nodes in this community are weakly interconnected._