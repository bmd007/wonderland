part of 'dance_partner_finder_bloc.dart';

abstract class DancePartnerEvent extends Equatable {
  const DancePartnerEvent();
}

class DancerLikedEvent extends DancePartnerEvent{
  final String dancerName;
  const DancerLikedEvent(this.dancerName);
  @override
  List<Object?> get props => [dancerName];
}

class DancerDislikedEvent extends DancePartnerEvent{
  final String dancerName;
  const DancerDislikedEvent(this.dancerName);
  @override
  List<Object?> get props => [dancerName];
}

class DancersLoadedEvent extends DancePartnerEvent{
  final List<String> loadedDancerNames;
  const DancersLoadedEvent(this.loadedDancerNames);
  @override
  List<Object?> get props => [loadedDancerNames];
}
