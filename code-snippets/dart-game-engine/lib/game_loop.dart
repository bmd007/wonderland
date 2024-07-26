import 'dart:async';

/// {@template game_loop}
/// A [GameLoop] is a loop that runs the game logic.
/// {@endtemplate}
class GameLoop {
  /// {@macro game_loop}
  GameLoop({
    required this.onTick,
    int tickRate = 20,
  }) : tickLengthInMs = 1000 ~/ tickRate;

  /// The function that will be called every tick.
  final void Function(double) onTick;

  /// The length of a tick in milliseconds.
  final int tickLengthInMs;

  int _previousTick = DateTime.now().millisecondsSinceEpoch;

  void _loop() {
    final now = DateTime.now().millisecondsSinceEpoch;

    if (_previousTick + tickLengthInMs <= now) {
      final delta = (now - _previousTick) / 1000;
      _previousTick = now;
      onTick(delta);
    }

    if (DateTime.now().millisecondsSinceEpoch - _previousTick < tickLengthInMs - 16) {
      Future.microtask(_loop);
    } else {
      Timer.run(_loop);
    }
  }

  /// Starts the game loop.
  void play() => _loop();
}
