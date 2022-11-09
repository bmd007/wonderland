import 'package:equatable/equatable.dart';

class ChatMessage extends Equatable {

  final String text;
  final MessageType messageType;
  final String participantName;

  const ChatMessage(this.text, this.messageType, this.participantName);

  @override
  List<Object?> get props => [text, messageType, participantName];

  bool isSent() {
    return messageType == MessageType.sent;
  }

  bool isReceived() {
    return messageType == MessageType.received;
  }

  bool isSystemic() {
    return messageType == MessageType.systemic;
  }
}

enum MessageType { sent, received, systemic }