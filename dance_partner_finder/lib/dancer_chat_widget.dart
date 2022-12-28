import 'package:dance_partner_finder/bloc/dancer_match_and_chat/chat_message.dart';
import 'package:dance_partner_finder/bloc/dancer_match_and_chat/dancer_chat_event.dart';
import 'package:dance_partner_finder/video_chat_screen/video_chat_widget.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dancer_match_and_chat/dancer_chat_bloc.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';

class DancerChatWidget extends StatelessWidget {
  final _messageTypingController = TextEditingController();

  DancerChatWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var matchAndChatBloc = context.watch<DancerMatchAndChatBloc>();
    return Scaffold(
      appBar: AppBar(
        elevation: 0,
        automaticallyImplyLeading: false,
        backgroundColor: Colors.white,
        flexibleSpace: SafeArea(
          child: Container(
            padding: const EdgeInsets.only(right: 16),
            child: Row(
              children: <Widget>[
                IconButton(
                  onPressed: () => matchAndChatBloc.add(BackToMatchesEvent()),
                  icon: const Icon(
                    Icons.arrow_back,
                    color: Colors.black,
                  ),
                ),
                const SizedBox(
                  width: 2,
                ),
                CircleAvatar(
                  backgroundImage:
                      NetworkImage(ProfileEditBloc.profilePicUrl(matchAndChatBloc.state.currentlyChattingWith)),
                  maxRadius: 20,
                ),
                const SizedBox(
                  width: 12,
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: <Widget>[
                          Text(
                            matchAndChatBloc.state.currentlyChattingWith,
                            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                          ),
                          const SizedBox(
                            height: 6,
                          ),
                          Text(
                            "Online",
                            style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
                          ),
                        ],
                      ),
                ),
                const Icon(
                  Icons.settings,
                  color: Colors.black54,
                ),
              ],
            ),
          ),
        ),
      ),
      body: chatBox(matchAndChatBloc, context),
    );
  }

  Widget chatBox(DancerMatchAndChatBloc chatBloc, BuildContext context) {
    _messageTypingController.value = _messageTypingController.value.copyWith(
      text: chatBloc.state.lastTextInTextBox,
      selection: TextSelection.collapsed(offset: chatBloc.state.lastTextInTextBox.length),
    );
    return Stack(
      children: <Widget>[
        ListView.builder(
          itemCount: chatBloc.state.chatHistory[chatBloc.state.currentlyChattingWith]?.length ?? 0,
          shrinkWrap: true,
          padding: const EdgeInsets.only(top: 10, bottom: 60),
          physics: const BouncingScrollPhysics(),
          itemBuilder: (context, index) => _messageWidgetBuilder(chatBloc, index),
        ),
        Align(
          alignment: Alignment.bottomLeft,
          child: Container(
            padding: const EdgeInsets.only(left: 10, bottom: 10, top: 10),
            height: 60,
            width: double.infinity,
            color: Colors.white,
            child: Row(
              children: <Widget>[
                GestureDetector(
                  onTap: () {
                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (context) => VideoChatWidget(
                                  chatParty: chatBloc.state.currentlyChattingWith,
                                )));
                  },
                  child: Container(
                    height: 30,
                    width: 30,
                    decoration: BoxDecoration(
                      color: Colors.lightBlue,
                      borderRadius: BorderRadius.circular(30),
                    ),
                    child: const Icon(
                      Icons.ac_unit_sharp,
                      color: Colors.white,
                      size: 20,
                    ),
                  ),
                ),
                const SizedBox(
                  width: 15,
                ),
                Expanded(
                  child: TextField(
                    onChanged: (value) => chatBloc.add(TextTypedEvent(value)),
                    controller: _messageTypingController,
                    decoration: const InputDecoration(border: InputBorder.none),
                  ),
                ),
                const SizedBox(
                  width: 15,
                ),
                FloatingActionButton(
                  onPressed: () {
                    if (chatBloc.state.lastTextInTextBox.isNotEmpty) {
                      chatBloc.add(DancerWantsToSendMessageEvent(ChatMessage(
                          chatBloc.state.lastTextInTextBox, MessageType.sent, chatBloc.state.currentlyChattingWith)));
                    }
                  },
                  backgroundColor: Colors.blue,
                  elevation: 0,
                  child: const Icon(
                    Icons.send,
                    color: Colors.white,
                    size: 18,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  _messageWidgetBuilder(bloc, index) {
    ChatMessage message = bloc.state.chatHistory[bloc.state.currentlyChattingWith]![index];
    Alignment alignment = Alignment.topRight;
    Color? color = Colors.blue[200];
    if (message.isReceived()) {
      alignment = Alignment.topLeft;
      color = Colors.green.shade500;
    } else if (message.isSystemic()) {
      alignment = Alignment.topCenter;
      color = Colors.grey.shade200;
    }
    return Container(
        padding: const EdgeInsets.only(left: 16, right: 16, top: 10, bottom: 10),
        child: Align(
          alignment: alignment,
          child: Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              color: color,
            ),
            padding: const EdgeInsets.all(16),
            child: Text(
              message.text,
              style: const TextStyle(fontSize: 15),
            ),
          ),
        ));
  }
}
