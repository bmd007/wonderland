import 'package:dance_partner_finder/bloc/login/login_cubit.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_fonts/google_fonts.dart';

class LoginPage extends StatelessWidget {
  final Color primaryColor = const Color(0xff18203d);
  final Color secondaryColor = const Color(0xff232c51);

  final Color logoGreen = const Color(0xff25bcbb);

  final TextEditingController nameController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  LoginPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return Scaffold(
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          elevation: 0,
        ),
        backgroundColor: primaryColor,
        body: Container(
          alignment: Alignment.topCenter,
          margin: const EdgeInsets.symmetric(horizontal: 30),
          child: SingleChildScrollView(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                Text(
                  'Login with your Google Account',
                  textAlign: TextAlign.center,
                  style: GoogleFonts.openSans(color: Colors.white, fontSize: 28),
                ),
                const SizedBox(
                  height: 50,
                ),
                const SizedBox(height: 20),
                MaterialButton(
                  elevation: 0,
                  minWidth: double.maxFinite,
                  height: 50,
                  onPressed: () => loginCubit.signInWithGoogle(),
                  color: Colors.blue,
                  textColor: Colors.white,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: const <Widget>[
                      Icon(FontAwesomeIcons.google),
                      SizedBox(width: 10),
                      Text('Sign-in using Google', style: TextStyle(color: Colors.white, fontSize: 16)),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ));
  }
}
