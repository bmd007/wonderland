import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:dance_partner_finder/subject_table.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

void main() async {
  runApp(const MyApp())
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(create: (context) => LoginCubit()),
      ],
      child: MaterialApp(
        title: 'Dance Partner Finder',
        theme: ThemeData(
          primarySwatch: Colors.blue,
        ),
        home: const SubjectTableWidget(),
      ),
    );
  }
}
