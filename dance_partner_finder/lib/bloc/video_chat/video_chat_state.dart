import 'package:equatable/equatable.dart';

class VideoChatState extends Equatable {
  final String currentlyChattingWith;
  final String thisDancerName;
  static const String _noOne = "NO_ONE";

  const VideoChatState(this.currentlyChattingWith, this.thisDancerName);

  static VideoChatState withThisDancerName(String thisDancerName, String currentlyChattingWith) {
    return const VideoChatState(_noOne,  _noOne);
  }

  bool isChattingWithSomeOne() {
    return _noOne != currentlyChattingWith;
  }

  @override
  List<Object> get props => [currentlyChattingWith, thisDancerName];
}
