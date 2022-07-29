import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/login/login_cubit.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';
import 'bloc/profile_bloc/profile_edit_state.dart';
import 'dance_partner_select_widget.dart';

class DanceProfileEditWidget extends StatelessWidget {
  const DanceProfileEditWidget({super.key});

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => ProfileEditBloc(),
      child: BlocBuilder<ProfileEditBloc, ProfileEditState>(
        builder: (context, state) {
          var profileEditBloc = context.watch<ProfileEditBloc>();
          if (state.isLoading) {
            return Image.asset('images/wait.png');
          }
          return Scaffold(
              appBar: AppBar(
                  centerTitle: true,
                  title: TextButton(onPressed: () => {}, child: const Text("to change it")),
                  actions: [
                    IconButton(
                      onPressed: () =>
                          Navigator.push(context, MaterialPageRoute(builder: (context) => DancePartnerSelectWidget())),
                      icon: Image.asset('images/match.png', height: 140, width: 140),
                    )
                  ]),
              body: body(profileEditBloc, loginCubit));
        },
      ),
    );
  }

  Image loadProfileImage(String dancerEmail) {
    return Image.asset('images/$dancerEmail.png', fit: BoxFit.fitHeight);
  }

  Widget body(ProfileEditBloc profileEditBloc, LoginCubit loginCubit) {
    return loginCubit.state.email.isNotEmpty && !profileEditBloc.state.isLoading
        ? Stack(
            fit: StackFit.expand,
            children: [
              loadProfileImage(loginCubit.state.email),
              Column(
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    loginCubit.state.email,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 50,
                    ),
                  ),
                  Text(
                    loginCubit.state.name,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 50,
                    ),
                  ),
                ],
              )
            ],
          )
        : Image.asset('images/wait.png');
  }
}
