import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part 'dance_partner_finder_event.dart';
part 'dance_partner_finder_state.dart';

class DancePartnerBloc extends Bloc<DancePartnerEvent, DancePartnerState> {
  DancePartnerBloc() : super(const DancePartnerLoading()) {
    add(const DancersLoadedEvent(['tom']));
    on<DancersLoadedEvent>((event, emit) {
      emit(Da)
    });
  }
}
