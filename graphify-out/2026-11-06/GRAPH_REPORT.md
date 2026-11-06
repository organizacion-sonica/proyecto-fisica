# Graph Report - proyecto-fisica  (2026-11-06)

## Corpus Check
- 57 files · ~16,356 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 497 nodes · 914 edges · 26 communities (9 shown, 17 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 29 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d56a347e`
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
- ScoreBoard
- SpriteSource
- AGENTS.md
- ScreenMapper
- InstructionsContent.kt
- MenuStyles.kt

## God Nodes (most connected - your core abstractions)
1. `SlingshotGame` - 42 edges
2. `GameEntity` - 32 edges
3. `BasketGameController` - 27 edges
4. `Box2DWorld` - 27 edges
5. `Bird` - 25 edges
6. `BoxHitbox` - 24 edges
7. `GameConfig` - 24 edges
8. `CircularHitbox` - 23 edges
9. `Hitbox` - 23 edges
10. `PhysicsWorld` - 23 edges

## Surprising Connections (you probably didn't know these)
- `GameController` --implements--> `AbilityEffects`  [EXTRACTED]
  app/src/main/kotlin/org/example/GameController.kt → app/src/main/kotlin/org/example/AbilityEffects.kt
- `AbstractEntity` --references--> `EntityView`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/EntityView.kt
- `AbstractEntity` --implements--> `GameEntity`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/GameEntity.kt
- `AbstractEntity` --references--> `Hitbox`  [EXTRACTED]
  app/src/main/kotlin/org/example/AbstractEntity.kt → app/src/main/kotlin/org/example/Hitbox.kt
- `AngleIndicator` --references--> `GameConfig`  [EXTRACTED]
  app/src/main/kotlin/org/example/AngleIndicator.kt → app/src/main/kotlin/org/example/GameConfig.kt

## Import Cycles
- None detected.

## Communities (26 total, 17 thin omitted)

### Community 0 - "Bird"
Cohesion: 0.08
Nodes (7): AbilityEffects, AbstractEntity, Bird, BirdPile, Bomb, Chuck, Red

### Community 1 - "GameEntity"
Cohesion: 0.05
Nodes (6): Box2DWorld, EntitySynchronizer, GameEntity, GameLoop, AnimationTimer, PhysicsWorld

### Community 3 - "BasketGameController"
Cohesion: 0.09
Nodes (3): BasketGameCollaborators, BasketGameController, BasketHoop

### Community 4 - "GameController"
Cohesion: 0.06
Nodes (10): AngleIndicator, AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor (+2 more)

### Community 5 - "BackgroundView"
Cohesion: 0.20
Nodes (3): BackgroundView, LaunchZone, SlingshotLevelComponents

### Community 6 - "MenuController"
Cohesion: 0.10
Nodes (3): MenuController, MenuFactory, MenuNavigator

### Community 8 - "ScoreBoard"
Cohesion: 0.08
Nodes (6): FileSpriteSource, RankEvaluator, RankScreen, Resoltera, SlingshotApp, TrajectoryCalculator

### Community 17 - "SpriteSource"
Cohesion: 0.11
Nodes (6): DianaManager, EntityFactory, ExplosionPlayer, GameConfig, Hoop, SpriteSource

### Community 25 - "ScreenMapper"
Cohesion: 0.14
Nodes (4): BodyFactory, ExplosionForce, DefaultScreenMapper, ScreenMapper

## Knowledge Gaps
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SlingshotGame` connect `SlingshotGame` to `Bird`, `GameEntity`, `BasketGameController`, `GameController`, `BackgroundView`, `BoxHitbox`, `SlingshotView`, `SpriteSource`?**
  _High betweenness centrality (0.340) - this node is a cross-community bridge._
- **Should `Bird` be split into smaller, more focused modules?**
  _Cohesion score 0.07827260458839407 - nodes in this community are weakly interconnected._
- **Why does `GameConfig` connect `SpriteSource` to `Bird`, `GameEntity`, `SlingshotGame`, `GameController`, `BackgroundView`, `MenuController`, `ScoreBoard`, `SlingshotView`, `ScoreBoard`, `ScreenMapper`?**
  _High betweenness centrality (0.311) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.0514216575922565 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.138) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.12477718360071301 - nodes in this community are weakly interconnected._
- **Should `BasketGameController` be split into smaller, more focused modules?**
  _Cohesion score 0.0944741532976827 - nodes in this community are weakly interconnected._