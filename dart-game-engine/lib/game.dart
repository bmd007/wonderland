import 'dart:async';
import 'dart:html' hide Body;

import 'package:forge2d/forge2d_browser.dart' hide Timer;

abstract class Game {
  List<Body> bodies = <Body>[];
  static const double _gravity = -10.0;
  static const double timeStep = 1 / 60;
  final World world;
  final Stopwatch _stopwatch;
  late int frameCount;
  late int? elapsedUs;

  Game(String name, [Vector2? gravity])
      : world = World(gravity ?? Vector2(0.0, _gravity)),
        _stopwatch = Stopwatch()..start() {world.setAllowSleep(true);}

  void step(num timestamp) {
    _stopwatch.reset();
    world.stepDt(timeStep);
    elapsedUs = _stopwatch.elapsedMicroseconds;
    world.drawDebugData();
    frameCount++;
    print("fps $frameCount");
  }

  void initializeCounters() {
    frameCount = 0;
    Timer.periodic(const Duration(seconds: 1), (Timer t) {
      frameCount = 0;
    });
    Timer.periodic(const Duration(milliseconds: 200), (Timer t) {
      if (elapsedUs == null) {
        return;
      }
    });
  }

  void initialize();
}
