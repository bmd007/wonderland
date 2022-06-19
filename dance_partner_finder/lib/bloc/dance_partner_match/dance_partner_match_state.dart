import 'package:equatable/equatable.dart';

class DancePartnerMatchState extends Equatable {
  final List<String> matchedDancerNames;
  final bool isLoading;
  final String thisDancerName; //todo rename

  const DancePartnerMatchState(
      this.isLoading, this.matchedDancerNames, this.thisDancerName);

  DancePartnerMatchState withThisDancerName(thisDancerName) {
    return DancePartnerMatchState(true, const [], thisDancerName);
  }

  DancePartnerMatchState loading() {
    return DancePartnerMatchState(true, const [], thisDancerName);
  }

  DancePartnerMatchState loaded(List<String> loadedMatchedNames) {
    return DancePartnerMatchState(false, loadedMatchedNames, thisDancerName);
  }

  static DancePartnerMatchState empty() {
    return const DancePartnerMatchState(false, [], "");
  }

  @override
  List<Object> get props => [isLoading, matchedDancerNames];

  DancePartnerMatchState addMatch(String match) {
    List<String> newMatchNames = matchedDancerNames.toList(growable: true);
    newMatchNames.add(match);
    return DancePartnerMatchState(false, newMatchNames, thisDancerName);
  }
}
