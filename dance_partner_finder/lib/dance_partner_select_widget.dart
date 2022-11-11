import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';
import 'bloc/dance_partner_finder/dance_partner_finder_event.dart';
import 'bloc/dance_partner_finder/dance_partner_finder_state.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';
import 'dance_partner_matches_page.dart';

class DancePartnerSelectWidget extends StatelessWidget {
  const DancePartnerSelectWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => DancePartnerFinderBloc(loginCubit.state.email),
      child: BlocBuilder<DancePartnerFinderBloc, DancePartnerFinderState>(
        builder: (context, state) {
          var dancerBloc = context.watch<DancePartnerFinderBloc>();
          return Scaffold(
            appBar: appBar(dancerBloc, context),
            body: body(dancerBloc, loginCubit.state.email),
          );
        },
      ),
    );
  }

  Widget body(DancePartnerFinderBloc dancerBloc, String thisDancerName) {
    return thisDancerName.isNotEmpty && !dancerBloc.state.isLoading && dancerBloc.state.dancerNames.isNotEmpty
        ? Stack(
            fit: StackFit.expand,
            children: [
              Image.network(ProfileEditBloc.profilePicUrl(dancerBloc.state.getCurrentDancerName()), fit: BoxFit.fitHeight),
              Column(
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    dancerBloc.state.getCurrentDancerName(),
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 50,
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                IconButton(
                  onPressed: () => dancerBloc.add(DancerDislikedEvent(dancerBloc.state.getCurrentDancerName())),
                  iconSize: 100,
                  icon: Image.asset('images/panda.gif'),
                ),
                      IconButton(
                        onPressed: () => dancerBloc.add(DancerLikedEvent(dancerBloc.state.getCurrentDancerName())),
                        iconSize: 150,
                        icon: Image.asset('images/dancer.png'),
                      ),
                    ],
                  )
                ],
              )
            ],
          )
        : Image.asset('images/wait.gif');
  }

  AppBar? appBar(DancePartnerFinderBloc dancerBloc, BuildContext context) {
    return AppBar(
      centerTitle: true,
      title: const Text("Choose your dance partner"),
      actions: [
        IconButton(
          onPressed: () =>
              Navigator.push(context, MaterialPageRoute(builder: (context) => const DancePartnerMatchesWidget())),
          icon: Image.asset('images/matches.png', height: 40, width: 40),
        )
      ],
      bottom: PreferredSize(
        preferredSize: const Size.fromHeight(70),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Slider(
                value: dancerBloc.state.searchingRadius,
                onChanged: (value) => {},
                onChangeEnd: (value) => dancerBloc.add(SearchingRadiusEnteredEvent(value)),
                min: 1,
                max: 400,
                divisions: 5,
                thumbColor: Colors.red,
                activeColor: Colors.black38),
            Text("${dancerBloc.state.searchingRadius.toInt()} KMs",
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 20,
                ))
          ],
        ),
      ),
    );
  }
}
