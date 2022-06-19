import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';

import 'dance_partner_match_event.dart';
import 'dance_partner_match_state.dart';

class DancePartnerMatchBloc extends Bloc<DancePartnerMatchEvent, DancePartnerMatchState> {
  DancePartnerMatchBloc() : super(DancePartnerMatchState.empty()) {
    on<ThisDancerChoseNameEvent>((event, emit) {
      emit(state.withThisDancerName(event.thisDancerName));
      ApiGatewayClientHolder.client.matchStreams().forEach((match) => add(MatchFoundEvent(match!)));
    });
    on<DancerMatchesLoadedEvent>((event, emit) {
      emit(state.loaded(event.loadedDancerNames));
    });
    on<MatchFoundEvent>((event, emit) {
      emit(state.addMatch(event.dancePartnerMatchName));
    });
  }
}
