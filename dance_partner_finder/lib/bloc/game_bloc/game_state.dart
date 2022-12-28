import 'package:equatable/equatable.dart';

import 'chat_message.dart';

class GameState extends Equatable {
  final Map<String, List<ChatMessage>> chatHistory;
  final bool isLoading;
  final String currentlyPlayingWith;
  final String lastTextInTextBox;
  static const String _noOne = "NO_ONE";

  const GameState(this.isLoading, this.chatHistory, this.currentlyPlayingWith, this.lastTextInTextBox);

  static GameState withThisPlayerName(thisPlayerName) {
    return const GameState(true, <String, List<ChatMessage>>{}, _noOne, "");
  }

  GameState loading() {
    return GameState(true, chatHistory, currentlyPlayingWith, lastTextInTextBox);
  }

  GameState loaded(
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
    return GameState(false, Map.fromEntries(newChatHistoryEntries), currentlyPlayingWith, lastTextInTextBox);
  }

  bool isPlayingWithSomeOne() {
    return _noOne != currentlyPlayingWith;
  }

  GameState chattingWith(String chatParticipant) {
    return GameState(false, chatHistory, chatParticipant, lastTextInTextBox);
  }

  GameState noMorePlaying() {
    return GameState(false, chatHistory, _noOne, "write here");
  }

  @override
  List<Object> get props => [isLoading, chatHistory, currentlyPlayingWith, lastTextInTextBox];

  GameState addMessage(ChatMessage loadedMassage) {
    return loaded(loadedMassage.participantName, [loadedMassage]);
  }

  String lastMessage(String matchedPlayerName) {
    if (!chatHistory.containsKey(matchedPlayerName)) {
      return "no such a match";
    }
    var chats = chatHistory[matchedPlayerName] ?? [];
    if(chats.isEmpty){
      return "no text yet";
    }
    return chats.last.text;
  }
}
