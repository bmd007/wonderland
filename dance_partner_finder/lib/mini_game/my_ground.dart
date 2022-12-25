import 'package:flame_forge2d/flame_forge2d.dart';

class MyGround extends BodyComponent {
  final Vector2 gameSize;

  MyGround(this.gameSize);

  @override
  Body createBody() {
    final shape = EdgeShape()..set(Vector2(0, gameSize.y - 3 + 300), Vector2(gameSize.x, gameSize.y - 3 + 3000));
    final fixtureDefinition = FixtureDef(shape, friction: 20);
    final bodyDefinition = BodyDef(position: Vector2.zero(), userData: this)..userData = this;
    return world.createBody(bodyDefinition)..createFixture(fixtureDefinition);
  }
}
