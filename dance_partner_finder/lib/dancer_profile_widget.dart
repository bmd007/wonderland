import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/login/login_cubit.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';
import 'bloc/profile_bloc/profile_edit_state.dart';
import 'dance_partner_select_widget.dart';

class DanceProfileWidget extends StatelessWidget {
  const DanceProfileWidget({super.key});

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => ProfileEditBloc(),
      child: BlocBuilder<ProfileEditBloc, ProfileEditState>(
        builder: (context, state) {
          if (state.isLoading) {
            return Image.asset('images/wait.png');
          }
          return Scaffold(
              appBar: AppBar(
                  actions: [
                    TextButton(
                      onPressed: () =>
                          IconButton(
                            onPressed: () =>
                                Navigator.push(context,
                                    MaterialPageRoute(builder: (context) => DancePartnerSelectWidget())),
                            icon: Image.asset(
                              'images/match.gif',
                              height: 40,
                              width: 40,
                            ),
                          ),
                      child: const Text("start matching", style: TextStyle(color: Colors.black)),
                    )
                  ])
              ,
              body:
          );
        },
      ),
    );
  }
}
