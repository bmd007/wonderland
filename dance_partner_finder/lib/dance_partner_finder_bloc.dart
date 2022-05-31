import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part 'dance_partner_finder_event.dart';
part 'dance_partner_finder_state.dart';

class DancePartnerBloc extends Bloc<DancePartnerEvent, DancePartnerState> {
  DancePartnerBloc() : super(DancePartnerState.loading()) {
    on<DancersLoadedEvent>((event, emit) {
      emit(DancePartnerState.loaded(event.loadedDancerNames));
    });
    on<DancerLikedEvent>((event, emit) {

    });
    on<DancerDislikedEvent>((event, emit) {
      //todo
    });
    add(const DancersLoadedEvent(['tom', 'like', 'match', 'dancer']));
  }
}
