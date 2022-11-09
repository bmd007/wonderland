import 'package:equatable/equatable.dart';

class VideoChatState extends Equatable {
  final bool isLoading;
  final String currentlyChattingWith;
  final String thisDancerName;
  final String offer;
  final String answer;
  static const String _noOne = "NO_ONE";

  const VideoChatState(this.isLoading, this.answer, this.currentlyChattingWith, this.offer, this.thisDancerName);

  static VideoChatState withThisDancerName(String thisDancerName, String currentlyChattingWith) {
    return const VideoChatState(false, "", _noOne, "", _noOne);
  }

  VideoChatState loading() {
    return VideoChatState(true, "", currentlyChattingWith, offer, thisDancerName);
  }

  bool isChattingWithSomeOne() {
    return _noOne != currentlyChattingWith;
  }

  VideoChatState offered(String offer) {
    return VideoChatState(false, "", currentlyChattingWith, offer, thisDancerName);
  }

  VideoChatState answered(String answer) {
    return VideoChatState(false, answer, currentlyChattingWith, offer, thisDancerName);
  }

  VideoChatState noMoreChatting() {
    return const VideoChatState(false, "", _noOne, "", _noOne);
  }

  @override
  List<Object> get props => [isLoading, answer, currentlyChattingWith, offer, thisDancerName];
}
