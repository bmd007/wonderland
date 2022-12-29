import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/client_holder.dart';
import 'package:dance_partner_finder/client/message_is_sent_to_you_event.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

import 'chat_message.dart';
import 'dancer_chat_event.dart';
import 'dancer_chat_state.dart';

class DancerMatchAndChatBloc
    extends Bloc<DancerChatAndMatchEvent, DancerMatchAndChatState> {
  late final RabbitMqWebSocketStompChatClient chatClient;

  DancerMatchAndChatBloc(String thisDancerName)
      : super(DancerMatchAndChatState.withThisDancerName(thisDancerName)) {
    on<MessagesLoadedEvent>((event, emit) {
      emit(state.loaded(event.chatParticipant, event.loadedMassages));
    });
    on<MessageLoadedEvent>((event, emit) {
      emit(state.addMessage(event.loadedMassage));
    });
    on<MatchFoundEvent>((event, emit) {
      emit(state.addMatch(event.matchName));
    });
    on<WantedToChatEvent>((event, emit) {
      emit(state.chattingWith(event.chatParticipant));
    });
    on<BackToMatchesEvent>((event, emit) {
      emit(state.noMoreChatting());
    });
    on<MessageReceivedEvent>((event, emit) {
      emit(state.addMessage(event.massage));
    });
    on<TextTypedEvent>((event, emit) {
      emit(state.typing(event.text));
    });
    on<DancerWantsToSendMessageEvent>((event, emit) async {
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/chat/messages', data: {
            "type": "MessageIsSentToYouEvent",
            "sender": thisDancerName,
            "receiver": event.massage.participantName,
            "content": event.massage.text
          })
          .asStream()
          .where((event) => event.statusCode == 200)
          .forEach((element) {
            print(element);
            emit(state.addMessage(event.massage));
            emit(state.typing(""));
          });
    });
    ClientHolder.rsocketClient
        .matchStreams(thisDancerName)
        .forEach((match) => add(MatchFoundEvent(match!)));

    chatClient = RabbitMqWebSocketStompChatClient("/queue/$thisDancerName",
        (StompFrame stompFrame) {
      if (stompFrame.headers.containsKey("type") &&
          stompFrame.headers["type"] == "MessageIsSentToYouEvent") {
        var messageIsSentToYouEvent =
            MessageIsSentToYouEvent.fromJson(stompFrame.body!);
        add(MessageReceivedEvent(ChatMessage(messageIsSentToYouEvent.content,
            MessageType.received, messageIsSentToYouEvent.sender)));
      }
    });
  }
}
