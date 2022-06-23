import 'package:equatable/equatable.dart';

import 'chat_message.dart';

abstract class MatchedDancerChatEvent extends Equatable {
  const MatchedDancerChatEvent();
}

class MessagesLoadedEvent extends MatchedDancerChatEvent {
  final String chatParticipant;
  final List<ChatMessage> loadedMassages;
  const MessagesLoadedEvent(this.chatParticipant, this.loadedMassages);

  @override
  List<Object?> get props => [chatParticipant, loadedMassages];
}

class MessageLoadedEvent extends MatchedDancerChatEvent {
  final String chatParticipant;
  final ChatMessage loadedMassage;

  const MessageLoadedEvent(this.chatParticipant, this.loadedMassage);

  @override
  List<Object?> get props => [chatParticipant, loadedMassage];
}

class StompConnectionReadyEvent extends MatchedDancerChatEvent {
  const StompConnectionReadyEvent();

  @override
  List<Object?> get props => ['ignore'];
}
