import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:http/http.dart' as http;
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
    on<DancerSendMessageEvent>((event, emit) {
      //todo send to backend as request and emit upon 200
      emit(state.addMessage(event.massage));
    });

    http.post(Uri.parse(
        'http://192.168.1.188:9531/v1/chat/$thisDancerName/queues')); //todo this call doesn't really belong here

    ClientHolder.client.matchStreams(state.thisDancerName).forEach((match) => add(MatchFoundEvent(match!)));

    chatClient = RabbitMqWebSocketStompChatClient(thisDancerName, handleMessages);

    add(const DancerSendMessageEvent(ChatMessage("text to taylor", MessageType.sent, "taylor")));
    add(const DancerSendMessageEvent(ChatMessage("text to taylor2", MessageType.sent, "taylor")));
    add(const DancerSendMessageEvent(ChatMessage("text to taylor3", MessageType.sent, "taylor")));
    add(const MessageReceivedEvent(ChatMessage("text from taylor", MessageType.received, "taylor")));
  }

  void handleMessages(StompFrame stompFrame) {
    print("bmd message received is: ${stompFrame.body} ${stompFrame.headers}");
  }

}
