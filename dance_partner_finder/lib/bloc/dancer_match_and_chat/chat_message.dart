import 'package:equatable/equatable.dart';

class ChatMessage extends Equatable {

  final String text;
  final bool sentOrReceived; //todo refactor to enum
  final String participantName;

  const ChatMessage(this.text, this.sentOrReceived, this.participantName);

  @override
  List<Object?> get props => [text, sentOrReceived, participantName];

}