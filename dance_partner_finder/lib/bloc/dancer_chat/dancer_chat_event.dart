import 'package:equatable/equatable.dart';

import 'chat_message.dart';

abstract class DancerChatEvent extends Equatable {
  const DancerChatEvent();
}

class MessagesLoadedEvent extends DancerChatEvent {
  final String chatParticipant;
  final List<ChatMessage> loadedMassages;
  const MessagesLoadedEvent(this.chatParticipant, this.loadedMassages);

  @override
  List<Object?> get props => [chatParticipant, loadedMassages];
}

class MessageLoadedEvent extends DancerChatEvent {
  final String chatParticipant;
  final ChatMessage loadedMassage;

  const MessageLoadedEvent(this.chatParticipant, this.loadedMassage);

  @override
  List<Object?> get props => [chatParticipant, loadedMassage];
}

class StompConnectionReadyEvent extends DancerChatEvent {
  const StompConnectionReadyEvent();

  @override
  List<Object?> get props => ['ignore'];
}
