import 'package:equatable/equatable.dart';

class LoginState extends Equatable {
  final String email;
  final String name;
  final String pictureUrl;
  final bool isLoggedIn;

  const LoginState(this.email, this.name, this.pictureUrl, this.isLoggedIn);

  static LoginState initial() {
    return const LoginState("no@one.com", "no body", "", false);
  }

  LoginState logOut() {
    return LoginState(email, name, pictureUrl, false);
  }

  LoginState login(String email, String name, String pictureUrl) {
    return LoginState(email, name, pictureUrl, true);
  }

  @override
  List<Object> get props => [email, name, pictureUrl, isLoggedIn];
}
