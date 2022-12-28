import 'dart:convert';

import 'package:equatable/equatable.dart';
import 'package:flame/components.dart';

abstract class GameEvent extends Equatable {
  const GameEvent();
}

class JoystickMovedEvent extends GameEvent {
  final Vector2 relativeDelta;
  final JoystickDirection direction;

  const JoystickMovedEvent(this.direction, this.relativeDelta);

  @override
  List<Object?> get props => [direction, relativeDelta];
}

class ShootButtonPushedEvent extends GameEvent {
  const ShootButtonPushedEvent();

  @override
  List<Object?> get props => [];
}

class JoystickMovedMessageReceivedEvent extends GameEvent {
  late final Vector2 relativeDelta;
  late final JoystickDirection direction;

  JoystickMovedMessageReceivedEvent(
      String directionString, double relativeDeltaX, relativeDeltaY) {
    direction = JoystickDirection.values
        .where((element) => element.name == directionString)
        .first;
    relativeDelta = Vector2(relativeDeltaX, relativeDeltaY);
  }

  static JoystickMovedMessageReceivedEvent fromJson(String jsonString) {
    Map<String, dynamic> keyValueMap = jsonDecode(jsonString);
    return JoystickMovedMessageReceivedEvent(keyValueMap["direction"],
        keyValueMap["relativeDeltaX"], keyValueMap["relativeDeltaY"]);
  }

  @override
  List<Object?> get props => [relativeDelta, direction];
}

class ShootButtonPushedMessageReceivedEvent extends GameEvent {

  const ShootButtonPushedMessageReceivedEvent();

  @override
  List<Object?> get props => [];
}

class StompConnectionReadyEvent extends GameEvent {
  @override
  List<Object?> get props => ['ignore'];
}
