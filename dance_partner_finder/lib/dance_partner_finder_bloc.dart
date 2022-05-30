import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part 'dance_partner_finder_event.dart';
part 'dance_partner_finder_state.dart';

class DancePartnerFinderBloc extends Bloc<DancePartnerFinderEvent, DancePartnerFinderState> {
  DancePartnerFinderBloc() : super(DancePartnerFinderInitial()) {
    on<DancePartnerFinderEvent>((event, emit) {
      // TODO: implement event handler
    });
  }
}
