import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:http/http.dart' as http;
import 'package:stomp_dart_client/stomp_frame.dart';

import 'dancer_chat_event.dart';
import 'dancer_chat_state.dart';

class DancerChatBloc extends Bloc<DancerChatEvent, DancerChatState> {
  late final RabbitMqWebSocketStompChatClient chatClient;

  DancerChatBloc(String thisDancerName)
      : super(DancerChatState.withThisDancerName(thisDancerName)) {
    on<MessagesLoadedEvent>((event, emit) {
      emit(state.loaded(event.chatParticipant, event.loadedMassages));
    });
    on<MessageLoadedEvent>((event, emit) {
      emit(state.addMessage(event.chatParticipant, event.loadedMassage));
    });

    emit(state.loading());

    http.post(Uri.parse(
        'http://192.168.1.188:9531/v1/chat/$thisDancerName/queues')); //todo this call doesn't really belong here
    chatClient = RabbitMqWebSocketStompChatClient(thisDancerName, handleMessages);
  }

  void handleMessages(StompFrame stompFrame) {
    print("bmd message received is: ${stompFrame.body} ${stompFrame.headers}");
  }
}
