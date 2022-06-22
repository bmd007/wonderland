import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:http/http.dart' as http;
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';

import 'dance_partner_match_event.dart';
import 'dance_partner_match_state.dart';

class DancePartnerMatchBloc extends Bloc<DancePartnerMatchEvent, DancePartnerMatchState> {
  late StompClient stompClient;

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

    var loginCode = {'login': 'mqtt-test', 'passcode': 'mqtt-test'};
    var config = StompConfig(
        url: 'ws://192.168.1.188:15674/ws',
        beforeConnect: () async => print("before connect"),
        onUnhandledFrame: (dynamic onUnhandledFrame) =>
            print('onUnhandledFrame $onUnhandledFrame'),
        onUnhandledMessage: (dynamic onUnhandledMessage) =>
            print('onUnhandledMessage $onUnhandledMessage'),
        onUnhandledReceipt: (dynamic onUnhandledReceipt) =>
            print('onUnhandledReceipt $onUnhandledReceipt'),
        onConnect: (connection) => stompClient.subscribe(
            destination: '/queue/taylor', callback: stompMessageHandler),
        stompConnectHeaders: loginCode,
        webSocketConnectHeaders: loginCode,
        onStompError: (dynamic error) =>
            print("stomp error ${error.toString()}"),
        onWebSocketError: (dynamic error) => print(error.toString()),
        onDebugMessage: (dynamic message) => print('debug message $message'),
        onWebSocketDone: () => print('web socket done'),
        onDisconnect: (dynamic stopFrame) => print('disconnected $stopFrame'));
    stompClient = StompClient(config: config);
    stompClient.activate();
  }

  void stompMessageHandler(frame) {
    print("/queue/taylor BMD $frame ${frame.body}");
  }

  void _onConnect(connection) {
    print("websocket connected $connection");
    // stompClient.send(
    //   destination: '/queue/taylor',
    //   body: '/amq/taylor tp taylor',
    // );
    ;
  }
}
