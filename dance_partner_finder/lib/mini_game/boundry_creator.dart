import 'package:flame/palette.dart';
import 'package:flame_forge2d/flame_forge2d.dart';

List<Wall> createBoundaries(Forge2DGame game) {
  final Vector2 topLeft = Vector2.zero();
  var worldSizeFromCamera = game.screenToWorld(game.camera.viewport.effectiveSize);
  final Vector2 bottomLeft = Vector2(0, worldSizeFromCamera.y + 0);
  final Vector2 bottomRight = Vector2(worldSizeFromCamera.x, worldSizeFromCamera.y + 0);
  final Vector2 topRight = Vector2(worldSizeFromCamera.x, 0);

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
      ..restitution = 0.0
      ..friction = 0.3;

    final bodyDef = BodyDef()
      ..userData = this // To be able to determine object in collision
      ..position = Vector2.zero()
      ..type = BodyType.static;

    return world.createBody(bodyDef)..createFixture(fixtureDef);
  }
}
