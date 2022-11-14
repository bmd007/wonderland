import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_bloc.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_event.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';

class VideoChatWidget extends StatelessWidget {
  final String chatParty;

  const VideoChatWidget({super.key, required this.chatParty});

  SizedBox videoRenderers(BuildContext context) {
    var videoChatBloc = context.watch<VideoChatBloc>();
    return SizedBox(
      height: 150,
      child: Row(children: [
        Flexible(
          child: Container(
            key: const Key('local'),
            margin: const EdgeInsets.fromLTRB(5.0, 5.0, 5.0, 5.0),
            decoration: const BoxDecoration(color: Colors.black),
            child: RTCVideoView(videoChatBloc.localVideoRenderer),
          ),
        ),
        Flexible(
          child: Container(
            key: const Key('remote'),
            margin: const EdgeInsets.fromLTRB(5.0, 5.0, 5.0, 5.0),
            decoration: const BoxDecoration(color: Colors.red),
            child: RTCVideoView(videoChatBloc.remoteVideoRenderer),
          ),
        ),
      ]),
    );
  }

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => VideoChatBloc(loginCubit.state.email, chatParty),
      child: BlocBuilder<VideoChatBloc, VideoChatState>(
        builder: (context, state) {
          return Scaffold(
              appBar: AppBar(centerTitle: true, title: Text("talking with $chatParty")),
              body: body(context, loginCubit));
        },
      ),
    );
  }

  Widget body(BuildContext context, LoginCubit loginCubit) {
    var videoChatBloc = context.watch<VideoChatBloc>();
    return Column(
      children: [
        videoRenderers(context),
        Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () {
                videoChatBloc.add(const OfferCreationRequestedEvent());
              },
              child: const Text("Offer"),
            ),
            const SizedBox(
              height: 10,
            ),
            const SizedBox(
              height: 10,
            ),
          ],
        )
      ],
    );
  }
}
