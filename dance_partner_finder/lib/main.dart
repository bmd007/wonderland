import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'dance_partner_finder_bloc.dart';

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
      home: const DancePartnerSelectWidget(),
    );
  }
}

class DancePartnerSelectWidget extends StatelessWidget {
  const DancePartnerSelectWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocProvider(
        create: (context) => DancePartnerBloc(),
        child: BlocBuilder<DancePartnerBloc, DancePartnerState>(
          buildWhen: (prev, state) => prev.runtimeType != state.runtimeType,
          builder: (context, state) {
            var danceBloc = context.read<DancePartnerBloc>();
            return Stack(
              fit: StackFit.expand,
              children: [
                Image.asset('images/${state.getCurrentDancerName()}.jpg', fit: BoxFit.fitHeight),
                Column(
                  mainAxisAlignment: MainAxisAlignment.end,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      state.getCurrentDancerName(),
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
                          onPressed: () => danceBloc
                              .add(DancerLikedEvent(state.getCurrentDancerName())),
                          iconSize: 100,
                          icon: Image.asset('images/tom.jpg'),
                        ),
                        IconButton(
                          onPressed: () => danceBloc.add(
                              DancerDislikedEvent(state.getCurrentDancerName())),
                          iconSize: 150,
                          icon: Image.asset('images/dancer.png'),
                        ),
                      ],
                    )
                  ],
                )
              ],
            );
          },
        ),
      ),
    );
  }
}
