import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user.dart';

final currentUserProvider = StateNotifierProvider<AuthNotifier, User?>((ref) {
  return AuthNotifier();
});

class AuthNotifier extends StateNotifier<User?> {
  AuthNotifier() : super(null);

  void login(String name, String email) {
    state = User(
      id: email.toLowerCase().replaceAll(RegExp(r'[^a-z0-9]'), ''),
      name: name,
      email: email,
      balance: 2450.00,
    );
  }

  void logout() {
    state = null;
  }

  void updateBalance(double newBalance) {
    if (state != null) {
      state = state!.copyWith(balance: newBalance);
    }
  }
}
