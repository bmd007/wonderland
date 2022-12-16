import 'package:flame/components.dart';
import 'package:flame/game.dart';
import 'package:flame/input.dart';
import 'package:flame/palette.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class MyForge2DFlameGame extends Forge2DGame with HasTappables {
  @override
  Future<void> onLoad() async {
    await super.onLoad();
    debugMode = false;
    addAll(createBoundaries(this));
    add(MyPlatform(Vector2(100, 30)));
    add(MyGirl(Vector2(100, 20)));
    add(MyGround(screenToWorld(camera.viewport.effectiveSize)));
  }
}

class MyPlatform extends BodyComponent {
  final Vector2 position;

  MyPlatform(this.position);

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = false;
    var girlAnimation = SpriteComponent()
      ..sprite = await gameRef.loadSprite("game/Tile_13.png")
      ..size = Vector2(24, 4)
      ..anchor = Anchor.topCenter;
    add(girlAnimation);
  }

  @override
  Body createBody() {
    PolygonShape shape = PolygonShape()..setAsBoxXY(12, 1);
    final fixtureDefinition = FixtureDef(shape, density: 1, restitution: 0.4, friction: 0.2);
    final bodyDefinition = BodyDef(position: position, type: BodyType.static);
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}

class MyGirl extends BodyComponent with Tappable {
  final Vector2 initialPosition;

  MyGirl(this.initialPosition);

  @override
  bool onTapDown(TapDownInfo info) {
    body.applyAngularImpulse(1000);
    body.applyLinearImpulse(Vector2(22, -10) * 1000);
    body.applyForce(Vector2(222, -1000) * 1000);
    body.applyTorque(1000);
    return false;
  }

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = false;
    SpriteAnimationData spriteData =
        SpriteAnimationData.sequenced(amount: 9, stepTime: 0.03, textureSize: Vector2(152.0, 142.0));
    SpriteAnimation spriteAnimation = await gameRef.loadSpriteAnimation("game/girl_gliding_sheet.png", spriteData);
    var girlAnimation = SpriteAnimationComponent()
      ..animation = spriteAnimation
      ..size = Vector2.all(12)
      ..anchor = Anchor.center;
    add(girlAnimation);
  }

  @override
  Body createBody() {
    final shape = CircleShape()..radius = 6;
    final fixtureDefinition = FixtureDef(shape, density: 1, restitution: 0.4, friction: 0.2);
    final bodyDefinition = BodyDef(position: initialPosition, type: BodyType.dynamic);
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}

class MyGround extends BodyComponent {
  final Vector2 gameSize;

  MyGround(this.gameSize);

  @override
  Body createBody() {
    final shape = EdgeShape()..set(Vector2(0, gameSize.y - 3), Vector2(gameSize.x, gameSize.y - 3));
    final fixtureDefinition = FixtureDef(shape, friction: 1);
    final bodyDefinition = BodyDef(position: Vector2.zero(), userData: this);
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}

List<Wall> createBoundaries(Forge2DGame game) {
  final Vector2 topLeft = Vector2.zero();
  final Vector2 bottomRight = game.screenToWorld(game.camera.viewport.effectiveSize);
  final Vector2 topRight = Vector2(bottomRight.x, topLeft.y);
  final Vector2 bottomLeft = Vector2(topLeft.x, bottomRight.y);

  return [
    Wall(topLeft, topRight),
    Wall(topRight, bottomRight),
    Wall(bottomLeft, topLeft),
    Wall(bottomRight, bottomLeft),
  ];
}

class Wall extends BodyComponent {
  final Vector2 start;
  final Vector2 end;

  Wall(this.start, this.end);

  @override
  Body createBody() {
    paint = BasicPalette.white.paint();
    final shape = EdgeShape()..set(start, end);

    final fixtureDef = FixtureDef(shape)
      ..restitution = 0.0
      ..friction = 0.3;

    final bodyDef = BodyDef()
      ..userData = this // To be able to determine object in collision
      ..position = Vector2.zero()
      ..type = BodyType.static;

    return world.createBody(bodyDef)..createFixture(fixtureDef);
  }
}
