import 'package:equatable/equatable.dart';

import 'video_chat_message.dart';

class VideoChatState extends Equatable {
  final Map<String, List<ChatMessage>> chatHistory;
  final bool isLoading;
  final String currentlyChattingWith;
  final String lastTextInTextBox;
  static const String _noOne = "NO_ONE";

  const VideoChatState(this.isLoading, this.chatHistory, this.currentlyChattingWith, this.lastTextInTextBox);

  static VideoChatState withThisDancerName(thisDancerName) {
    return const VideoChatState(true, <String, List<ChatMessage>>{}, _noOne, "");
  }

  VideoChatState loading() {
    return VideoChatState(true, chatHistory, currentlyChattingWith, lastTextInTextBox);
  }

  VideoChatState loaded(
      String chatParticipant, List<ChatMessage> loadedMassages) {
    List<ChatMessage> newMessageListForParticipant = List.empty(growable: true);
    if (!chatHistory.containsKey(chatParticipant) ||
        (chatHistory.containsKey(chatParticipant) && chatHistory[chatParticipant]!.isEmpty)) {
      newMessageListForParticipant
          .add(ChatMessage("start of your conversation with $chatParticipant", MessageType.systemic, _noOne));
    } else {
      newMessageListForParticipant.addAll(chatHistory[chatParticipant]!);
    }
    newMessageListForParticipant.addAll(loadedMassages);
    var newEntry = MapEntry(chatParticipant, newMessageListForParticipant.toList(growable: false));
    var newChatHistoryEntries =
        chatHistory.entries.where((element) => element.key != chatParticipant).followedBy([newEntry]);
    return VideoChatState(false, Map.fromEntries(newChatHistoryEntries), currentlyChattingWith, lastTextInTextBox);
  }

  bool isChattingWithSomeOne() {
    return _noOne != currentlyChattingWith;
  }

  VideoChatState chattingWith(String chatParticipant) {
    return VideoChatState(false, chatHistory, chatParticipant, lastTextInTextBox);
  }

  VideoChatState noMoreChatting() {
    return VideoChatState(false, chatHistory, _noOne, "write here");
  }

  @override
  List<Object> get props => [isLoading, chatHistory, currentlyChattingWith, lastTextInTextBox];

  VideoChatState addMessage(ChatMessage loadedMassage) {
    return loaded(loadedMassage.participantName, [loadedMassage]);
  }

  VideoChatState addMatch(String chatParticipant) {
    return loaded(chatParticipant, []);
  }

  String lastMessage(String matchedDancerName) {
    if (!chatHistory.containsKey(matchedDancerName)) {
      return "no such a match";
    }
    var chats = chatHistory[matchedDancerName] ?? [];
    if(chats.isEmpty){
      return "no text yet";
    }
    return chats.last.text;
  }
  //todo implement removing messages related to a specific person

  VideoChatState typing(String text) {
    return VideoChatState(isLoading, chatHistory, currentlyChattingWith, text);
  }
}
