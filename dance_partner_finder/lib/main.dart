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
        create: (context) => DancePartnerFinderBloc(),
        child: Stack(
          fit: StackFit.expand,
          children: [
            BlocBuilder<DancePartnerFinderBloc, DancePartnerFinderState>(
              buildWhen: (prev, state) => prev.runtimeType != state.runtimeType,
              builder: (context, state) {
                return Image.asset('images/tom.jpg', fit: BoxFit.fitHeight);
              },
            ),
            Column(
              mainAxisAlignment: MainAxisAlignment.end,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  "Tom",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 30,
                  ),
                ),
                BlocBuilder<DancePartnerFinderBloc, DancePartnerFinderState>(
                  builder: (context, state) {
                    var danceBloc = context.read<DancePartnerFinderBloc>();
                    return Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        IconButton(
                          onPressed: () => danceBloc.add(DancerLikedEvent(state.currentDancerName)),
                          iconSize: 100,
                          icon: Image.asset('images/tom.jpg'),
                        ),
                        IconButton(
                          onPressed: () => danceBloc.add(DancerDissLikedEvent(state.currentDancerName)),
                          iconSize: 150,
                          icon: Image.asset('images/dancer.png'),
                        ),
                      ],
                    );
                  },
                )
              ],
            )
          ],
        ),
      ),
    );
  }
}
