part of 'dance_partner_finder_bloc.dart';

abstract class DancePartnerFinderState extends Equatable {
  final List<String> dancerNames;
  final String currentDancerName;
  final bool isLoading;

  @override
  const DancePartnerFinderState(this.currentDancerName, this.isLoading, this.dancerNames);

  @override
  List<Object> get props => [currentDancerName, isLoading, dancerNames];
}

class DancePartnerFinderLoading extends DancePartnerFinderState {
  const DancePartnerFinderLoading() : super('', true, const []);
}
