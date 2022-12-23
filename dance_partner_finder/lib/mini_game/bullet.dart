import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class Bullet extends BodyComponent {
  final Vector2 initialPosition;
  late SpriteComponent component;

  Bullet(this.initialPosition);

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = false;
    component = SpriteComponent()
      ..sprite = await gameRef.loadSprite("TeamGunner/EXTRAS/BulletStream.png", srcSize: Vector2(80, 16))
      ..size = Vector2(2, 1)
      ..anchor = Anchor.center;
    add(component);
  }

  @override
  Body createBody() {
    final shape = PolygonShape()..setAsBoxXY(1, 0.5);
    final fixtureDefinition = FixtureDef(shape, density: 1, restitution: 0.1, friction: 0.3);
    final bodyDefinition = BodyDef(position: initialPosition, type: BodyType.dynamic)
      ..fixedRotation = true
      ..bullet = true;
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}
