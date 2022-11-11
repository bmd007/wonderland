import 'package:equatable/equatable.dart';

class DancePartnerFinderState extends Equatable {

  final List<String> dancerNames;
  final int currentDancerIndex;
  final double searchingRadius;
  final bool isLoading;

  const DancePartnerFinderState(this.isLoading, this.currentDancerIndex, this.dancerNames, this.searchingRadius);

  DancePartnerFinderState setSearchingRadius(double searchingRadius) {
    return DancePartnerFinderState(true, 0, const [], searchingRadius);
  }

  DancePartnerFinderState loading() {
    return DancePartnerFinderState(true, 0, const [], searchingRadius);
  }

  DancePartnerFinderState loaded(List<String> loadedDancerNames) {
    return DancePartnerFinderState(false, 0, loadedDancerNames, searchingRadius);
  }

  static DancePartnerFinderState empty() {
    return const DancePartnerFinderState(false, 0, [], 200);
  }

  DancePartnerFinderState moveToNextDancer() {
    if (currentDancerIndex + 1 >= dancerNames.length) {
      return loading();
    }
    return DancePartnerFinderState(false, currentDancerIndex + 1, dancerNames, searchingRadius);
  }

  bool isRunningOutOfDancers(){
    return currentDancerIndex >= dancerNames.length || dancerNames.isEmpty;
  }

  String getCurrentDancerName() {
    return dancerNames.elementAt(currentDancerIndex);
  }

  @override
  List<Object> get props => [currentDancerIndex, isLoading, dancerNames, searchingRadius];

  DancePartnerFinderState addPotentialDancer(String potentialDancePartnerName) {
    List<String> newDancerNames = dancerNames.toList(growable: true);
    newDancerNames.add(potentialDancePartnerName);
    return DancePartnerFinderState(false, currentDancerIndex, newDancerNames, searchingRadius);
  }
}
