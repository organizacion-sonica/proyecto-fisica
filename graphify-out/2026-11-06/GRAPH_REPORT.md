# Graph Report - proyecto-fisica  (2026-11-06)

## Corpus Check
- 56 files · ~16,247 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 3, .properties 2, .toml 1)

## Summary
- 492 nodes · 904 edges · 30 communities (6 shown, 24 thin omitted)
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
- EntitySynchronizer
- MenuController
- EntityView
- ScoreBoard
- OvalHitbox
- BoxHitbox
- SlingshotView
- CircularHitbox
- Hitbox
- .collides
- AngleIndicator
- SpriteSource
- AGENTS.md
- Box2DWorld
- ScreenMapper
- BodyFactory
- Box2DWorld.kt
- InstructionsContent.kt
- MenuStyles.kt

## God Nodes (most connected - your core abstractions)
1. `SlingshotGame` - 41 edges
2. `GameEntity` - 32 edges
3. `BasketGameController` - 27 edges
4. `Box2DWorld` - 27 edges
5. `Bird` - 25 edges
6. `BoxHitbox` - 24 edges
7. `GameConfig` - 24 edges
8. `CircularHitbox` - 23 edges
9. `Hitbox` - 23 edges
10. `OvalHitbox` - 22 edges

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

## Communities (30 total, 24 thin omitted)

### Community 0 - "Bird"
Cohesion: 0.07
Nodes (11): AbilityEffects, BackgroundView, Bird, BirdPile, Bomb, Chuck, EntityFactory, GameConfig (+3 more)

### Community 3 - "BasketGameController"
Cohesion: 0.07
Nodes (5): BasketGameCollaborators, BasketGameController, BasketHoop, Hoop, RankEvaluator

### Community 4 - "GameController"
Cohesion: 0.07
Nodes (9): AngleDisplay, ExplosionVisuals, GameCollaborators, RankDisplay, ScoreKeeper, TargetScene, TrajectoryPredictor, GameController (+1 more)

### Community 6 - "MenuController"
Cohesion: 0.10
Nodes (3): MenuController, MenuFactory, MenuNavigator

### Community 8 - "ScoreBoard"
Cohesion: 0.07
Nodes (6): FileSpriteSource, RankScreen, Resoltera, ScoreBoard, SlingshotApp, TrajectoryCalculator

### Community 17 - "SpriteSource"
Cohesion: 0.10
Nodes (4): DianaManager, ExplosionPlayer, SpriteSource, Target

## Knowledge Gaps
- **24 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SlingshotGame` connect `SlingshotGame` to `Bird`, `GameEntity`, `BasketGameController`, `GameController`, `BoxHitbox`, `SlingshotView`, `SpriteSource`?**
  _High betweenness centrality (0.332) - this node is a cross-community bridge._
- **Should `Bird` be split into smaller, more focused modules?**
  _Cohesion score 0.06871035940803383 - nodes in this community are weakly interconnected._
- **Why does `GameConfig` connect `Bird` to `SlingshotGame`, `BasketGameController`, `MenuController`, `ScoreBoard`, `SlingshotView`, `AngleIndicator`, `SpriteSource`, `Box2DWorld`, `ScreenMapper`, `BodyFactory`?**
  _High betweenness centrality (0.314) - this node is a cross-community bridge._
- **Should `GameEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.10153846153846154 - nodes in this community are weakly interconnected._
- **Why does `BoxHitbox` connect `BoxHitbox` to `BoxHitbox.kt`, `SlingshotGame`, `Hitbox`, `.collides`?**
  _High betweenness centrality (0.138) - this node is a cross-community bridge._
- **Should `SlingshotGame` be split into smaller, more focused modules?**
  _Cohesion score 0.11932773109243698 - nodes in this community are weakly interconnected._
- **Should `BasketGameController` be split into smaller, more focused modules?**
  _Cohesion score 0.06976744186046512 - nodes in this community are weakly interconnected._