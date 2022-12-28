import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/client_holder.dart';
import 'package:dance_partner_finder/client/message_is_sent_to_you_event.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

import 'chat_message.dart';
import 'game_event.dart';
import 'game_state.dart';

class GameBloc extends Bloc<GameEvent, GameState> {
  late final RabbitMqWebSocketStompChatClient chatClient;

  GameBloc(String thisPlayerName)
      : super(GameState.withThisPlayerName(thisPlayerName)) {
    on<JoystickMovedMessageReceivedEvent>((event, emit) {

    });

    on<JoystickMovedEvent>((event, emit) async {
      // await ClientHolder.apiGatewayHttpClient
      //     .post('/v1/game/messages', data: {
      //       "headers": {"type", "game.input.joystick"},
      //       "sender": thisPlayerName,
      //       "receiver": event.massage.participantName,
      //       "content": event.massage.text
      //     })
      //     .asStream()
      //     .where((event) => event.statusCode == 200)
      //     .forEach((element) {
      //       print(element);
      //       // emit(state.addMessage(event.massage));
      //     });
    });

    on<ShootButtonPushedEvent>((event, emit) async {
      // await ClientHolder.apiGatewayHttpClient
      //     .post('/v1/game/messages', data: {
      //       "headers": {"type", "game.input.button.shoot"},
      //       "sender": thisPlayerName,
      //       "receiver": event.massage.participantName,
      //       "content": event.massage.text
      //     })
      //     .asStream()
      //     .where((event) => event.statusCode == 200)
      //     .forEach((element) {
      //       print(element);
      //       // emit(state.addMessage(event.massage));
      //     });
    });

    chatClient = RabbitMqWebSocketStompChatClient("/queue/$thisPlayerName",
        (StompFrame stompFrame) {
      if (stompFrame.headers.containsKey("type") && stompFrame.headers["type"] == "game.input.joystick") {
        // var messageIsSentToYouEvent =
        //     MessageIsSentToYouEvent.fromJson(stompFrame.body!);
        // add(JoystickMovedMessageReceivedEvent(ChatMessage(
        //     messageIsSentToYouEvent.content,
        //     MessageType.received,
        //     messageIsSentToYouEvent.sender)));
      } else if (stompFrame.headers.containsKey("type") && stompFrame.headers["type"] == "game.input.button.shoot") {

      }
    });
  }
}
