import 'package:forge2d/forge2d.dart';

class Wall {
  final Vector2 start;
  final Vector2 end;
  final World world;
  late final Body body;

  Wall(this.start, this.end, this.world) {
    final shape = EdgeShape()..set(start, end);

    final fixtureDefinition = FixtureDef(shape)
      ..restitution = 0.1
      ..friction = 0.3;

    final bodyDefinition = BodyDef()
      ..userData = this
      ..position = Vector2.zero()
      ..type = BodyType.static
      ..userData = this;

    body = world.createBody(bodyDefinition);
    body.createFixture(fixtureDefinition);
  }
}
