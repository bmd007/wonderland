import 'package:equatable/equatable.dart';

import 'video_chat_message.dart';

abstract class VideoChatEvent extends Equatable {
  const VideoChatEvent();
}

class MessagesLoadedEvent extends VideoChatEvent {
  final String chatParticipant;
  final List<ChatMessage> loadedMassages;

  const MessagesLoadedEvent(this.chatParticipant, this.loadedMassages);

  @override
  List<Object?> get props => [chatParticipant, loadedMassages];
}

class MessageLoadedEvent extends VideoChatEvent {
  final String chatParticipant;
  final ChatMessage loadedMassage;

  const MessageLoadedEvent(this.chatParticipant, this.loadedMassage);

  @override
  List<Object?> get props => [chatParticipant, loadedMassage];
}

class MessageReceivedEvent extends VideoChatEvent {
  final ChatMessage massage;

  const MessageReceivedEvent(this.massage);

  @override
  List<Object?> get props => [massage];
}

class DancerSendMessageEvent extends VideoChatEvent {
  final ChatMessage massage;

  const DancerSendMessageEvent(this.massage);

  @override
  List<Object?> get props => [massage];
}

class MatchFoundEvent extends VideoChatEvent {
  final String matchName;

  const MatchFoundEvent(this.matchName);

  @override
  List<Object?> get props => [matchName];
}

class WantedToChatEvent extends VideoChatEvent {
  final String chatParticipant;

  const WantedToChatEvent(this.chatParticipant);

  @override
  List<Object?> get props => [chatParticipant];
}

class TextTypedEvent extends VideoChatEvent {
  final String text;

  const TextTypedEvent(this.text);

  @override
  List<Object?> get props => [text];
}

class BackToMatchesEvent extends VideoChatEvent {
  @override
  List<Object?> get props => ["ignore"];
}

class StompConnectionReadyEvent extends VideoChatEvent {
  @override
  List<Object?> get props => ['ignore'];
}
