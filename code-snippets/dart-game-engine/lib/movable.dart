import 'dart:convert';

import 'package:equatable/equatable.dart';

class Movable extends Equatable {
  final String id;
  final double linearVelocityX;
  final double initialPositionX;
  final double initialPositionY;
  final double initialAngel;
  final double linearVelocityY;
  final double angularVelocity;

  const Movable(
      this.id,
      this.initialPositionX,
      this.initialPositionY,
      this.initialAngel,
      this.linearVelocityX,
      this.linearVelocityY,
      this.angularVelocity);

  static Movable fromJson(String jsonString) {
    var decodedMap = json.decode(jsonString);
    return Movable(
        decodedMap['id'],
        decodedMap['initialPositionX'],
        decodedMap['initialPositionY'],
        decodedMap['initialAngel'],
        decodedMap['linearVelocityX'],
        decodedMap['linearVelocityY'],
        decodedMap['angularVelocity']);
  }

  Map<String, dynamic> toJson() => {
        "id": id,
        "initialPositionX": initialPositionX,
        "initialPositionY": initialPositionY,
        "initialAngel": initialAngel,
        "linearVelocityX": linearVelocityX,
        "linearVelocityY": linearVelocityY,
        "angularVelocity": angularVelocity
      };

  @override
  List<Object?> get props => [
        id,
        initialPositionX,
        initialPositionY,
        initialAngel,
        linearVelocityX,
        linearVelocityY,
        angularVelocity
      ];
}
