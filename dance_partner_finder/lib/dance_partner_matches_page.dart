import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dancer_match_and_chat/dancer_chat_bloc.dart';
import 'bloc/dancer_match_and_chat/dancer_chat_state.dart';

class DancePartnerMatchesWidget extends StatelessWidget {
  final String thisDancerName;

  const DancePartnerMatchesWidget({super.key, required this.thisDancerName});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(
            create: (context) => DancerMatchAndChatBloc(thisDancerName))
      ],
      child: BlocBuilder<DancerMatchAndChatBloc, DancerMatchAndChatState>(
        builder: (context, state) {
          return state.isLoading
              ? Image.asset('images/wait.png')
              : ListView(
                  padding: const EdgeInsets.symmetric(vertical: 8.0),
                  children: state.chatHistory.keys
                      .map((matchedDancerName) => DancePartnerMatchWidget(
                          matchedDancerName: matchedDancerName,
                          lastMessage: state.lastMessage(matchedDancerName)))
                      .toList());
        },
      ),
    );
  }
}

class DancePartnerMatchWidget extends StatelessWidget {
  final String matchedDancerName;
  final String lastMessage;

  const DancePartnerMatchWidget(
      {super.key, required this.matchedDancerName, required this.lastMessage});

  TextStyle? _getTextStyle(bool newMessageAvailable) {
    if (newMessageAvailable) {
      return const TextStyle(
        color: Colors.redAccent,
        decoration: TextDecoration.underline,
      );
    }
    return const TextStyle(
      color: Colors.black54,
      decoration: TextDecoration.lineThrough,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        onLongPress: () {},
        onTap: () {},
        leading: CircleAvatar(
            backgroundImage: AssetImage('images/$matchedDancerName.png')),
        title: Column(children: [
          Text(matchedDancerName, style: _getTextStyle(true)),
          Text(lastMessage)
        ]),
      ),
    );
  }
}

