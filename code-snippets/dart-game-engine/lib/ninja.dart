import 'package:forge2d/forge2d.dart';

class Ninja {
  final String id;
  final Vector2 initialLocation;
  final World world;
  late final Body body;

  Ninja(this.world, this.id, this.initialLocation) {
    final shape = CircleShape()..radius = 3;
    final fixtureDefinition =
    FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
    final bodyDefinition = BodyDef(position: initialLocation, type: BodyType.dynamic)
      ..fixedRotation = true
      ..userData = this;
    final ninja = world.createBody(bodyDefinition);
    ninja.createFixture(fixtureDefinition);

    body = world.createBody(bodyDefinition);
    body.createFixture(fixtureDefinition);
  }
}
