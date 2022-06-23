import 'package:equatable/equatable.dart';

import 'chat_message.dart';

abstract class DancerChatAndMatchEvent extends Equatable {
  const DancerChatAndMatchEvent();
}

class MessagesLoadedEvent extends DancerChatAndMatchEvent {
  final String chatParticipant;
  final List<ChatMessage> loadedMassages;

  const MessagesLoadedEvent(this.chatParticipant, this.loadedMassages);

  @override
  List<Object?> get props => [chatParticipant, loadedMassages];
}

class MessageLoadedEvent extends DancerChatAndMatchEvent {
  final String chatParticipant;
  final ChatMessage loadedMassage;

  const MessageLoadedEvent(this.chatParticipant, this.loadedMassage);

  @override
  List<Object?> get props => [chatParticipant, loadedMassage];
}

class MatchFoundEvent extends DancerChatAndMatchEvent {
  final String matchName;

  const MatchFoundEvent(this.matchName);

  @override
  List<Object?> get props => [matchName];
}

class StompConnectionReadyEvent extends DancerChatAndMatchEvent {
  const StompConnectionReadyEvent();

  @override
  List<Object?> get props => ['ignore'];
}
