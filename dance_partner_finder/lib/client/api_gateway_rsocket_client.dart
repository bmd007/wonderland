import 'dart:convert';
import 'dart:typed_data';

import 'package:rsocket/metadata/composite_metadata.dart';
import 'package:rsocket/payload.dart';
import 'package:rsocket/rsocket.dart';
import 'package:rsocket/rsocket_connector.dart';
import 'package:rxdart/rxdart.dart';

class ApiGatewayRSocketClient {
  Future<RSocket> _rsocketConnectionStream = RSocketConnector.create()
      .keepAlive(2000, 999999999)
      .connect('ws://192.168.1.188:7022')
      .catchError((error) => print(error));
  
  Payload routeAndDataPayload(String route, String data) {
    var compositeMetadata =
        CompositeMetadata.fromEntries([RoutingMetadata(route, List.empty())]);
    var metadataBytes = compositeMetadata.toUint8Array();
    var dataBytes = Uint8List.fromList(utf8.encode(data));
    return Payload.from(metadataBytes, dataBytes);
  }

  Stream<String?> fetchDancePartnerSeekersNames(String name, double latitude, double longitude) {
    var body = """
    {
      "location": {
          "latitude": "$latitude",
          "longitude": "$longitude"
        },
        "dancerPartnerSeekerName": "${name}"
    }
  """;
    return _rsocketConnectionStream
        .asStream()
        .asyncExpand((rSocket) => rSocket.requestStream!(
            routeAndDataPayload("/api/dance/partner/finder/names", body)))
        .map((element) => element!.getDataUtf8())
        .doOnError((p0, p1) => print(p1));
  }

  Stream<void> introduceAsDancePartnerSeeker(String name, double latitude, double longitude) {
    var body = """
    {
      "location": {
          "latitude": "$latitude",
          "longitude": "$longitude"
        },
        "name": "${name}"
    }
  """;
    return _rsocketConnectionStream
        .asStream()
        .asyncMap((rSocket) => rSocket.fireAndForget!(
            routeAndDataPayload("/api/dance/partner/finder/addName", body)))
        .doOnError((error, stackTrace) => print(stackTrace));
  }

  Stream<void> likeADancer(String whoHasLiked, String whomIsLiked) {
    var body = """
    {
      "whoHasLiked": "$whoHasLiked",
      "whomIsLiked": "$whomIsLiked"
    }
  """;
    return _rsocketConnectionStream
        .asStream()
        .asyncMap((rSocket) => rSocket.fireAndForget!(
            routeAndDataPayload("/api/dance/partner/finder/like", body)))
        .doOnError((error, stackTrace) => print(stackTrace));
  }

  Stream<void> disLikeADancer(String whoHasDisLiked, String whomIsDisLiked) {
    var body = """
    {
      "whoHasDisLiked": "$whoHasDisLiked",
      "whomIsDisLiked": "$whomIsDisLiked"
    }
  """;
    return _rsocketConnectionStream
        .asStream()
        .asyncMap((rSocket) => rSocket.fireAndForget!(
            routeAndDataPayload("/api/dance/partner/finder/disLike", body)))
        .doOnError((error, stackTrace) => print(stackTrace));
  }

  Stream<String?> matchStreams(String thisDancerName) {
    return _rsocketConnectionStream
        .asStream()
        .asyncExpand((rSocket) => rSocket.requestStream!(
            routeAndDataPayload("/api/dance/partner/finder/matches", thisDancerName)))
        .map((element) => element!.getDataUtf8())
        .doOnError((error, stackTrace) => print(stackTrace));
  }
}
