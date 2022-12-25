import 'package:flame/components.dart';
import 'package:flame/game.dart';
import 'package:flame/input.dart';
import 'package:flame/palette.dart';
import 'package:flame_forge2d/flame_forge2d.dart';
import 'package:flutter/material.dart';

import 'boundary_creator.dart';
import 'enemy.dart';
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
    addAll(createBoundaries(screenSize));

    final knobPaint = BasicPalette.red.withAlpha(200).paint();
    final backgroundPaint = BasicPalette.blue.withAlpha(100).paint();
    joystickComponent = JoystickComponent(
      knob: CircleComponent(radius: 20, paint: knobPaint),
      background: CircleComponent(radius: 60, paint: backgroundPaint),
      margin: const EdgeInsets.only(left: 40, bottom: 40),
    );
    await add(joystickComponent);

    myGirl = MyGirl(joystickComponent, screenSize / 2);
    add(myGirl);

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
        onPressed: () async {
          await myGirl.throwKanui();
        });

    add(shapeButton);

    add(Enemy(screenSize / 1.5));
    add(Enemy(screenSize / 2.5));
  }
}
