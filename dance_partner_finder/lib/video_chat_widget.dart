import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_bloc.dart';
import 'package:dance_partner_finder/bloc/video_chat/video_chat_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class VideoChatWidget extends StatelessWidget {
  final String chatParty;

  const VideoChatWidget({super.key, required this.chatParty});

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => VideoChatBloc(chatParty),
      child: BlocBuilder<VideoChatBloc, VideoChatState>(
        builder: (context, state) {
          return Scaffold(
              appBar: AppBar(centerTitle: true, title: Text("talking with $chatParty")), body: body(context, loginCubit));
        },
      ),
    );
  }

  Widget body(BuildContext context, LoginCubit loginCubit) {
    var videoChatBloc = context.watch<VideoChatBloc>();
    return !videoChatBloc.state.isLoading ? Image.asset('images/wait.gif') : Image.asset('images/wait.gif');
  }
}
