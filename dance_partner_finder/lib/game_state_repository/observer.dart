import 'package:dance_partner_finder/game_state_repository/remote_game_state.dart';

class Observer {
  void notifyGameState(RemoteGameState state) => print(state);
}
