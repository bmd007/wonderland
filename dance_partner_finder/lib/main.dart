import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/login/login_state.dart';
import 'dance_partner_select_widget.dart';
import 'firebase_options.dart';
import 'login_page.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform)
      .then((value) => runApp(const MyApp()))
      .then((value) => flutter );
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

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
        home: BlocBuilder<LoginCubit, LoginState>(
          builder: (context, state) {
            return state.isLoggedIn
                ? DancePartnerSelectWidget()
                : LoginPage(); //todo show profile edit screen instead of DancePartnerSelectWidget
          },
        ),
      ),
    );
  }
}
