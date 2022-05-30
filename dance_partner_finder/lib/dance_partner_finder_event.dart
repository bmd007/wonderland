part of 'dance_partner_finder_bloc.dart';

abstract class DancePartnerFinderEvent extends Equatable {
  final String dancerName;

  DancePartnerFinderEvent(this.dancerName);

  @override
  List<Object?> get props => [dancerName];

}

class DancerLikedEvent extends DancePartnerFinderEvent{
  DancerLikedEvent(super.dancerName);
}

class DancerDissLikedEvent extends DancePartnerFinderEvent{
  DancerDissLikedEvent(super.dancerName);
}

