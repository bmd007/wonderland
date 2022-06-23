import 'package:dance_partner_finder/bloc/dancer_match_and_chat/dancer_chat_event.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dancer_match_and_chat/dancer_chat_bloc.dart';
import 'bloc/dancer_match_and_chat/dancer_chat_state.dart';

class DancerChatWidget extends StatelessWidget {
  const DancerChatWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<DancerMatchAndChatBloc, DancerMatchAndChatState>(
      builder: (context, state) {
        var bloc = context.watch<DancerMatchAndChatBloc>();
        return Card(
          child: Column(
            children: [
              ListTile(
                onLongPress: () {},
                onTap: () => bloc.add(BackToMatchesEvent()),
                leading: CircleAvatar(
                    backgroundImage: AssetImage(
                        'images/${bloc.state.currentlyChattingWith}.png')),
                title: Text(bloc.state.currentlyChattingWith),
              ),
              Image(image: AssetImage(
                  'images/${bloc.state.currentlyChattingWith}.png'))
            ],
          ),
        );
      },
    );
  }
}
