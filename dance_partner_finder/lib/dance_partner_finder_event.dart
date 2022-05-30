part of 'dance_partner_finder_bloc.dart';

abstract class DancePartnerEvent extends Equatable {
  final String dancerName;

  const DancePartnerEvent(this.dancerName);

  @override
  List<Object?> get props => [dancerName];

}

class DancerLikedEvent extends DancePartnerEvent{
  const DancerLikedEvent(super.dancerName);
}

class DancerDislikedEvent extends DancePartnerEvent{
  const DancerDislikedEvent(super.dancerName);
}

