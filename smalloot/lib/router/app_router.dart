import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/auth_provider.dart';
import '../screens/chat_screen.dart';
import '../screens/contacts_screen.dart';
import '../screens/home_screen.dart';
import '../screens/login_screen.dart';
import '../screens/send_money_screen.dart';
import '../screens/shell_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final user = ref.watch(currentUserProvider);

  return GoRouter(
    initialLocation: user == null ? '/login' : '/',
    redirect: (context, state) {
      final loggedIn = user != null;
      final loggingIn = state.matchedLocation == '/login';
      if (!loggedIn && !loggingIn) return '/login';
      if (loggedIn && loggingIn) return '/';
      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) => ShellScreen(child: child),
        routes: [
          GoRoute(
            path: '/',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: HomeScreen(),
            ),
          ),
          GoRoute(
            path: '/contacts',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: ContactsScreen(),
            ),
            routes: [
              GoRoute(
                path: 'chat/:contactId',
                builder: (context, state) {
                  final contactId = state.pathParameters['contactId']!;
                  final contactName =
                      state.uri.queryParameters['name'] ?? contactId;
                  return ChatScreen(
                    contactId: contactId,
                    contactName: contactName,
                  );
                },
              ),
              GoRoute(
                path: 'send/:contactId',
                builder: (context, state) {
                  final contactId = state.pathParameters['contactId']!;
                  final contactName =
                      state.uri.queryParameters['name'] ?? contactId;
                  return SendMoneyScreen(
                    contactId: contactId,
                    contactName: contactName,
                  );
                },
              ),
            ],
          ),
        ],
      ),
    ],
  );
});
