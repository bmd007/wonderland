import 'package:flame/components.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

class MyPlatform extends BodyComponent {
  final Vector2 position;

  MyPlatform(this.position);

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    renderBody = true;
    var girlAnimation = SpriteComponent()
      ..sprite = await gameRef.loadSprite("Tiles/Tile_13.png")
      ..size = Vector2(20, 2)
      ..anchor = Anchor.topCenter;
    add(girlAnimation);
  }

  @override
  Body createBody() {
    PolygonShape shape = PolygonShape()..setAsBoxXY(10, 0.5);
    final fixtureDefinition = FixtureDef(shape, density: 1, restitution: 0.1, friction: 0.3);
    final bodyDefinition = BodyDef(position: position, type: BodyType.static);
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}
