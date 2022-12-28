import 'package:equatable/equatable.dart';

import 'chat_message.dart';

abstract class GameEvent extends Equatable {
  const GameEvent();
}

class MessagesLoadedEvent extends GameEvent {
  final String chatParticipant;
  final List<ChatMessage> loadedMassages;

  const MessagesLoadedEvent(this.chatParticipant, this.loadedMassages);

  @override
  List<Object?> get props => [chatParticipant, loadedMassages];
}

class MessageLoadedEvent extends GameEvent {
  final String chatParticipant;
  final ChatMessage loadedMassage;

  const MessageLoadedEvent(this.chatParticipant, this.loadedMassage);

  @override
  List<Object?> get props => [chatParticipant, loadedMassage];
}

class JoystickMovedMessageReceivedEvent extends GameEvent {

  const JoystickMovedMessageReceivedEvent();

  @override
  List<Object?> get props => [];
}

class ShootButtonPushedMessageReceivedEvent extends GameEvent {

  const ShootButtonPushedMessageReceivedEvent();

  @override
  List<Object?> get props => [];
}

class StompConnectionReadyEvent extends GameEvent {
  @override
  List<Object?> get props => ['ignore'];
}

class JoystickMovedEvent extends GameEvent {

  const JoystickMovedEvent();

  @override
  List<Object?> get props => [];
}

class ShootButtonPushedEvent extends GameEvent {

  const ShootButtonPushedEvent();

  @override
  List<Object?> get props => [];
}
