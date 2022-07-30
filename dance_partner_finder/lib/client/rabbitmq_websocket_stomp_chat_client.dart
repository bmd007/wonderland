import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

class RabbitMqWebSocketStompChatClient {
  late final StompClient stompClient;

  RabbitMqWebSocketStompChatClient(String thisDancerName,
      void Function(StompFrame stompFrame) stompMessageHandler) {
    var loginCode = {'login': 'mqtt-test', 'passcode': 'mqtt-test'};

    var config = StompConfig(
        url: 'ws://34.88.16.93:15674/ws',
        // beforeConnect: () async => print("before connect"),
        onUnhandledFrame: (dynamic onUnhandledFrame) =>
            print('onUnhandledFrame $onUnhandledFrame'),
        onUnhandledMessage: (dynamic onUnhandledMessage) =>
            print('onUnhandledMessage $onUnhandledMessage'),
        onUnhandledReceipt: (dynamic onUnhandledReceipt) =>
            print('onUnhandledReceipt $onUnhandledReceipt'),
        onConnect: (connection) => stompClient.subscribe(
            destination: '/queue/$thisDancerName',
            callback: stompMessageHandler),
        stompConnectHeaders: loginCode,
        webSocketConnectHeaders: loginCode,
        onStompError: (dynamic error) =>
            print("stomp error ${error.toString()}"),
        onWebSocketError: (dynamic error) => print(error.toString()),
        // onDebugMessage: (dynamic message) => print('debug message $message'),
        onWebSocketDone: () => print('web socket done'),
        onDisconnect: (dynamic stopFrame) => print('disconnected $stopFrame'));
    stompClient = StompClient(config: config);
    stompClient.activate();
  }

  void _onConnect(StompFrame connection) {
    print("webSocket connected $connection");
    // stompClient.send(
    //   destination: '/queue/taylor',
    //   body: '/amq/taylor tp taylor',
    // );
  }
}
