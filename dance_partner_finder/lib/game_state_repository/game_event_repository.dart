import 'package:dance_partner_finder/client/client_holder.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:dance_partner_finder/game_state_repository/observer.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

import 'game_event.dart';

class GameEventRepository {
  late final RabbitMqWebSocketStompChatClient chatClient;
  final String thisPlayerName;
  Set<Observer> observers = {};

  GameEventRepository(this.thisPlayerName) {
    chatClient = RabbitMqWebSocketStompChatClient(
        "/queue/${thisPlayerName}_game", (StompFrame stompFrame) {
      if (stompFrame.headers.containsKey("type") &&
          stompFrame.headers["type"] == "game.input.joystick") {
        var joystickMovedMessageReceivedEvent =
            JoystickMovedMessageReceivedEvent.fromJson(stompFrame.body!);
        for (var element in observers) {
          element.notifyGameEvent(joystickMovedMessageReceivedEvent);
        }
      } else if (stompFrame.headers.containsKey("type") &&
          stompFrame.headers["type"] == "game.input.button.shoot") {}
    });
  }

  void sendJoystickEvent(JoystickMovedEvent event) async {
    await ClientHolder.apiGatewayHttpClient
        .post('/v1/game/messages', data: {
          "headers": {"type", "game.input.joystick"},
          "sender": thisPlayerName,
          "receiver": thisPlayerName,
          "content": {
            "direction": event.direction.name,
            "relativeDeltaX": event.relativeDelta.x,
            "relativeDeltaY": event.relativeDelta.y,
          }
        })
        .asStream()
        .where((event) => event.statusCode == 200)
        .forEach((element) {
          print(element);
        });
  }
}
