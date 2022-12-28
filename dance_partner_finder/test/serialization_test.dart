import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Serialize nested json', () {
    //given
    Map<String, dynamic> map = {
      "type": "game.input.joystick",
      "sender": "bmd579@gmail.com",
      "receiver": "bmd579@gmail.com",
      "content": {
        "direction": "up",
        "relativeDeltaX": 3.3,
        "relativeDeltaY": 2.3,
      }
    };
    //when
    var jsonEncodedString = jsonEncode(map);
    print(jsonEncodedString);
    Map<String, dynamic> jsonDecodedMap = jsonDecode(jsonEncodedString);
    //then
    expect(map, jsonDecodedMap);
  });
}