import 'dart:convert';

import 'package:equatable/equatable.dart';

class Movable extends Equatable {
  final String id;
  final double positionX;
  final double positionY;
  final double velocityX;
  final double velocityY;
  final double angel;

  const Movable(this.id, this.positionX, this.positionY, this.velocityX,
      this.velocityY, this.angel);

  static Movable fromJson(String jsonString) {
    var decodedMap = json.decode(jsonString);
    return Movable(
        decodedMap['id'],
        decodedMap['positionX'],
        decodedMap['positionY'],
        decodedMap['velocityX'],
        decodedMap['velocityY'],
        decodedMap['angel']);
  }

  @override
  List<Object?> get props =>
      [id, positionX, positionY, velocityX, velocityY, angel];
}
