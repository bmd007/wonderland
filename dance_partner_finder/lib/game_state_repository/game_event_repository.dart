import 'dart:collection';

import 'package:dance_partner_finder/client/client_holder.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

import 'game_event.dart';
import 'movable.dart';

class GameEventRepository {
  late final RabbitMqWebSocketStompChatClient chatClient;
  final String thisPlayerName;
  Queue<Movable> movables = Queue<Movable>();

  GameEventRepository(this.thisPlayerName) {
    chatClient = RabbitMqWebSocketStompChatClient(
        "/queue/${thisPlayerName}_game", (StompFrame stompFrame) {
      if (stompFrame.headers.containsKey("type") &&
          stompFrame.headers["type"] == "game_state") {
        var ninja = Movable.fromJson(stompFrame.body!);
        movables.add(ninja);
      }
    });
  }

  void sendJoystickEvent(JoystickMovedEvent event) async {
    await ClientHolder.apiGatewayHttpClient
        .post('/v1/game/report/input/joystick', data: {
      "relativeDeltaX": event.relativeDelta.x,
      "relativeDeltaY": event.relativeDelta.y,
      "direction": event.direction.name
    })
        .asStream()
        .where((event) => event.statusCode == 200)
        .forEach((element) {});
  }

  void sendNinjaLocationToBeEchoedBack(final String id,
      final double linearVelocityX,
      final double linearVelocityY,
      final double angularVelocity,) async {
    var body = {
      "id": id,
      "linearVelocityX": linearVelocityX,
      "linearVelocityY": linearVelocityY,
      "angularVelocity": angularVelocity
    };
    ClientHolder.apiGatewayHttpClient
        .post('/v1/game/state/echo', data: body)
        .asStream()
        .where((event) => event.statusCode == 200)
        .forEach((element) {});
  }
}
