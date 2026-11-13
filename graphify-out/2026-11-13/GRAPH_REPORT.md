# Graph Report - proyecto-fisica  (2026-11-06)

## Corpus Check
- 62 files · ~16,859 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 545 nodes · 1009 edges · 29 communities (8 shown, 21 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 38 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `801f4554`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Bird
- GameEntity
- SlingshotGame
- BasketGameController
- GameController
- BodyRegistry
- MenuController
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
- ScreenMapper
- BodyFactory
- InstructionsContent.kt
- MenuStyles.kt
- ExplosionForce
- GameLoop

## God Nodes (most connected - your core abstractions)
1. `SlingshotGame` - 41 edges
2. `GameEntity` - 38 edges
3. `Bird` - 28 edges
4. `Box2DWorld` - 28 edges
5. `BasketGameController` - 27 edges
6. `GameConfig` - 25 edges
7. `BoxHitbox` - 24 edges
8. `Hitbox` - 24 edges
9. `PhysicsWorld` - 24 edges
10. `CircularHitbox` - 23 edges

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

## Communities (29 total, 21 thin omitted)

### Community 0 - "Bird"
Cohesion: 0.05
Nodes (11): AbilityEffects, AbstractEntity, BasketTimer, Bird, BirdPile, Bomb, Chuck, EntityFactory (+3 more)

### Community 3 - "BasketGameController"
Cohesion: 0.07
Nodes (5): BasketGameController, BasketHoop, BasketScoreBoard, Hoop, RankEvaluator

### Community 4 - "GameController"
Cohesion: 0.07
Nodes (10): BasketGameCollaborators, AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor (+2 more)

### Community 6 - "MenuController"
Cohesion: 0.10
Nodes (3): MenuController, MenuFactory, MenuNavigator

### Community 8 - "ScoreBoard"
Cohesion: 0.05
Nodes (9): BasketHud, ExplosionPlayer, FileSpriteSource, RankScreen, Resoltera, ScoreBoard, SlingshotApp, SpriteSource (+1 more)

### Community 11 - "SlingshotView"
Cohesion: 0.10
Nodes (4): BackgroundView, LaunchZone, SlingshotLevelComponents, SlingshotView

### Community 17 - "SpriteSource"
Cohesion: 0.09
Nodes (3): DianaManager, EntityView, Target

### Community 25 - "ScreenMapper"
Cohesion: 0.23
Nodes (3): EntitySynchronizer, DefaultScreenMapper, ScreenMapper

## Knowledge Gaps
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SlingshotGame` connect `SlingshotGame` to `Bird`, `GameEntity`, `GameLoop`, `BasketGameController`, `GameController`, `ScoreBoard`, `BoxHitbox`, `SlingshotView`?**
  _High betweenness centrality (0.317) - this node is a cross-community bridge._
- **Should `Bird` be split into smaller, more focused modules?**
  _Cohesion score 0.05325814536340852 - nodes in this community are weakly interconnected._
- **Why does `GameConfig` connect `Bird` to `SlingshotGame`, `BasketGameController`, `MenuController`, `ScoreBoard`, `SlingshotView`, `Box2DWorld`, `AngleIndicator`, `SpriteSource`, `ScreenMapper`, `BodyFactory`?**
  _High betweenness centrality (0.310) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.09523809523809523 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.132) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.1350806451612903 - nodes in this community are weakly interconnected._
- **Should `BasketGameController` be split into smaller, more focused modules?**
  _Cohesion score 0.06755260243632337 - nodes in this community are weakly interconnected._