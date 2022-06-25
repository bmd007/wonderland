import 'package:dance_partner_finder/dancer_chat_widget.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dancer_match_and_chat/dancer_chat_bloc.dart';
import 'bloc/dancer_match_and_chat/dancer_chat_event.dart';
import 'bloc/dancer_match_and_chat/dancer_chat_state.dart';
import 'bloc/login/login_cubit.dart';

class DancePartnerMatchesWidget extends StatelessWidget {

  const DancePartnerMatchesWidget({super.key});

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => DancerMatchAndChatBloc(loginCubit.state.email),
      child: BlocBuilder<DancerMatchAndChatBloc, DancerMatchAndChatState>(
        builder: (context, state) {
          if (state.isLoading) {
            return Image.asset('images/wait.png');
          } else if (state.isChattingWithSomeOne()) {
            return DancerChatWidget();
          }
          return ListView(
              padding: const EdgeInsets.symmetric(vertical: 8.0),
              children: state.chatHistory.keys
                  .map(
                    (matchedDancerName) => DancePartnerMatchWidget(
                        matchedDancerName: matchedDancerName,
                        lastMessage: state.lastMessage(matchedDancerName)),
                  )
                  .toList());
        },
      ),
    );
  }
}

class DancePartnerMatchWidget extends StatelessWidget {
  final String matchedDancerName;
  final String lastMessage;

  const DancePartnerMatchWidget({super.key, required this.matchedDancerName, required this.lastMessage});

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
    var bloc = context.watch<DancerMatchAndChatBloc>();
    return Card(
      child: ListTile(
        onLongPress: () {},
        onTap: () => bloc.add(WantedToChatEvent(matchedDancerName)),
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

