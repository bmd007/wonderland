part of 'dance_partner_finder_bloc.dart';

abstract class DancePartnerState extends Equatable {
  final List<String> dancerNames;
  final String currentDancerName;
  final bool isLoading;

  @override
  const DancePartnerState(this.currentDancerName, this.isLoading, this.dancerNames);

  @override
  List<Object> get props => [currentDancerName, isLoading, dancerNames];
}

class DancePartnerLoading extends DancePartnerState {
  const DancePartnerLoading() : super('', true, const []);
}
