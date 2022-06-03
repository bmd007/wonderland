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

Stream<String?> fetchNames() {
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .asStream()
      .asyncExpand((rSocket) => rSocket.requestStream!(routeAndDataPayload("names", "")))
      .map((element) => element!.getDataUtf8());
}

Future<void> addName(String name) {
  return RSocketConnector.create()
      .connect('tcp://192.168.1.188:7022')
      .then((rSocket) => rSocket.fireAndForget!(routeAndDataPayload("addName", name)));
}
