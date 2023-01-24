import 'dart:convert';

import "package:dart_amqp/dart_amqp.dart";
import 'package:dart_game_engine/game_loop.dart';
import 'package:dart_game_engine/movable.dart';
import 'package:dart_game_engine/wall.dart';
import 'package:forge2d/forge2d.dart';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';

// Configure routes.
final _router = Router()..get('/echo/<message>', _echoHandler);

Response _echoHandler(Request request) {
  final message = request.params['message'];
  return Response.ok('$message\n');
}

const gameMessageExchange = "messages/game";
final settings = ConnectionSettings(
    host: "localhost",
    port: 5672,
    authProvider: PlainAuthenticator("imarabbit", "noyouarenot"));
final client = Client(settings: settings);

void main(List<String> args) async {
  // final ip = InternetAddress.anyIPv4;
  // final handler = Pipeline().addMiddleware(logRequests()).addHandler(_router);
  // final port = int.parse(Platform.environment['PORT'] ?? '8080');
  // final server = await serve(handler, ip, port);
  // print('Server listening on port ${server.port}');

  final channel = await client.channel();
  final mm7q = await channel.queue("mm7amini@gmail.com_game", durable: true);
  mm7q.purge();
  final exchange =
      await channel.exchange(gameMessageExchange, ExchangeType.DIRECT, durable: true);

  final world = World(Vector2(0, 10));
  final size = Vector2(1366, 768);
  final right = size.x + 100;
  final bottom = size.y;
  final Vector2 topLeft = Vector2.zero() + Vector2(5, 5);
  final Vector2 topRight = Vector2(right, 0) + Vector2(-5, 5);
  final Vector2 bottomLeft = Vector2(0, bottom) + Vector2(5, -5);
  final Vector2 bottomRight = Vector2(right, bottom) + Vector2(-5, -5);
  addWallToWorld(Wall(topLeft, topRight), world);
  addWallToWorld(Wall(topRight, bottomRight), world);
  addWallToWorld(Wall(bottomLeft, topLeft), world);
  addWallToWorld(Wall(bottomRight, bottomLeft), world);

  final shape = CircleShape()..radius = 3;
  final fixtureDefinition =
      FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
  final bodyDefinition = BodyDef(position: size / 6, type: BodyType.dynamic)
    ..fixedRotation = true
    ..userData = "green";
  final ninja = world.createBody(bodyDefinition);
  ninja.createFixture(fixtureDefinition);

  final loop = GameLoop(onTick: (tick) {
    world.stepDt(tick);
    // print("${ninja.linearVelocity} : ${ninja.position}");
    sendGameState(
        exchange,
        Movable(
            "green",
            ninja.position.x,
            ninja.position.y,
            ninja.angle,
            ninja.linearVelocity.x,
            ninja.linearVelocity.y,
            ninja.angularVelocity));
  });
  loop.play();


  // client.close();
}

void addWallToWorld(Wall wall, World world) {
  wall.createBody(world);
}

Future<void> sendGameState(Exchange exchange, Movable movable) async {
  var messageProperties = MessageProperties.persistentMessage()
    ..appId = "wonderland.message-publisher"
    ..headers = {"type": "game_state"};
  exchange.publish(movable.toJson(), "mm7amini@gmail.com", properties: messageProperties);
}
