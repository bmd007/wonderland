import 'dart:math';

import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

import 'bullet.dart';

class Enemy extends BodyComponent {
  SpriteAnimationData runningAnimationDate =
      SpriteAnimationData.sequenced(amount: 5, stepTime: 0.03, textureSize: Vector2(286 / 6, 48.0));
  SpriteAnimationData idleAnimationData =
      SpriteAnimationData.sequenced(amount: 4, stepTime: 0.03, textureSize: Vector2(240.0 / 5, 48));
  SpriteAnimationData jumpingAnimationData =
      SpriteAnimationData.sequenced(amount: 1, stepTime: 0.03, textureSize: Vector2(96.0 / 2, 48.0));
  SpriteAnimationData dyingAnimationData =
      SpriteAnimationData.sequenced(amount: 7, stepTime: 0.03, textureSize: Vector2(384.0 / 8, 48.0));
  late SpriteAnimation runningAnimation;
  late SpriteAnimation idleAnimation;
  late SpriteAnimation jumpingAnimation;
  late SpriteAnimation dyingAnimation;
  bool lookingTowardRight = true;
  bool landedSinceLastElevation = false;
  final double speed = 20;
  final Vector2 initialPosition;
  late SpriteAnimationComponent component;

  Enemy(this.initialPosition);

  void move(double dt, JoystickDirection direction, Vector2 relativeDelta) {
    if (direction == JoystickDirection.down) {
      component.animation = idleAnimation;
      if (landedSinceLastElevation) {
        body.linearVelocity.x = 0;
      }
    } else if (direction == JoystickDirection.downLeft || direction == JoystickDirection.left) {
      if (lookingTowardRight) {
        component.flipHorizontally();
      }
      lookingTowardRight = false;
      if (body.linearVelocity.y == 0) {
        body.linearVelocity = Vector2(-speed, body.linearVelocity.y);
      }
      component.animation = runningAnimation;
    } else if (direction == JoystickDirection.downRight || direction == JoystickDirection.right) {
      if (!lookingTowardRight) {
        component.flipHorizontally();
      }
      lookingTowardRight = true;
      if (body.linearVelocity.y == 0) {
        body.linearVelocity = Vector2(speed, body.linearVelocity.y);
      }
      component.animation = runningAnimation;
    } else if (direction == JoystickDirection.up && landedSinceLastElevation) {
      landedSinceLastElevation = false;
      body.applyLinearImpulse(Vector2(0, -1200));
    } else if (direction == JoystickDirection.upLeft && landedSinceLastElevation) {
      if (lookingTowardRight) {
        component.flipHorizontally();
      }
      lookingTowardRight = false;
      landedSinceLastElevation = false;
      body.linearVelocity.x = 0;
      body.applyLinearImpulse(Vector2(relativeDelta.x * 1200, relativeDelta.y * 1200));
    } else if (direction == JoystickDirection.upRight && landedSinceLastElevation) {
      if (!lookingTowardRight) {
        component.flipHorizontally();
      }
      lookingTowardRight = true;
      body.linearVelocity.x = 0;
      landedSinceLastElevation = false;
      body.applyLinearImpulse(Vector2(relativeDelta.x * 1200, relativeDelta.y * 1200));
    }
  }

  @override
  void update(double dt) {
    super.update(dt);
    if (dt.ceil() % 2 == 0) {
      shootBullet();
    }

    landedSinceLastElevation = body.linearVelocity.y == 0;

    if (body.linearVelocity.y == 0) {
      component.animation = idleAnimation;
    } else if (body.linearVelocity.y != 0) {
      component.animation = jumpingAnimation;
    }
    if (dt.ceil() % 2 == 0) {
      var direction = JoystickDirection.values[Random.secure().nextInt(8)];
      move(dt, direction, Vector2(10000, 10000));
    } else if (landedSinceLastElevation) {
      body.linearVelocity.x = 0;
    }
  }

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = true;
    runningAnimation = await gameRef.loadSpriteAnimation(
        "TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Run.png", runningAnimationDate);
    idleAnimation = await gameRef.loadSpriteAnimation(
        "TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Idle.png", idleAnimationData);
    jumpingAnimation = await gameRef.loadSpriteAnimation(
        "TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Jump.png", jumpingAnimationData);
    dyingAnimation = await gameRef.loadSpriteAnimation(
        "TeamGunner/CHARACTER_SPRITES/Green/Gunner_Green_Death.png", dyingAnimationData);

    component = SpriteAnimationComponent()
      ..animation = idleAnimation
      ..size = Vector2.all(7)
      ..anchor = Anchor.center;
    add(component);
  }

  @override
  Body createBody() {
    final shape = PolygonShape()..setAsBoxXY(2, 2);
    final fixtureDefinition = FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
    final bodyDefinition = BodyDef(position: initialPosition, type: BodyType.dynamic)..fixedRotation = true;
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }

  shootBullet() async {
    Bullet bullet = Bullet(body.position);
    await parent?.add(bullet);
    bullet.body.linearVelocity.x = lookingTowardRight ? 20 : -20;
    bullet.body.linearVelocity.y = 0;
  }
}
