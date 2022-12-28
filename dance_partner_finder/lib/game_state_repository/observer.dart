import 'game_event.dart';

class Observer {
  void notifyGameEvent(GameEvent event) => print(event);
}
