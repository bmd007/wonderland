import 'package:flame/palette.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

List<Wall> createBoundaries(Vector2 screenSize) {
  final Vector2 topLeft = Vector2.zero();
  final Vector2 bottomLeft = Vector2(0, screenSize.y + 0);
  final Vector2 bottomRight = Vector2(screenSize.x, screenSize.y + 0);
  final Vector2 topRight = Vector2(screenSize.x, 0);

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
      ..restitution = 0.1
      ..friction = 0.3;

    final bodyDef = BodyDef()
      ..userData = this // To be able to determine object in collision
      ..position = Vector2.zero()
      ..type = BodyType.static
      ..userData = this;

    return world.createBody(bodyDef)..createFixture(fixtureDef);
  }
}
