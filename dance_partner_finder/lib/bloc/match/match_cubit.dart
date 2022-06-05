import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_rsocket_client.dart';
import 'package:equatable/equatable.dart';

part 'match_state.dart';

class HasMatchCubit extends Cubit<MatchState> {
  HasMatchCubit() : super(MatchState.initial());

  setName(String thisDancerName) {
    emit(state.setThisDancerName(thisDancerName));
    matchStreams().forEach((element) => {
          print("Matched with ${element}"),
          emit(state.setMatchFound()),
        });
  }
}
