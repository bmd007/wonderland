import 'package:equatable/equatable.dart';

import 'chat_message.dart';

class DancerMatchAndChatState extends Equatable {
  final Map<String, List<ChatMessage>> chatHistory;
  final bool isLoading;
  final String thisDancerName; //todo might not be needed as a state here
  final String currentlyChattingWith;

  static const String _NO_ONE = "NO_ONE";

  const DancerMatchAndChatState(this.isLoading, this.chatHistory,
      this.thisDancerName, this.currentlyChattingWith);

  static DancerMatchAndChatState withThisDancerName(thisDancerName) {
    return DancerMatchAndChatState(
        true, const <String, List<ChatMessage>>{}, thisDancerName, _NO_ONE);
  }

  DancerMatchAndChatState loading() {
    return DancerMatchAndChatState(
        true, chatHistory, thisDancerName, currentlyChattingWith);
  }

  DancerMatchAndChatState loaded(
      String chatParticipant, List<ChatMessage> loadedMassages) {
    var newEntry = MapEntry(
        chatParticipant,
        loadedMassages
            .followedBy(chatHistory[chatParticipant] ?? [])
            .toList(growable: false));
    var newChatHistoryEntries = chatHistory.entries
        .where((element) => element.key != chatParticipant)
        .followedBy([newEntry]);
    var state = DancerMatchAndChatState(
        false,
        Map.fromEntries(newChatHistoryEntries),
        thisDancerName,
        currentlyChattingWith);
    return state;
  }

  bool isChattingWithSomeOne() {
    return _NO_ONE != currentlyChattingWith;
  }

  DancerMatchAndChatState chattingWith(String chatParticipant) {
    return DancerMatchAndChatState(
        false, chatHistory, thisDancerName, chatParticipant);
  }

  DancerMatchAndChatState noMoreChatting() {
    return DancerMatchAndChatState(false, chatHistory, thisDancerName, _NO_ONE);
  }

  @override
  List<Object> get props => [isLoading, chatHistory, thisDancerName, currentlyChattingWith];

  DancerMatchAndChatState addMessage(
      String chatParticipant, ChatMessage loadedMassage) {
    return loaded(chatParticipant, [loadedMassage]);
  }

  DancerMatchAndChatState addMatch(String chatParticipant) {
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
}
