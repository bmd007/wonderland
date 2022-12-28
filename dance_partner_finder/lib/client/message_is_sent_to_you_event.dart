import 'dart:convert';

class MessageIsSentToYouEvent {
  final String content;
  final String sender;

  MessageIsSentToYouEvent(this.content, this.sender);

  static MessageIsSentToYouEvent fromJson(String jsonString){
    Map<String, dynamic> keyValueMap = jsonDecode(jsonString);
    return MessageIsSentToYouEvent(keyValueMap["content"], keyValueMap["sender"]);
  }
}
