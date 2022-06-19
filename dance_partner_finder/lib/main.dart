import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';
import 'dance_partner_matches_page.dart';

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
          BlocProvider(create: (context1) => DancePartnerFinderBloc()),
          // BlocProvider(create: (context1) => HasMatchCubit())
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
    var dancerBloc = context.watch<DancePartnerFinderBloc>();
    return Scaffold(
      appBar: appBar(dancerBloc),
      bottomNavigationBar: bottomNavigationBar(dancerBloc, context),
      body: body(dancerBloc),
    );
  }

  NavigationBar? bottomNavigationBar(
      DancePartnerFinderBloc dancerBloc, BuildContext context) {
    if (dancerBloc.state.thisDancerName.isNotEmpty) {}
    return NavigationBar(
      destinations: [
        IconButton(
          onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                  builder: (context) => DancePartnerMatchesWidget())),
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
          );
  }

  Widget body(DancePartnerFinderBloc dancerBloc) {
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
        : Image.asset('images/wait.png');
  }

  AppBar? appBar(DancePartnerFinderBloc dancerBloc) {
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
