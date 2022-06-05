import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';

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
      home: DancePartnerSelectWidget(),
    );
  }
}

class DancePartnerSelectWidget extends StatelessWidget {
  DancePartnerSelectWidget({Key? key}) : super(key: key);

  final _thisDancerNamTextController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    var danceBloc = context.watch()<DancePartnerBloc>();
    var danceState = danceBloc.state;
    return Scaffold(
      appBar: danceState.thisDancerName.isEmpty
          ? AppBar(
              title: TextField(
                controller: _thisDancerNamTextController,
              ),
              actions: [
                  TextButton(
                    onPressed: () => danceBloc.add(ThisDancerChoseNameEvent(
                                  _thisDancerNamTextController.text)),
                          child: const Text("touch after naming",
                              style: TextStyle(color: Colors.black)),
                        )
                      ])
                : null,

            bottomNavigationBar: NavigationBar(destinations: [],),

            body: danceState.thisDancerName.isNotEmpty && !danceState.isLoading
                ? Stack(
                    fit: StackFit.expand,
                    children: [
                      Image.asset('images/${danceState.getCurrentDancerName()}.png',
                          fit: BoxFit.fitHeight),
                      Column(
                        mainAxisAlignment: MainAxisAlignment.end,
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Text(
                            danceState.getCurrentDancerName(),
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
                          onPressed: () => danceBloc.add(DancerDislikedEvent(
                              danceState.getCurrentDancerName())),
                          iconSize: 100,
                          icon: Image.asset('images/tom.png'),
                        ),
                        IconButton(
                                onPressed: () => danceBloc.add(DancerLikedEvent(
                                    danceState.getCurrentDancerName())),
                                iconSize: 150,
                                icon: Image.asset('images/dancer.png'),
                              ),
                            ],
                          )
                        ],
                      )
                    ],
                  )
                : Text(
                    "loading or waiting for this dancer's name",
                    style: TextStyle(color: Colors.redAccent),
                  ),
          );
  }
}
