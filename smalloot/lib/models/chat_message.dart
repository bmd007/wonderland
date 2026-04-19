class ChatMessage {
  final String id;
  final String senderId;
  final String senderName;
  final String recipientId;
  final String content;
  final DateTime timestamp;
  final bool isMe;

  const ChatMessage({
    required this.id,
    required this.senderId,
    required this.senderName,
    required this.recipientId,
    required this.content,
    required this.timestamp,
    required this.isMe,
  });
}
