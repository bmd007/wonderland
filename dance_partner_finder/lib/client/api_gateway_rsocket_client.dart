import 'dart:convert';
import 'dart:typed_data';

import 'package:rsocket/metadata/composite_metadata.dart';
import 'package:rsocket/payload.dart';
import 'package:rsocket/rsocket.dart';
import 'package:rsocket/rsocket_connector.dart';
import 'package:rxdart/rxdart.dart';

class ApiGatewayRSocketClient {

  Future<RSocket> rsocketConnectionStream() => RSocketConnector.create()
      .connect('ws://192.168.1.188:7022')
      .whenComplete(() => "creation of rsocket connection completed")
      .catchError((error) => print(error));

  Payload routeAndDataPayload(String route, String data) {
    var compositeMetadata =
        CompositeMetadata.fromEntries([RoutingMetadata(route, List.empty())]);
    var metadataBytes = compositeMetadata.toUint8Array();
    var dataBytes = Uint8List.fromList(utf8.encode(data));
    return Payload.from(metadataBytes, dataBytes);
  }

  Stream<String?> fetchNames(String name, double latitude, double longitude) {
    var body = """
    {
      "location": {
          "latitude": "$latitude",
          "longitude": "$longitude"
        },
        "dancerPartnerSeekerName": "${name}"
    }
  """;
    return rsocketConnectionStream()
        .asStream()
        .asyncExpand((rSocket) => rSocket.requestStream!(
            routeAndDataPayload("/api/dance/partner/finder/names", body)))
        .map((element) => element!.getDataUtf8())
        .doOnError((p0, p1) => print(p1));
  }

  Future<void> addName(String name, double latitude, double longitude) {
    var body = """
    {
      "location": {
          "latitude": "$latitude",
          "longitude": "$longitude"
        },
        "name": "${name}"
    }
  """;
    return rsocketConnectionStream()
        .then((rSocket) => rSocket.fireAndForget!(
            routeAndDataPayload("/api/dance/partner/finder/addName", body)))
        .onError((error, stackTrace) => print(stackTrace));
  }

  Future<void> likeADancer(String whoHasLiked, String whomIsLiked) {
    var body = """
    {
      "whoHasLiked": "$whoHasLiked",
      "whomIsLiked": "$whomIsLiked"
    }
  """;
    return rsocketConnectionStream().then((rSocket) => rSocket.fireAndForget!(
        routeAndDataPayload("/api/dance/partner/finder/like", body)));
  }

  Future<void> disLikeADancer(String whoHasDisLiked, String whomIsDisLiked) {
    var body = """
    {
      "whoHasDisLiked": "$whoHasDisLiked",
      "whomIsDisLiked": "$whomIsDisLiked"
    }
  """;
    return rsocketConnectionStream().then((rSocket) => rSocket.fireAndForget!(
        routeAndDataPayload("/api/dance/partner/finder/disLike", body)));
  }

  Stream<String?> matchStreams() {
    return rsocketConnectionStream()
        .asStream()
        .asyncExpand((rSocket) => rSocket.requestStream!(
            routeAndDataPayload("/api/dance/partner/finder/matches", "")))
        .map((element) => element!.getDataUtf8());
  }
}
