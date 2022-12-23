import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class MyGirlKanui extends BodyComponent {
  late Vector2 initialPosition;
  late SpriteComponent component;

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = false;
    component = SpriteComponent()
      ..sprite = await gameRef.loadSprite("red_girl/Kunai.png", srcSize: Vector2(160, 32))
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
