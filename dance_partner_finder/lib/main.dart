import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

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
      // BlocProvider(
      //   create: (_) => DancePartnerBloc(),
      //   child: DancePartnerSelectWidget(),
      // ),
    );
  }
}
class DancePartnerSelectWidget extends StatelessWidget {
  const DancePartnerSelectWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Image.asset('images/tom.jpg');
  }
}

