import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class MyGirl extends BodyComponent {
  SpriteAnimationData glidingAnimationData =
  SpriteAnimationData.sequenced(amount: 9, stepTime: 0.03, textureSize: Vector2(152.0, 142.0));
  SpriteAnimationData runningAnimationDate =
  SpriteAnimationData.sequenced(amount: 9, stepTime: 0.03, textureSize: Vector2(375.0, 520.0));
  SpriteAnimationData idleAnimationData =
  SpriteAnimationData.sequenced(amount: 9, stepTime: 0.03, textureSize: Vector2(290.0, 500.0));
  SpriteAnimationData jumpingAnimationData =
  SpriteAnimationData.sequenced(amount: 9, stepTime: 0.03, textureSize: Vector2(399.0, 543.0));
  late SpriteAnimation glidingAnimation;
  late SpriteAnimation runningAnimation;
  late SpriteAnimation idleAnimation;
  late SpriteAnimation jumpingAnimation;
  bool lookingTowardRight = true;
  bool landedSinceLastElevation = false;
  final double speed = 20;
  JoystickComponent joystick;
  late Vector2 initialPosition;
  late double groundLevel;
  late SpriteAnimationComponent girlComponent;

  MyGirl(Vector2 gameSize, this.joystick) {
    initialPosition = gameSize / 2;
    groundLevel = gameSize.y + 0 - 3;
    print(gameSize);
    print(groundLevel);
  }

  void move(double dt) {
    var direction = joystick.direction;
    if (direction == JoystickDirection.down) {
      girlComponent.animation = idleAnimation;
      body.linearVelocity.x = 0;
    } else if (direction == JoystickDirection.downLeft || direction == JoystickDirection.left) {
      if (lookingTowardRight) {
        girlComponent.flipHorizontally();
      }
      lookingTowardRight = false;
      if (body.linearVelocity.y == 0) {
        body.linearVelocity = Vector2(-speed, body.linearVelocity.y);
      }
      girlComponent.animation = runningAnimation;
    } else if (direction == JoystickDirection.downRight || direction == JoystickDirection.right) {
      if (!lookingTowardRight) {
        girlComponent.flipHorizontally();
      }
      lookingTowardRight = true;
      if (body.linearVelocity.y == 0) {
        body.linearVelocity = Vector2(speed, body.linearVelocity.y);
      }
      girlComponent.animation = runningAnimation;
    } else if ((direction == JoystickDirection.up ||
        direction == JoystickDirection.upRight ||
        direction == JoystickDirection.upLeft) &&
        landedSinceLastElevation) {
      landedSinceLastElevation = false;
      body.applyLinearImpulse(Vector2(0, 10000));
    }
  }

  @override
  void update(double dt) {
    super.update(dt);
    body.angularVelocity = 0;
    landedSinceLastElevation = body.linearVelocity.y == 0;

    if (body.linearVelocity.y == 0) {
      girlComponent.animation = idleAnimation;
    } else if (body.linearVelocity.y < -5) {
      girlComponent.animation = jumpingAnimation;
    } else if (body.linearVelocity.y > 5) {
      girlComponent.animation = glidingAnimation;
    }
    if (!joystick.delta.isZero()) {
      move(dt);
    } else {
      body.linearVelocity.x = 0;
    }
  }

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = false;
    glidingAnimation = await gameRef.loadSpriteAnimation("red_girl/gliding_spriteSheet.png", glidingAnimationData);
    runningAnimation = await gameRef.loadSpriteAnimation("red_girl/running_spriteSheet.png", runningAnimationDate);
    idleAnimation = await gameRef.loadSpriteAnimation("red_girl/idle_spriteSheet.png", idleAnimationData);
    jumpingAnimation = await gameRef.loadSpriteAnimation("red_girl/jumping_spriteSheet.png", jumpingAnimationData);

    girlComponent = SpriteAnimationComponent()
      ..animation = idleAnimation
      ..size = Vector2.all(6)
      ..anchor = Anchor.center;
    add(girlComponent);
    camera.followBodyComponent(this, useCenterOfMass: false);
    camera.zoom = 15;
  }

  @override
  Body createBody() {
    final shape = PolygonShape()..setAsBoxXY(3, 3);
    final fixtureDefinition = FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
    final bodyDefinition = BodyDef(position: initialPosition, type: BodyType.dynamic);
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}
