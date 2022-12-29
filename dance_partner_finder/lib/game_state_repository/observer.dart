import 'game_event.dart';

class Observer {
  void notifyGameEvent(GameEvent event) => print(event);
  void notifyGameState(String direction) => print(direction);
}
