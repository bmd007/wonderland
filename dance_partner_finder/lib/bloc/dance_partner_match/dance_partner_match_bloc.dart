
import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:http/http.dart' as http;
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';

import 'dance_partner_match_event.dart';
import 'dance_partner_match_state.dart';

class DancePartnerMatchBloc
    extends Bloc<DancePartnerMatchEvent, DancePartnerMatchState> {
  late StompClient client;

  DancePartnerMatchBloc(String thisDancerName)
      : super(DancePartnerMatchState.withThisDancerName(thisDancerName)) {
    on<DancerMatchesLoadedEvent>((event, emit) {
      emit(state.loaded(event.loadedDancerNames));
    });
    on<MatchFoundEvent>((event, emit) {
      emit(state.addMatch(event.dancePartnerMatchName));
    });

    emit(state.loading());

    http.post(Uri.parse(
        'http://192.168.1.188:9531/v1/chat/$thisDancerName/queues')); //todo this call doesn't really belong here

    ApiGatewayClientHolder.client
        .matchStreams(state.thisDancerName)
        .forEach((match) => add(MatchFoundEvent(match!)));


    var loginCode = {'login': 'guest', 'passcode': 'guest'};
    var config = StompConfig(
        url: 'ws://192.168.1.188:61613',
        onConnect: (connection) => client.subscribe(
            destination: state.thisDancerName,
            callback: (frame) => print("BMD $frame ${frame.body}")),
        useSockJS: false,
        stompConnectHeaders: loginCode,
        webSocketConnectHeaders: loginCode,
        onStompError: (error) => print("stomp error $error"),
        onWebSocketError: (error) => print('webSocket error $error'),
        onDebugMessage: (message) => print('debug message $message'),
        onWebSocketDone: () => print('web socket done'),
        onDisconnect: (stopFrame) => print('disconnected $stopFrame'));
    client = StompClient(config: config);
    client.activate();
  }

}
