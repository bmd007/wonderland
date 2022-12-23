import 'dart:collection';

import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

import 'my_girl_kanui.dart';

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
  final JoystickComponent joystick;
  final Vector2 initialPosition;
  late SpriteAnimationComponent component;
  Queue<MyGirlKanui> kanuies = Queue<MyGirlKanui>();

  MyGirl(this.joystick, this.initialPosition);

  void move(double dt) {
    var direction = joystick.direction;
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
      print(joystick.relativeDelta);
      body.applyLinearImpulse(Vector2(joystick.relativeDelta.x * 1200, joystick.relativeDelta.y * 1200));
    } else if (direction == JoystickDirection.upRight && landedSinceLastElevation) {
      if (!lookingTowardRight) {
        component.flipHorizontally();
      }
      lookingTowardRight = true;
      body.linearVelocity.x = 0;
      landedSinceLastElevation = false;
      body.applyLinearImpulse(Vector2(joystick.relativeDelta.x * 1200, joystick.relativeDelta.y * 1200));
    }
  }

  @override
  void update(double dt) {
    super.update(dt);
    landedSinceLastElevation = body.linearVelocity.y == 0;

    if (body.linearVelocity.y == 0) {
      component.animation = idleAnimation;
    } else if (body.linearVelocity.y < -5) {
      component.animation = jumpingAnimation;
    } else if (body.linearVelocity.y > 5) {
      component.animation = glidingAnimation;
    }
    if (!joystick.delta.isZero()) {
      move(dt);
    } else if (landedSinceLastElevation) {
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

    component = SpriteAnimationComponent()
      ..animation = idleAnimation
      ..size = Vector2.all(6)
      ..anchor = Anchor.center;
    add(component);
    camera.followBodyComponent(this, useCenterOfMass: false);
    camera.zoom = 9;

    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
    kanuies.add(MyGirlKanui());
  }

  @override
  Body createBody() {
    final shape = PolygonShape()..setAsBoxXY(3, 3);
    final fixtureDefinition = FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
    final bodyDefinition = BodyDef(position: initialPosition, type: BodyType.dynamic)..fixedRotation = true;
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }

  throwKanui() async {
    if (kanuies.isNotEmpty) {
      var kanui = kanuies.removeFirst();
      kanui.initialPosition = body.position;
      await parent?.add(kanui);
      kanui.body.linearVelocity.x = lookingTowardRight ? 60 : -60;
      kanui.body.linearVelocity.y = 0;
    }
  }
}
