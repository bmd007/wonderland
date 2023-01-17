import 'dart:convert';

import 'package:equatable/equatable.dart';

class Movable extends Equatable {
  final String id;
  final double linearVelocityX;
  final double angularVelocity;
  final double linearVelocityY;

  Movable(this.id, this.linearVelocityX, this.angularVelocity, this.linearVelocityY);

  static Movable fromJson(String jsonString) {
    var decodedMap = json.decode(jsonString);
    return Movable(
        decodedMap['id'],
        decodedMap['angularVelocity'],
        decodedMap['linearVelocityX'],
        decodedMap['linearVelocityY']);
  }

  @override
  List<Object?> get props =>
      [id, linearVelocityY, angularVelocity, linearVelocityX];
}
