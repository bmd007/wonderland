import 'dart:collection';

import 'package:dance_partner_finder/game_state_repository/movable.dart';
import 'package:dance_partner_finder/game_state_repository/observer.dart';
import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';
import 'package:flutter/foundation.dart';

import 'bullet.dart';
import 'my_girl_kanui.dart';

class MyGreenGirl<MyForge2DFlameGame> extends BodyComponent
    with ContactCallbacks
    implements Observer {
  SpriteAnimationData glidingAnimationData = SpriteAnimationData.sequenced(
      amount: 9, stepTime: 0.03, textureSize: Vector2(152.0, 142.0));
  SpriteAnimationData runningAnimationDate = SpriteAnimationData.sequenced(
      amount: 9, stepTime: 0.03, textureSize: Vector2(375.0, 520.0));
  SpriteAnimationData idleAnimationData = SpriteAnimationData.sequenced(
      amount: 9, stepTime: 0.03, textureSize: Vector2(290.0, 500.0));
  SpriteAnimationData jumpingAnimationData = SpriteAnimationData.sequenced(
      amount: 9, stepTime: 0.03, textureSize: Vector2(399.0, 543.0));
  late SpriteAnimation glidingAnimation;
  late SpriteAnimation runningAnimation;
  late SpriteAnimation idleAnimation;
  late SpriteAnimation jumpingAnimation;
  bool lookingTowardRight = true;
  bool landedSinceLastElevation = false;
  final double speed = 20;
  final Vector2 initialPosition;
  late SpriteAnimationComponent component;
  Queue<MyGirlKanui> kanuies = Queue<MyGirlKanui>();
  final playerLife = ValueNotifier<int>(100);

  MyGreenGirl(this.initialPosition);

  @override
  void update(double dt) {
    super.update(dt);
    if (playerLife.value <= 0) {
      removeFromParent();
    }
    landedSinceLastElevation = body.linearVelocity.y == 0;

    if (body.linearVelocity.y == 0) {
      component.animation = idleAnimation;
    } else if (body.linearVelocity.y < -5) {
      component.animation = jumpingAnimation;
    } else if (body.linearVelocity.y > 5) {
      component.animation = glidingAnimation;
    }

    if (landedSinceLastElevation) {
      body.linearVelocity.x = 0;
    }
  }

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = false;
    glidingAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/gliding_spriteSheet.png", glidingAnimationData);
    runningAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/running_spriteSheet.png", runningAnimationDate);
    idleAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/idle_spriteSheet.png", idleAnimationData);
    jumpingAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/jumping_spriteSheet.png", jumpingAnimationData);

    component = SpriteAnimationComponent()
      ..animation = idleAnimation
      ..anchor = Anchor.center
      ..size = Vector2.all(6)
      ..anchor = Anchor.center;
    add(component);

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
    final shape = CircleShape()..radius = 3;
    final fixtureDefinition =
        FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
    final bodyDefinition =
        BodyDef(position: initialPosition, type: BodyType.dynamic)
          ..fixedRotation = true
          ..userData = this;
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }

  throwKanui() async {
    if (kanuies.isNotEmpty) {
      var kanui = kanuies.removeFirst();
      var positionDelta = lookingTowardRight
          ? Vector2(component.x + 5, 0)
          : Vector2(-component.x - 5, 0);
      kanui.initialPosition = body.position + positionDelta;
      await parent?.add(kanui);
      if (!lookingTowardRight) {
        kanui.component.flipHorizontally();
      }
      kanui.body.linearVelocity.x = lookingTowardRight ? 80 : -80;
      kanui.body.linearVelocity.y = -5;
    }
  }

  @override
  void beginContact(Object other, Contact contact) {
    if (other is Bullet) {
      playerLife.value = playerLife.value - 1;
    }
  }

  @override
  void notifyGameState(Movable ninja) {
    body.linearVelocity = Vector2(ninja.linearVelocityX, ninja.linearVelocityY);
    body.angularVelocity = ninja.angularVelocity;
  }
}
