import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dance Partner Matches',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MultiBlocProvider(
        providers: [],
        child: DancePartnerMatchesWidget(),
      ),
    );
  }
}

class DancePartnerMatchWidget extends StatelessWidget {
  final String dancerName;

  const DancePartnerMatchWidget({super.key, required this.dancerName});

  TextStyle? _getTextStyle(bool newMessageAvailable) {
    if (newMessageAvailable) {
      return const TextStyle(
        color: Colors.redAccent,
        decoration: TextDecoration.underline,
      );
    }
    return const TextStyle(
      color: Colors.black54,
      decoration: TextDecoration.lineThrough,
    );
  }

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: () {},
      leading: CircleAvatar(
          child:
              Image.asset('images/${dancerName}.png', fit: BoxFit.fitHeight)),
      title: Text(dancerName, style: _getTextStyle(true)),
    );
  }
}

class DancePartnerMatchesWidget extends StatelessWidget {
  DancePartnerMatchesWidget({Key? key}) : super(key: key);
  final List<String> _matchedDancerPartnerNames = ["taylor", "jlo"];

  @override
  Widget build(BuildContext context) {
    return ListView(
        padding: const EdgeInsets.symmetric(vertical: 8.0),
        children: _matchedDancerPartnerNames
            .map((name) => DancePartnerMatchWidget(dancerName: name))
            .toList());
  }
}
