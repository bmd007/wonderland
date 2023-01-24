import 'package:forge2d/forge2d.dart';

class Wall {
  final Vector2 start;
  final Vector2 end;

  Wall(this.start, this.end);

  Body createBody(World world) {
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
