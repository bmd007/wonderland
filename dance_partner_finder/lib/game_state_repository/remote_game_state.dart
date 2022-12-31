import 'dart:convert';

RemoteGameState remoteGameStateFromJson(String str) =>
    RemoteGameState.fromJson(json.decode(str));

String remoteGameStateToJson(RemoteGameState data) =>
    json.encode(data.toJson());

class RemoteGameState {
  RemoteGameState({
    this.tickNumber,
    this.timeStamp,
    this.movables,
  });

  RemoteGameState.fromJson(dynamic json) {
    tickNumber = json['tickNumber'];
    timeStamp = json['timeStamp'];
    if (json['movables'] != null) {
      movables = [];
      json['movables'].forEach((v) {
        movables?.add(Movable.fromJson(v));
      });
    }
  }

  String? tickNumber;
  String? timeStamp;
  List<Movable>? movables;

  RemoteGameState copyWith({
    String? tickNumber,
    String? timeStamp,
    List<Movable>? movables,
  }) =>
      RemoteGameState(
        tickNumber: tickNumber ?? this.tickNumber,
        timeStamp: timeStamp ?? this.timeStamp,
        movables: movables ?? this.movables,
      );

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{};
    map['tickNumber'] = tickNumber;
    map['timeStamp'] = timeStamp;
    if (movables != null) {
      map['movables'] = movables?.map((v) => v.toJson()).toList();
    }
    return map;
  }
}

Movable movablesFromJson(String str) => Movable.fromJson(json.decode(str));

String movablesToJson(Movable data) => json.encode(data.toJson());

class Movable {
  Movable({
    this.id,
    this.positionX,
    this.positionY,
    this.velocityX,
    this.velocityY,
    this.angel,
  });

  Movable.fromJson(dynamic json) {
    id = json['id'];
    positionX = json['positionX'];
    positionY = json['positionY'];
    velocityX = json['velocityX'];
    velocityY = json['velocityY'];
    angel = json['angel'];
  }

  String? id;
  double? positionX;
  double? positionY;
  double? velocityX;
  double? velocityY;
  double? angel;

  Movable copyWith({
    String? id,
    double? positionX,
    double? positionY,
    double? velocityX,
    double? velocityY,
    double? angel,
  }) =>
      Movable(
        id: id ?? this.id,
        positionX: positionX ?? this.positionX,
        positionY: positionY ?? this.positionY,
        velocityX: velocityX ?? this.velocityX,
        velocityY: velocityY ?? this.velocityY,
        angel: angel ?? this.angel,
      );

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{};
    map['id'] = id;
    map['positionX'] = positionX;
    map['positionY'] = positionY;
    map['velocityX'] = velocityX;
    map['velocityY'] = velocityY;
    map['angel'] = angel;
    return map;
  }
}
