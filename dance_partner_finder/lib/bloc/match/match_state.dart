part of 'match_cubit.dart';

class MatchState extends Equatable {
  final bool matchFound;
  final bool loading;
  final String thisDancerName;

  const MatchState(this.loading, this.matchFound, this.thisDancerName);

  static MatchState initial(){
    return const MatchState(true, false, "");
  }

  MatchState setThisDancerName(String thisDancerName){
    return MatchState(true, false, thisDancerName);
  }

  MatchState setMatchFound(){
    return MatchState(false, true, thisDancerName);
  }

  @override
  List<Object> get props => [loading, matchFound, thisDancerName];
}

