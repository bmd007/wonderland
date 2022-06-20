import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:http/http.dart' as http;

import 'dance_partner_match_event.dart';
import 'dance_partner_match_state.dart';

class DancePartnerMatchBloc
    extends Bloc<DancePartnerMatchEvent, DancePartnerMatchState> {
  DancePartnerMatchBloc(String thisDancerName)
      : super(DancePartnerMatchState.withThisDancerName(thisDancerName)) {
    on<DancerMatchesLoadedEvent>((event, emit) {
      emit(state.loaded(event.loadedDancerNames));
    });
    on<MatchFoundEvent>((event, emit) {
      emit(state.addMatch(event.dancePartnerMatchName));
    });
    emit(state.loading());

    http.post(Uri.parse(
        'http://192.168.1.188:9531/create/queue/for/${thisDancerName}'));

    ApiGatewayClientHolder.client
        .matchStreams(state.thisDancerName)
        .forEach((match) => add(MatchFoundEvent(match!)));
  }
}
