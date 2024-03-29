import 'package:equatable/equatable.dart';
import 'package:flame/components.dart';

class JoystickMovedEvent extends Equatable{
  final Vector2 relativeDelta;
  final JoystickDirection direction;

  const JoystickMovedEvent(this.direction, this.relativeDelta);

  Map<String, dynamic> toJson() => {
        "relativeDeltaX": relativeDelta.x,
        "relativeDeltaY": relativeDelta.y,
        "direction": direction.name
      };

  @override
  List<Object?> get props => [direction, relativeDelta];
}

