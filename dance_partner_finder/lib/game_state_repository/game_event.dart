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
