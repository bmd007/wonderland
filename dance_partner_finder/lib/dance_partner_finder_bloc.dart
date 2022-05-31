import 'dart:io';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part 'dance_partner_finder_event.dart';
part 'dance_partner_finder_state.dart';

class DancePartnerBloc extends Bloc<DancePartnerEvent, DancePartnerState> {
  DancePartnerBloc() : super(DancePartnerState.empty()) {
    on<ThisDancerChoseNameEvent>((event, emit) {
      emit(state.withThisDancerName(event.thisDancerName));
      sleep(const Duration(milliseconds: 5));
      emit(state.loading());
      sleep(const Duration(milliseconds: 15));
      add(const DancersLoadedEvent(['tom', 'like', 'match', 'dancer']));
    });
    on<DancersLoadedEvent>((event, emit) {
      emit(state.loaded(event.loadedDancerNames));
    });
    on<DancerLikedEvent>((event, emit) {
      emit(state.moveToNextDancer());
    });
    on<DancerDislikedEvent>((event, emit) {
    });
  }
}
