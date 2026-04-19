import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';
import '../models/chat_message.dart';

const _uuid = Uuid();

final chatProvider =
    StateNotifierProvider<ChatNotifier, Map<String, List<ChatMessage>>>((ref) {
  return ChatNotifier();
});

class ChatNotifier extends StateNotifier<Map<String, List<ChatMessage>>> {
  ChatNotifier()
      : super({
          'alice': [
            ChatMessage(
              id: _uuid.v4(),
              senderId: 'alice',
              senderName: 'Alice Johnson',
              recipientId: 'me',
              content: 'Hey! Did you get the transfer?',
              timestamp: DateTime.now().subtract(const Duration(hours: 2)),
              isMe: false,
            ),
            ChatMessage(
              id: _uuid.v4(),
              senderId: 'me',
              senderName: 'You',
              recipientId: 'alice',
              content: 'Yes, got it! Thanks for splitting dinner.',
              timestamp: DateTime.now()
                  .subtract(const Duration(hours: 1, minutes: 55)),
              isMe: true,
            ),
          ],
          'bob': [
            ChatMessage(
              id: _uuid.v4(),
              senderId: 'me',
              senderName: 'You',
              recipientId: 'bob',
              content: 'Sent you the money for the coffee beans!',
              timestamp: DateTime.now().subtract(const Duration(days: 1)),
              isMe: true,
            ),
            ChatMessage(
              id: _uuid.v4(),
              senderId: 'bob',
              senderName: 'Bob Smith',
              recipientId: 'me',
              content: 'Awesome, received it. Enjoy the brew!',
              timestamp: DateTime.now()
                  .subtract(const Duration(hours: 23, minutes: 30)),
              isMe: false,
            ),
          ],
        });

  void sendMessage({
    required String contactId,
    required String content,
    required String senderName,
  }) {
    final message = ChatMessage(
      id: _uuid.v4(),
      senderId: 'me',
      senderName: senderName,
      recipientId: contactId,
      content: content,
      timestamp: DateTime.now(),
      isMe: true,
    );

    final messages = Map<String, List<ChatMessage>>.from(state);
    final contactMessages = List<ChatMessage>.from(messages[contactId] ?? []);
    contactMessages.add(message);
    messages[contactId] = contactMessages;
    state = messages;
  }
}
