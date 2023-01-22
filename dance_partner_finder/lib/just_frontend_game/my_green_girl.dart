import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class MyGreenGirl<JustFrontendGame> extends BodyComponent
    with ContactCallbacks {
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
  final double speed = 20;
  final String id;
  final Vector2 initialPosition;
  late SpriteAnimationComponent component;

  MyGreenGirl(this.initialPosition, this.id);

  @override
  void update(double dt) {
    super.update(dt);
    if (body.linearVelocity.y == 0) {
      component.animation = idleAnimation;
      if (body.linearVelocity.x != 0) {
        component.animation = runningAnimation;
      }
    } else if (body.linearVelocity.y < -5) {
      component.animation = jumpingAnimation;
    } else if (body.linearVelocity.y > 5) {
      component.animation = glidingAnimation;
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
}
