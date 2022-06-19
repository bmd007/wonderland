part of 'dance_partner_finder_bloc.dart';

class DancePartnerFinderState extends Equatable {
  final List<String> dancerNames;
  final int currentDancerIndex;
  final bool isLoading;
  final String thisDancerName; //todo rename

  const DancePartnerFinderState(this.isLoading, this.currentDancerIndex,
      this.dancerNames, this.thisDancerName);

  DancePartnerFinderState withThisDancerName(thisDancerName) {
    return DancePartnerFinderState(true, 0, const [], thisDancerName);
  }

  DancePartnerFinderState loading() {
    return DancePartnerFinderState(true, 0, const [], thisDancerName);
  }

  DancePartnerFinderState loaded(List<String> loadedDancerNames) {
    return DancePartnerFinderState(false, 0, loadedDancerNames, thisDancerName);
  }

  static DancePartnerFinderState empty() {
    return const DancePartnerFinderState(false, 0, [], "");
  }

  DancePartnerFinderState moveToNextDancer() {
    if (currentDancerIndex + 1 >= dancerNames.length) {
      return loading();
    }
    return DancePartnerFinderState(
        false, currentDancerIndex + 1, dancerNames, thisDancerName);
  }

  String getCurrentDancerName() {
    if (dancerNames.isEmpty || isLoading) {
      return "wait";
    }
    return dancerNames.elementAt(currentDancerIndex);
  }

  @override
  List<Object> get props => [currentDancerIndex, isLoading, dancerNames];

  DancePartnerFinderState addPotentialDancer(String potentialDancePartnerName) {
    List<String> newDancerNames = dancerNames.toList(growable: true);
    newDancerNames.add(potentialDancePartnerName);
    return DancePartnerFinderState(false, currentDancerIndex, newDancerNames, thisDancerName);
  }
}
