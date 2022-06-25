import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:rxdart/rxdart.dart';

import 'login_state.dart';

class LoginCubit extends Cubit<LoginState> {
  LoginCubit() : super(LoginState.initial()) {
    FirebaseAuth.instance.authStateChanges().listen((User? user) {
      if (user == null) {
        print('User is currently signed out!');
        emit(state.logOut());
      } else {
        print('User is signed in! $user');

        ClientHolder.apiGatewayHttpClient
            .post('/v1/chat/queues/user/${user.email!}')
            .asStream()
            .doOnError((p0, p1) => print("error creating Q for ${user.email!}: $p0 $p1"))
            .forEach((element) => print(element));

        emit(state.login(user.email!, user.displayName!, user.photoURL!));
      }
    });
  }

  void singOff() {
    FirebaseAuth.instance.signOut();
  }

  void signInWithGoogle() {
    (kIsWeb ? _signInWithGoogleWeb() : _signInWithGoogleNonWeb())
        .asStream()
        .doOnError((p0, p1) => print("login error $p0 $p1"))
        .forEach((element) => print(element));
  }

  Future<UserCredential> _signInWithGoogleNonWeb() async {
    final GoogleSignInAccount? googleUser = await GoogleSignIn().signIn();
    final GoogleSignInAuthentication? googleAuth = await googleUser?.authentication;
    final credential = GoogleAuthProvider.credential(
      accessToken: googleAuth?.accessToken,
      idToken: googleAuth?.idToken,
    );
    return await FirebaseAuth.instance.signInWithCredential(credential);
  }

  Future<UserCredential> _signInWithGoogleWeb() async {
    GoogleAuthProvider googleProvider = GoogleAuthProvider();
    googleProvider.addScope('https://www.googleapis.com/auth/contacts.readonly');
    googleProvider.setCustomParameters({'login_hint': 'bmd579@gmail.com'});
    return FirebaseAuth.instance.signInWithPopup(googleProvider);
  }
}
