import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';
import 'bloc/match/match_cubit.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dance Partner Finder',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MultiBlocProvider(
        providers: [
          BlocProvider(create: (context) => DancePartnerBloc()),
          BlocProvider(create: (context) => HasMatchCubit())
        ],
        child: DancePartnerSelectWidget(),
      ),
    );
  }
}

class DancePartnerSelectWidget extends StatelessWidget {
  DancePartnerSelectWidget({Key? key}) : super(key: key);

  final _thisDancerNamTextController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    var dancerBloc = context.watch<DancePartnerBloc>();
    var matchCubit = context.watch<HasMatchCubit>();
    return Scaffold(
      appBar: appBar(dancerBloc),
      bottomNavigationBar: bottomNavigationBar(matchCubit, dancerBloc),
      body: body(dancerBloc),
    );
  }

  NavigationBar? bottomNavigationBar(
      HasMatchCubit matchCubit, DancePartnerBloc dancerBloc) {
    if (matchCubit.state.thisDancerName.isEmpty &&
        dancerBloc.state.thisDancerName.isNotEmpty) {
      matchCubit.setName(dancerBloc.state.thisDancerName);
    }
    return matchCubit.state.matchFound && !matchCubit.state.loading
        ? NavigationBar(
            destinations: [
              const Center(
                  child: Text("NEW MATCH",
                      style: TextStyle(
                          color: Colors.red, fontWeight: FontWeight.bold))),
              Image.asset(
                'images/match.gif',
                height: 40,
                width: 40,
              )
            ],
            height: 40,
          )
        : null;
  }

  Widget body(DancePartnerBloc dancerBloc) {
    return dancerBloc.state.thisDancerName.isNotEmpty &&
            !dancerBloc.state.isLoading
        ? Stack(
            fit: StackFit.expand,
            children: [
              Image.asset(
                  'images/${dancerBloc.state.getCurrentDancerName()}.png',
                  fit: BoxFit.fitHeight),
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
                        onPressed: () => dancerBloc.add(DancerDislikedEvent(
                            dancerBloc.state.getCurrentDancerName())),
                        iconSize: 100,
                        icon: Image.asset('images/panda.gif'),
                      ),
                      IconButton(
                        onPressed: () => dancerBloc.add(DancerLikedEvent(
                            dancerBloc.state.getCurrentDancerName())),
                        iconSize: 150,
                        icon: Image.asset('images/dancer.png'),
                      ),
                    ],
                  )
                ],
              )
            ],
          )
        : const Text(
            "loading or waiting for this dancer's name",
            style: TextStyle(color: Colors.redAccent),
          );
  }

  AppBar? appBar(DancePartnerBloc dancerBloc) {
    return dancerBloc.state.thisDancerName.isEmpty
        ? AppBar(
            title: TextField(
              controller: _thisDancerNamTextController,
            ),
            actions: [
                TextButton(
                  onPressed: () => dancerBloc.add(ThisDancerChoseNameEvent(
                      _thisDancerNamTextController.text)),
                  child: const Text("touch after naming",
                      style: TextStyle(color: Colors.black)),
                )
              ])
        : null;
  }
}
