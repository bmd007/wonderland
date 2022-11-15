import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_bloc.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';

import 'components/bootom_menu.dart';
import 'components/header_comp.dart';

class VideoChatWidget extends StatelessWidget {
  final String chatParty;

  const VideoChatWidget({super.key, required this.chatParty});

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
    // Flexible(
    //     child: Container(
    //       key: const Key('remote'),
    //       // margin: const EdgeInsets.fromLTRB(5.0, 5.0, 5.0, 5.0),
    //       decoration: const BoxDecoration(color: Colors.red),
    //       child: RTCVideoView(videoChatBloc.remoteVideoRenderer),
    //     )
    final size = MediaQuery.of(context).size;
    return Scaffold(
      body: Stack(
        children: [
          GestureDetector(
            onDoubleTap: () {},
            child: Container(
              height: size.height,
              width: size.width,
              child: Flexible(
                child: Container(
                  key: const Key('remote'),
                  // margin: const EdgeInsets.fromLTRB(5.0, 5.0, 5.0, 5.0),
                  child: RTCVideoView(videoChatBloc.remoteVideoRenderer),
                ),
              ),
            ),
          ),
          AnimatedPositioned(
            left: 0,
            right: 0,
            top: -200,
            duration: const Duration(milliseconds: 300),
            child: CallScreenHeaderComponents(ctx: context),
          ),
          const AnimatedPositioned(
            duration: Duration(milliseconds: 300),
            bottom: -250,
            left: 0,
            right: 0,
            child: BottomMenu(),
          ),
        ],
      ),
    );
  }
}
