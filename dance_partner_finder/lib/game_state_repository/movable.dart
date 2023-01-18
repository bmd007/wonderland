import 'dart:convert';

import 'package:equatable/equatable.dart';

class Movable extends Equatable {
  final String id;
  final double linearVelocityX;
  final double linearVelocityY;
  final double angularVelocity;

  const Movable(this.id, this.linearVelocityX, this.linearVelocityY,
      this.angularVelocity);

  static Movable fromJson(String jsonString) {
    var decodedMap = json.decode(jsonString);
    return Movable(decodedMap['id'], decodedMap['linearVelocityX'],
        decodedMap['linearVelocityY'], decodedMap['angularVelocity']);
  }

  @override
  List<Object?> get props =>
      [id, linearVelocityY, angularVelocity, linearVelocityX];
}
