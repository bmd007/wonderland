import 'dart:io';
import 'package:location/location.dart';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:rxdart/rxdart.dart';

part 'dance_partner_finder_event.dart';
part 'dance_partner_finder_state.dart';




class DancePartnerBloc extends Bloc<DancePartnerEvent, DancePartnerState> {

  Future<LocationData> getLocation() async {
    Location location = Location();

    if (!await location.serviceEnabled()) {
      if (!await location.requestService()) {
        return Future.error(Error());;
      }
    }
    if (await location.hasPermission() == PermissionStatus.denied) {
      if (await location.requestPermission() != PermissionStatus.granted) {
        return Future.error(Error());
      }
    }

    return await location.getLocation();
  }

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
    getLocation().then((value) => print("BMD::$value"));
  }
}
