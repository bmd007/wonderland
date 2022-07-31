import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:dance_partner_finder/client/message_is_sent_to_you_event.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:rxdart/rxdart.dart';
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
    on<DancerSendMessageEvent>((event, emit) async {
      //todo some sort of refactoring here if possible!?
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/chat/messages', data: {
            "sender": thisDancerName,
            "receiver": event.massage.participantName,
            "content": event.massage.text
          })
          .asStream()
          .doOnError((p0, p1) => print(p0))
          .doOnError((p0, p1) => print(p1))
          .where((event) => event.statusCode == 200)
          .forEach((element) {
            print(element);
            emit(state.addMessage(event.massage));
          });
    });

    ClientHolder.client.matchStreams(thisDancerName).forEach((match) => add(MatchFoundEvent(match!)));

    chatClient = RabbitMqWebSocketStompChatClient(thisDancerName, (StompFrame stompFrame) {
      if (stompFrame.headers.containsKey("type") && stompFrame.headers["type"] == "MessageIsSentToYouEvent") {
        var messageIsSentToYouEvent = MessageIsSentToYouEvent.fromJson(stompFrame.body!);
        add(MessageReceivedEvent(
            ChatMessage(messageIsSentToYouEvent.content, MessageType.received, messageIsSentToYouEvent.sender)));
      }
    });
  }
}
