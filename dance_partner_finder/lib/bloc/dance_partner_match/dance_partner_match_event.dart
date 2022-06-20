import 'package:equatable/equatable.dart';

abstract class DancePartnerMatchEvent extends Equatable {
  const DancePartnerMatchEvent();
}

class ThisDancerChoseNameEvent extends DancePartnerMatchEvent {
  final String thisDancerName;

  const ThisDancerChoseNameEvent(this.thisDancerName);

  @override
  List<Object?> get props => [thisDancerName];
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

class StompConnectionReadyEvent extends DancePartnerMatchEvent {
  const StompConnectionReadyEvent();

  @override
  List<Object?> get props => ['ignore'];
}
