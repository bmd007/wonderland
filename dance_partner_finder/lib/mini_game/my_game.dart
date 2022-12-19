import 'package:flame/components.dart';
import 'package:flame/game.dart';
import 'package:flame/input.dart';
import 'package:flame/palette.dart';
import 'package:flame_forge2d/flame_forge2d.dart';
import 'package:flutter/material.dart';

import 'boundry_creator.dart';
import 'my_girl.dart';

class MyForge2DFlameGame extends Forge2DGame with HasDraggables, HasTappables {
  late final JoystickComponent joystickComponent;
  late final MyGirl myGirl;
  late final HudButtonComponent shapeButton;

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    debugMode = false;
    var screenSize = screenToWorld(camera.viewport.effectiveSize);
    addAll(createBoundaries(this));
    // add(MyPlatform(screenSize / 2));
    // add(MyPlatform(Vector2(screenSize.x / 2, screenSize.y)));
    // add(MyPlatform(Vector2(screenSize.x / 2, screenSize.y * 2)));
    // add(MyGround(screenSize));

    final knobPaint = BasicPalette.red.withAlpha(200).paint();
    final backgroundPaint = BasicPalette.blue.withAlpha(100).paint();
    joystickComponent = JoystickComponent(
      knob: CircleComponent(radius: 30, paint: knobPaint),
      background: CircleComponent(radius: 60, paint: backgroundPaint),
      margin: const EdgeInsets.only(left: 40, bottom: 40),
    );

    myGirl = MyGirl(screenSize, joystickComponent);

    final shapeButton = HudButtonComponent(
      button: CircleComponent(radius: 20),
      buttonDown: RectangleComponent(
        size: Vector2(10, 10),
        paint: BasicPalette.blue.paint(),
      ),
      margin: const EdgeInsets.only(
        right: 85,
        bottom: 150,
      ),
    );

    add(myGirl);
    add(shapeButton);
    add(joystickComponent);
  }

// @override
// void update(double dt){
//   super.update(dt);
// }
}
