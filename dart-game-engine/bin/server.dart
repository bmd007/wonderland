import 'dart:convert';

import "package:dart_amqp/dart_amqp.dart";
import 'package:dart_game_engine/movable.dart';
import 'package:dart_game_engine/wall.dart';
import 'package:forge2d/forge2d.dart';
import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';

// Configure routes.
final _router = Router()
  ..get('/', _rootHandler)
  ..get('/echo/<message>', _echoHandler);

Response _rootHandler(Request req) {
  return Response.ok('Hello, World!\n');
}

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
  // final port = int.parse(Platform.environment['PORT'] ?? '8090');
  // final server = await serve(handler, ip, port);
  // print('Server listening on port ${server.port}');

  // final channel = await client.channel();
  // final mm7q = await channel.queue("mm7amini@gmail.com_game");
  // mm7q.purge();
  // final exchange =
  //     await channel.exchange(gameMessageExchange, ExchangeType.DIRECT);

  World world = World(Vector2(0, -10));

  var size = Vector2(1366, 768);

  var right = size.x + 100;
  var bottom = size.y;
  final Vector2 topLeft = Vector2.zero() + Vector2(5, 5);
  final Vector2 topRight = Vector2(right, 0) + Vector2(-5, 5);
  final Vector2 bottomLeft = Vector2(0, bottom) + Vector2(5, -5);
  final Vector2 bottomRight = Vector2(right, bottom) + Vector2(-5, -5);
  addToWorld(Wall(topLeft, topRight), world);
  addToWorld(Wall(topRight, bottomRight), world);
  addToWorld(Wall(bottomLeft, topLeft), world);
  addToWorld(Wall(bottomRight, bottomLeft), world);

  final shape = CircleShape()..radius = 3;
  final fixtureDefinition =
      FixtureDef(shape, density: 2, restitution: 0.1, friction: 2);
  final bodyDefinition = BodyDef(position: size / 2, type: BodyType.dynamic)
    ..fixedRotation = true
    ..userData = "ninja";
  var ninja = world.createBody(bodyDefinition);
  ninja.createFixture(fixtureDefinition);

  world.drawDebugData();
  double drawInterval = 1000000000.0 / 60;
  double delta = 0;
  var lastTime = DateTime.now().microsecondsSinceEpoch;
  var currentTime = 0;
  var timer = 0;
  int drawCount = 0;

  while (true) {
    currentTime = DateTime.now().microsecondsSinceEpoch;
    delta += (currentTime - lastTime) / drawInterval;
    timer += (currentTime - lastTime);
    lastTime = currentTime;
    if (delta >= 1) {
      world.stepDt(delta);

      print("${ninja.linearVelocity} : ${ninja.position}");

      delta--;
      drawCount++;
    }

    if (timer > 1000000000) {
      print("FPS: $drawCount");
      drawCount = 0;
      timer = 0;
    }
  }

  // sendGameState(
  //     exchange,
  //     Movable(id, initialPositionX, initialPositionY, initialAngel,
  //         linearVelocityX, linearVelocityY, angularVelocity));

  // client.close();
}

void addToWorld(Wall wall, World world) {
  wall.createBody(world);
}

void sendGameState(Exchange exchange, Movable movable) {
  var messageProperties = MessageProperties.persistentMessage()
    ..appId = "wonderland.message-publisher"
    ..headers = {"type": "game_state"};
  var message = jsonEncode(movable);
  exchange.publish(message, "mm7amini@gmail.com",
      properties: messageProperties);
}
