import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';
import 'bloc/dance_partner_finder/dance_partner_finder_event.dart';
import 'bloc/dance_partner_finder/dance_partner_finder_state.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';
import 'dance_partner_matches_page.dart';

class DancePartnerSelectWidget extends StatelessWidget {
  DancePartnerSelectWidget({Key? key}) : super(key: key);

  final _searchingRadiusTextController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => DancePartnerFinderBloc(loginCubit.state.email),
      child: BlocBuilder<DancePartnerFinderBloc, DancePartnerFinderState>(
        builder: (context, state) {
          var dancerBloc = context.watch<DancePartnerFinderBloc>();
          return Scaffold(
            // appBar: appBar(dancerBloc),
            bottomNavigationBar: bottomNavigationBar(dancerBloc, context, loginCubit.state.email),
            body: body(dancerBloc, loginCubit.state.email),
          );
        },
      ),
    );
  }

  NavigationBar? bottomNavigationBar(DancePartnerFinderBloc dancerBloc, BuildContext context, String thisDancerName) {
    return thisDancerName.isNotEmpty
        ? NavigationBar(
            destinations: [
              IconButton(
                onPressed: () => Navigator.push(context,
                    MaterialPageRoute(builder: (context) => const DancePartnerMatchesWidget())),
                icon: Image.asset(
                  'images/match.gif',
                  height: 40,
                  width: 40,
                ),
              ),
        Image.asset(
                'images/match.png',
                height: 40,
                width: 40,
              ),
            ],
            height: 40,
          )
        : null;
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

  AppBar? appBar(DancePartnerFinderBloc dancerBloc) {
    //todo change radius selector to another type of input than text
    return dancerBloc.state.searchingRadius == 0
        ? AppBar(
            title: TextField(
              controller: _searchingRadiusTextController,
            ),
            actions: [
                TextButton(
                  onPressed: () => dancerBloc
                      .add(SearchingRadiusEnteredEvent(int.parse(_searchingRadiusTextController.text.trim()))),
                  child: const Text("touch after radius entry", style: TextStyle(color: Colors.black)),
                )
              ])
        : null;
  }
}
