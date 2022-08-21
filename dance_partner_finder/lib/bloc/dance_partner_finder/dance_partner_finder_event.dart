import 'package:equatable/equatable.dart';

abstract class DancePartnerFinderEvent extends Equatable {
  const DancePartnerFinderEvent();
}

class DancerLikedEvent extends DancePartnerFinderEvent {
  final String dancerName;

  const DancerLikedEvent(this.dancerName);

  @override
  List<Object?> get props => [dancerName];
}

class DancerDislikedEvent extends DancePartnerFinderEvent {
  final String dancerName;

  const DancerDislikedEvent(this.dancerName);

  @override
  List<Object?> get props => [dancerName];
}

class SearchingRadiusEnteredEvent extends DancePartnerFinderEvent {
  final double searchingRadius;

  const SearchingRadiusEnteredEvent(this.searchingRadius);

  @override
  List<Object?> get props => [searchingRadius];
}

class DancersLoadedEvent extends DancePartnerFinderEvent {
  final List<String> loadedDancerNames;

  const DancersLoadedEvent(this.loadedDancerNames);

  @override
  List<Object?> get props => [loadedDancerNames];
}

class PotentialDancerPartnerFoundEvent extends DancePartnerFinderEvent {
  final String potentialDancePartnerName;

  const PotentialDancerPartnerFoundEvent(this.potentialDancePartnerName);

  @override
  List<Object?> get props => [potentialDancePartnerName];
}
