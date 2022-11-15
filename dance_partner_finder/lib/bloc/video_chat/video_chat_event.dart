import 'package:equatable/equatable.dart';

abstract class VideoChatEvent extends Equatable {
  const VideoChatEvent();
}
class AnswerReceivedEvent extends VideoChatEvent {
  final String answer;
  const AnswerReceivedEvent(this.answer);

  @override
  List<Object?> get props => [answer];
}

class CreateAnswerRequestedEvent extends VideoChatEvent {
  const CreateAnswerRequestedEvent();

  @override
  List<Object?> get props => [];
}

class OfferCreationRequestedEvent extends VideoChatEvent {
  const OfferCreationRequestedEvent();

  @override
  List<Object?> get props => [];
}

class CandidatePropagationRequestedEvent extends VideoChatEvent {
  final String candidate;

  const CandidatePropagationRequestedEvent(this.candidate);

  @override
  List<Object?> get props => [candidate];
}

class OfferReceivedEvent extends VideoChatEvent {
  final String offer;

  const OfferReceivedEvent(this.offer);

  @override
  List<Object?> get props => [offer];
}

class CandidateReceivedEvent extends VideoChatEvent {
  final String candidate;

  const CandidateReceivedEvent(this.candidate);

  @override
  List<Object?> get props => [candidate];
}

class StompConnectionReadyEvent extends VideoChatEvent {
  @override
  List<Object?> get props => ['ignore'];
}
