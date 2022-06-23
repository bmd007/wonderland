import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';
import 'bloc/dancer_chat/dancer_chat_bloc.dart';
import 'bloc/dancer_chat/dancer_chat_state.dart';

class ChatWithMatchedDancerWidget extends StatelessWidget {
  final String matchedDancerName;

  const ChatWithMatchedDancerWidget(
      {super.key, required this.matchedDancerName});

  @override
  Widget build(BuildContext context) {
    var dancerBloc = context.watch<
        DancePartnerFinderBloc>(); //todo create a user account/authentication bloc for holding this dance name
    return BlocProvider(
      create: (BuildContext context) =>
          DancerChatBloc(dancerBloc.state.thisDancerName),
      child: BlocBuilder<DancerChatBloc, DancerChatState>(
        builder: (context, state) {
          return Card(
              child: Column(
            children: [
              Text(dancerBloc.state.thisDancerName),
              // Text(state.chatHistory.keys.first),
            ],
          ));
        },
      ),
    );
  }
}
