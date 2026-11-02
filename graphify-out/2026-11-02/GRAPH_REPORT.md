# Graph Report - proyecto-fisica  (2026-11-02)

## Corpus Check
- 54 files · ~16,090 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 482 nodes · 885 edges · 31 communities (7 shown, 24 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 33 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `213bdb09`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Bird
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
- AbstractEntity
- AngleIndicator
- SpriteSource
- AGENTS.md
- Box2DWorld
- ScreenMapper
- BodyFactory
- ExplosionForce.kt
- InstructionsContent.kt
- MenuStyles.kt
- ScoreBoard

## God Nodes (most connected - your core abstractions)
1. `SlingshotGame` - 39 edges
2. `GameEntity` - 32 edges
3. `BasketGameController` - 27 edges
4. `Box2DWorld` - 27 edges
5. `Bird` - 25 edges
6. `GameConfig` - 25 edges
7. `BoxHitbox` - 24 edges
8. `CircularHitbox` - 23 edges
9. `Hitbox` - 23 edges
10. `PhysicsWorld` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Box2DWorld` --calls--> `BodyFactory`  [INFERRED]
  app/src/main/kotlin/org/example/Box2DWorld.kt → app/src/main/kotlin/org/example/BodyFactory.kt
- `Box2DWorld` --calls--> `EntitySynchronizer`  [INFERRED]
  app/src/main/kotlin/org/example/Box2DWorld.kt → app/src/main/kotlin/org/example/EntitySynchronizer.kt
- `Box2DWorld` --calls--> `ExplosionForce`  [INFERRED]
  app/src/main/kotlin/org/example/Box2DWorld.kt → app/src/main/kotlin/org/example/ExplosionForce.kt
- `Box2DWorld` --calls--> `ScreenMapper`  [INFERRED]
  app/src/main/kotlin/org/example/Box2DWorld.kt → app/src/main/kotlin/org/example/ScreenMapper.kt
- `SlingshotGame` --calls--> `EntityFactory`  [INFERRED]
  app/src/main/kotlin/org/example/SlingshotGame.kt → app/src/main/kotlin/org/example/EntityFactory.kt

## Import Cycles
- None detected.

## Communities (31 total, 24 thin omitted)

### Community 0 - "Bird"
Cohesion: 0.10
Nodes (8): AbilityEffects, Bird, BirdPile, Bomb, Chuck, EntityFactory, GameConfig, Red

### Community 2 - "SlingshotGame"
Cohesion: 0.11
Nodes (3): LaunchZone, SlingshotGame, AnimationTimer

### Community 3 - "BasketGameController"
Cohesion: 0.07
Nodes (5): BasketGameCollaborators, BasketGameController, BasketHoop, Hoop, RankEvaluator

### Community 4 - "GameController"
Cohesion: 0.09
Nodes (8): AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor, GameController

### Community 6 - "MenuController"
Cohesion: 0.10
Nodes (3): MenuController, MenuFactory, MenuNavigator

### Community 8 - "ScoreBoard"
Cohesion: 0.09
Nodes (5): FileSpriteSource, RankScreen, Resoltera, SlingshotApp, TrajectoryCalculator

### Community 17 - "SpriteSource"
Cohesion: 0.10
Nodes (4): DianaManager, ExplosionPlayer, SpriteSource, Target

## Knowledge Gaps
- **24 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameConfig` connect `Bird` to `SlingshotGame`, `BasketGameController`, `BackgroundView`, `MenuController`, `ScoreBoard`, `SlingshotView`, `AngleIndicator`, `SpriteSource`, `Box2DWorld`, `ScreenMapper`, `BodyFactory`, `ScoreBoard`?**
  _High betweenness centrality (0.326) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Box2DWorld` (e.g. with `BodyFactory` and `EntitySynchronizer`) actually correct?**
  _`Box2DWorld` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Should `Bird` be split into smaller, more focused modules?**
  _Cohesion score 0.09848484848484848 - nodes in this community are weakly interconnected._
- **Why does `SlingshotGame` connect `SlingshotGame` to `Bird`, `GameEntity`, `BasketGameController`, `GameController`, `BoxHitbox`, `SlingshotView`, `SpriteSource`?**
  _High betweenness centrality (0.323) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.09113300492610837 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.137) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.10526315789473684 - nodes in this community are weakly interconnected._