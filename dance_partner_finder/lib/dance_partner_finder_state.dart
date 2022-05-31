part of 'dance_partner_finder_bloc.dart';

class DancePartnerState extends Equatable {
  final List<String> dancerNames;
  final int currentDancerIndex;
  final bool isLoading;

  @override
  const DancePartnerState(this.isLoading, this.currentDancerIndex, this.dancerNames);

  static DancePartnerState loading(){
    return const DancePartnerState(true, 0, []);
  }

  static DancePartnerState loaded(List<String> loadedDancerNames){
    return DancePartnerState(false, 0, loadedDancerNames);
  }

  DancePartnerState moveToNextDancer(){
    if(currentDancerIndex + 1 >= dancerNames.length){
      return this;
    }
    return DancePartnerState(false, currentDancerIndex + 1, dancerNames);
  }

  String getCurrentDancerName(){
    if(dancerNames.isEmpty){
      return "tom";
    }
    return dancerNames.elementAt(currentDancerIndex);
  }


  @override
  List<Object> get props => [currentDancerIndex, isLoading, dancerNames];
}
