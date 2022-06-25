import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:firebase_auth/firebase_auth.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';
import 'dance_partner_select_widget.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(create: (context) => DancePartnerFinderBloc()),
      ],
      child: MaterialApp(
        title: 'Dance Partner Finder',
        theme: ThemeData(
          primarySwatch: Colors.blue,
        ),
        home: DancePartnerSelectWidget(),
      ),
    );
  }
}
