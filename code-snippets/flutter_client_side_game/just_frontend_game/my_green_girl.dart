import 'package:dance_partner_finder/game_state_repository/movable.dart';
import 'package:dance_partner_finder/just_frontend_game/just_frontend_game.dart';
import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class MyGreenGirl extends SpriteAnimationComponent
    with ContactCallbacks, HasGameRef<JustFrontendGame> {
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
  final String id = "green";
  final Vector2 initialPosition;

  MyGreenGirl(this.initialPosition);


  @override
  Future<void> onLoad() async {
    await super.onLoad();
    glidingAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/gliding_spriteSheet.png", glidingAnimationData);
    runningAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/running_spriteSheet.png", runningAnimationDate);
    idleAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/idle_spriteSheet.png", idleAnimationData);
    jumpingAnimation = await gameRef.loadSpriteAnimation(
        "green_girl/jumping_spriteSheet.png", jumpingAnimationData);

    animation = idleAnimation;
    anchor = Anchor.center;
    size = Vector2(100, 100);
  }

  void handleMovable(Movable movable) async {
    if (movable.linearVelocityY == 0) {
      animation = idleAnimation;
      if (movable.linearVelocityX != 0) {
        animation = runningAnimation;
      }
    } else if (movable.linearVelocityY < -5) {
      animation = jumpingAnimation;
    } else if (movable.linearVelocityY > 5) {
      animation = glidingAnimation;
    }
    position.x = movable.initialPositionX;
    position.y = movable.initialPositionY;
    angle = movable.initialAngel;
  }
}
