import 'dart:convert';
import 'dart:typed_data';

import 'package:rsocket/metadata/composite_metadata.dart';
import 'package:rsocket/payload.dart';
import 'package:rsocket/rsocket_connector.dart';

Payload routeAndDataPayload(String route, String data) {
  var compositeMetadata =
  CompositeMetadata.fromEntries([RoutingMetadata(route, List.empty())]);
  var metadataBytes = compositeMetadata.toUint8Array();
  var dataBytes = Uint8List.fromList(utf8.encode(data));
  return Payload.from(metadataBytes, dataBytes);
}

Stream<String?> fetchNames(String thisDancerName) {
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .asStream()
      .asyncExpand((rSocket) => rSocket.requestStream!(routeAndDataPayload("/api/dance/partner/finder/names", thisDancerName)))
      .map((element) => element!.getDataUtf8());
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
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .then((rSocket) => rSocket.fireAndForget!(routeAndDataPayload("/api/dance/partner/finder/addName", body)));
}

Future<void> likeADancer(String whoHasLiked, String whomIsLiked) {
  var body = """
    {
      "whoHasLiked": "$whoHasLiked",
      "whomIsLiked": "$whomIsLiked"
    }
  """;
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .then((rSocket) => rSocket.fireAndForget!(routeAndDataPayload("/api/dance/partner/finder/like", body)));
}

Future<void> disLikeADancer(String whoHasDisLiked, String whomIsDisLiked) {
  var body = """
    {
      "whoHasDisLiked": "$whoHasDisLiked",
      "whomIsDisLiked": "$whomIsDisLiked"
    }
  """;
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .then((rSocket) => rSocket.fireAndForget!(routeAndDataPayload("/api/dance/partner/finder/disLike", body)));
}

Stream<String?> matchStreams() {
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .asStream()
      .asyncExpand((rSocket) => rSocket.requestStream!(routeAndDataPayload("/api/dance/partner/finder/matches", "")))
      .map((element) => element!.getDataUtf8());
}

