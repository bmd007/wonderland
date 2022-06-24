import 'package:dance_partner_finder/bloc/dancer_match_and_chat/chat_message.dart';
import 'package:dance_partner_finder/bloc/dancer_match_and_chat/dancer_chat_event.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dancer_match_and_chat/dancer_chat_bloc.dart';
import 'bloc/dancer_match_and_chat/dancer_chat_state.dart';

class DancerChatWidget extends StatelessWidget {
  final _messageTypingController = TextEditingController();

  DancerChatWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<DancerMatchAndChatBloc, DancerMatchAndChatState>(
      builder: (context, state) {
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
                      backgroundImage: AssetImage('images/${matchAndChatBloc.state.currentlyChattingWith}.png'),
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
          body: chatBox(matchAndChatBloc),
        );
      },
    );
  }

  Widget chatBox(DancerMatchAndChatBloc chatBloc) {
    return Stack(
      children: <Widget>[
        ListView.builder(
          itemCount: chatBloc.state.chatHistory[chatBloc.state.currentlyChattingWith]?.length ?? 0,
          shrinkWrap: true,
          padding: const EdgeInsets.only(top: 10, bottom: 10),
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
                  onTap: () {},
                  child: Container(
                    height: 30,
                    width: 30,
                    decoration: BoxDecoration(
                      color: Colors.lightBlue,
                      borderRadius: BorderRadius.circular(30),
                    ),
                    child: const Icon(
                      Icons.add,
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
                    controller: _messageTypingController,
                    decoration: const InputDecoration(
                        hintText: "Write message...",
                        hintStyle: TextStyle(color: Colors.black54),
                        border: InputBorder.none),
                  ),
                ),
                const SizedBox(
                  width: 15,
                ),
                FloatingActionButton(
                  onPressed: () => chatBloc.add(DancerSendMessageEvent(ChatMessage(
                      _messageTypingController.text, MessageType.sent, chatBloc.state.currentlyChattingWith))),
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
