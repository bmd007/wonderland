import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:location/location.dart';
import 'package:rxdart/rxdart.dart';

import 'dance_partner_finder_event.dart';
import 'dance_partner_finder_state.dart';

Stream<LocationData> getCurrentLocation() {
  Location location = Location();

  return location
      .requestService()
      .asStream()
      .doOnData((event) => print("request service result: ${event}"))
      .asyncMap((value) => location.requestPermission())
      .doOnData((event) => print("request permission result: ${event}"))
      .asyncMap((value) => location.getLocation())
      .doOnData((event) => print("request location result: ${event}"))
      .doOnError(
        (p0, p1) => print("location error: ${p0}:${p1}"),
      );

  // if (!await location.serviceEnabled() && !await location.requestService()) {
  //   return Future.error(Error());
  // }
  // if (await location.hasPermission() == PermissionStatus.denied &&
  //     await location.requestPermission() != PermissionStatus.granted) {
  //   return Future.error(Error());
  // }
  // return await location.getLocation();
}

class DancePartnerFinderBloc extends Bloc<DancePartnerFinderEvent, DancePartnerFinderState> {
  DancePartnerFinderBloc(String thisDancerName) : super(DancePartnerFinderState.empty()) {
    on<SearchingRadiusEnteredEvent>((event, emit) {
      emit(state.setSearchingRadius(event.searchingRadius));

      getCurrentLocation()
          .doOnData((location) => ClientHolder.client
              .introduceAsDancePartnerSeeker(thisDancerName, location.latitude!, location.longitude!))
          .asyncExpand((location) => ClientHolder.client
              .fetchDancePartnerSeekersNames(thisDancerName, location.latitude!, location.longitude!, event.searchingRadius))
          .forEach((potentialDancePartner) =>
              add(PotentialDancerPartnerFoundEvent(potentialDancePartner!)));
    });
    on<DancersLoadedEvent>((event, emit) {
      emit(state.loaded(event.loadedDancerNames));
    });
    on<PotentialDancerPartnerFoundEvent>((event, emit) {
      emit(state.addPotentialDancer(event.potentialDancePartnerName));
    });
    on<DancerLikedEvent>((event, emit) {
      ClientHolder.client.likeADancer(thisDancerName, event.dancerName).forEach((element) {});
      emit(state.moveToNextDancer());
    });
    on<DancerDislikedEvent>((event, emit) {
      ClientHolder.client.disLikeADancer(thisDancerName, event.dancerName).forEach((element) {});
      emit(state.moveToNextDancer());
    });
  }
}
