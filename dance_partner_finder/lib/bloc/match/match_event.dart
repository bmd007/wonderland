part of 'match_bloc.dart';

abstract class MatchEvent extends Equatable {}

class DancePartnerFoundEvent extends MatchEvent{
  final String dancerName;
  DancePartnerFoundEvent(this.dancerName);
  @override
  List<Object?> get props => [dancerName];
}
