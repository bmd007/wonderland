part of 'dance_partner_finder_bloc.dart';

class DancePartnerState extends Equatable {
  final List<String> dancerNames;
  final int currentDancerIndex;
  final bool isLoading;
  final String thisDancerName;//todo rename

  @override
  const DancePartnerState(this.isLoading, this.currentDancerIndex, this.dancerNames, this.thisDancerName);

  DancePartnerState withThisDancerName(thisDancerName){
    return DancePartnerState(true, 0, [], thisDancerName);
  }

  DancePartnerState loading(){
    return DancePartnerState(true, 0, [], thisDancerName);
  }

  DancePartnerState loaded(List<String> loadedDancerNames){
    return DancePartnerState(false, 0, loadedDancerNames, thisDancerName);
  }


  static DancePartnerState empty(){
    return const DancePartnerState(false, 0, [], "");
  }

  DancePartnerState moveToNextDancer(){
    if(currentDancerIndex + 1 >= dancerNames.length){
      return this;
    }
    return DancePartnerState(false, currentDancerIndex + 1, dancerNames, thisDancerName);
  }

  String getCurrentDancerName(){
    if(dancerNames.isEmpty){
      return "tom";
    }
    return dancerNames.elementAt(currentDancerIndex);
  }

  @override
  List<Object> get props => [currentDancerIndex, isLoading, dancerNames];

  DancePartnerState addPotentialDancer(String potentialDancePartnerName) {
    List<String> newDancerNames = dancerNames.toList(growable: true);
    newDancerNames.add(potentialDancePartnerName);
    return DancePartnerState(false, currentDancerIndex, newDancerNames, thisDancerName);
  }
}
