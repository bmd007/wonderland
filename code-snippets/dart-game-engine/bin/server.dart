import "package:dart_amqp/dart_amqp.dart";
import 'package:dart_game_engine/game_loop.dart';
import 'package:dart_game_engine/movable.dart';
import 'package:dart_game_engine/ninja.dart';
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

  Wall(topLeft, topRight, world);
  Wall(topRight, bottomRight, world);
  Wall(bottomLeft, topLeft, world);
  Wall(bottomRight, bottomLeft, world);
  var green = Ninja(world, "green", size / 6);
  var red = Ninja(world, "red", size / 5);

  final loop = GameLoop(onTick: (tick) {
    world.stepDt(tick);
    sendGameState(exchange, red);
    sendGameState(exchange, green);
  });
  loop.play();

  // client.close();
}

Future<void> sendGameState(Exchange exchange, Ninja ninja) async {
  if (!ninja.body.isAwake) {
    return;
  }
  var movable = Movable(
      ninja.id,
      ninja.body.position.x,
      ninja.body.position.y,
      ninja.body.angle,
      ninja.body.linearVelocity.x,
      ninja.body.linearVelocity.y,
      ninja.body.angularVelocity);
  var messageProperties = MessageProperties.persistentMessage()
    ..appId = "wonderland.message-publisher"
    ..headers = {"type": "game_state"};
  exchange.publish(movable.toJson(), "mm7amini@gmail.com",
      properties: messageProperties);
}
// TODO add flip state to movable DTO and keep track of it in Ninja class
