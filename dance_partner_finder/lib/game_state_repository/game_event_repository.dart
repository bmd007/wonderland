import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:dance_partner_finder/game_state_repository/observer.dart';
import 'package:dance_partner_finder/game_state_repository/remote_game_state.dart';
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
          stompFrame.headers["type"] == "game_state") {
        var remoteGameState = RemoteGameState.fromJson(stompFrame.body!);
        for (var element in observers) {
          element.notifyGameState(remoteGameState);
        }
      }
    });
  }

  void sendJoystickEvent(JoystickMovedEvent event) {}
}
