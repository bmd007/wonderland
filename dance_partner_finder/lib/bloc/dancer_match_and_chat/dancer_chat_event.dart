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

class WantedToChatEvent extends DancerChatAndMatchEvent {
  final String chatParticipant;

  const WantedToChatEvent(this.chatParticipant);

  @override
  List<Object?> get props => [chatParticipant];
}

class BackToMatchesEvent extends DancerChatAndMatchEvent {
  @override
  List<Object?> get props => ["ignore"];
}

class StompConnectionReadyEvent extends DancerChatAndMatchEvent {
  @override
  List<Object?> get props => ['ignore'];
}
