import 'package:equatable/equatable.dart';

import 'chat_message.dart';

class DancerChatState extends Equatable {
  final Map<String, List<ChatMessage>> chatHistory;
  final bool isLoading;
  final String thisDancerName; //todo might not be needed as a state here

  const DancerChatState(
      this.isLoading, this.chatHistory, this.thisDancerName);

  static DancerChatState withThisDancerName(thisDancerName) {
    return DancerChatState(
        true, const <String, List<ChatMessage>>{}, thisDancerName);
  }

  DancerChatState loading() {
    return DancerChatState(true, chatHistory, thisDancerName);
  }

  DancerChatState loaded(
      String chatParticipant, List<ChatMessage> loadedMassages) {
    chatHistory.update(chatParticipant,
        (value) => value.followedBy(loadedMassages).toList(growable: false),
        ifAbsent: () => loadedMassages);
    return DancerChatState(false, chatHistory, thisDancerName);
  }

  @override
  List<Object> get props => [isLoading, chatHistory, thisDancerName];

  DancerChatState addMessage(
      String chatParticipant, ChatMessage loadedMassage) {
    chatHistory.update(chatParticipant,
        (value) => value.followedBy([loadedMassage]).toList(growable: false),
        ifAbsent: () => [loadedMassage].toList(growable: false));
    return DancerChatState(false, chatHistory, thisDancerName);
  }

  //todo implement removing messages related to a specific person
}
