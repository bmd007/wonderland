import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user.dart';

final contactsProvider = Provider<List<User>>((ref) {
  return const [
    User(
      id: 'alice',
      name: 'Alice Johnson',
      email: 'alice@example.com',
      balance: 3200.50,
    ),
    User(
      id: 'bob',
      name: 'Bob Smith',
      email: 'bob@example.com',
      balance: 1580.75,
    ),
    User(
      id: 'carol',
      name: 'Carol Williams',
      email: 'carol@example.com',
      balance: 4100.00,
    ),
    User(
      id: 'dave',
      name: 'Dave Brown',
      email: 'dave@example.com',
      balance: 920.30,
    ),
    User(
      id: 'eve',
      name: 'Eve Davis',
      email: 'eve@example.com',
      balance: 5500.00,
    ),
  ];
});
