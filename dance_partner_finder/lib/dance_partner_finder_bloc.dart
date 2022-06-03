import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:location/location.dart';

part 'dance_partner_finder_event.dart';
part 'dance_partner_finder_state.dart';


Future<LocationData> getLocation() async {
  Location location = Location();

  if (!await location.serviceEnabled() && !await location.requestService()) {
    return Future.error(Error());
  }
  if (await location.hasPermission() == PermissionStatus.denied &&
      await location.requestPermission() != PermissionStatus.granted) {
    return Future.error(Error());
  }
  return await location.getLocation();
}

class DancePartnerBloc extends Bloc<DancePartnerEvent, DancePartnerState> {
  DancePartnerBloc() : super(DancePartnerState.empty()) {
    on<ThisDancerChoseNameEvent>((event, emit) {
      emit(state.withThisDancerName(event.thisDancerName));

      add(PotentialDancerPartnerFoundEvent('tom'));
      add(PotentialDancerPartnerFoundEvent('like'));
      add(PotentialDancerPartnerFoundEvent('match'));
    });
    on<DancersLoadedEvent>((event, emit) {
      emit(state.loaded(event.loadedDancerNames));
    });
    on<PotentialDancerPartnerFoundEvent>((event, emit) {
      emit(state.addPotentialDancer(event.potentialDancePartnerName));
    });
    on<DancerLikedEvent>((event, emit) {
      emit(state.moveToNextDancer());
    });
    on<DancerDislikedEvent>((event, emit) {});

    // getLocation()
    // .then((value) => print("BMD:location:$value"))
    // .then((value) => addName(state.thisDancerName))
    // .asStream()
    // .asyncExpand((event) => fetchNames())
    // .forEach((potentialDancePartner) => add(PotentialDancerPartnerFoundEvent(potentialDancePartner!)));
  }
}
