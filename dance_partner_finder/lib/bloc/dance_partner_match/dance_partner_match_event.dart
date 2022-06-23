import 'package:equatable/equatable.dart';

abstract class DancePartnerMatchEvent extends Equatable {
  const DancePartnerMatchEvent();
}

class DancerMatchesLoadedEvent extends DancePartnerMatchEvent {
  final List<String> loadedDancerNames;

  const DancerMatchesLoadedEvent(this.loadedDancerNames);

  @override
  List<Object?> get props => [loadedDancerNames];
}

class MatchFoundEvent extends DancePartnerMatchEvent {
  final String dancePartnerMatchName;

  const MatchFoundEvent(this.dancePartnerMatchName);

  @override
  List<Object?> get props => [dancePartnerMatchName];
}
