import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';
import '../models/transaction.dart';
import 'auth_provider.dart';

const _uuid = Uuid();

final transactionsProvider =
    StateNotifierProvider<TransactionsNotifier, List<Transaction>>((ref) {
  return TransactionsNotifier(ref);
});

class TransactionsNotifier extends StateNotifier<List<Transaction>> {
  final Ref _ref;

  TransactionsNotifier(this._ref)
      : super([
          Transaction(
            id: _uuid.v4(),
            fromUserId: 'alice',
            fromUserName: 'Alice Johnson',
            toUserId: 'me',
            toUserName: 'You',
            amount: 125.00,
            note: 'Dinner split',
            timestamp: DateTime.now().subtract(const Duration(hours: 2)),
            type: TransactionType.received,
          ),
          Transaction(
            id: _uuid.v4(),
            fromUserId: 'me',
            fromUserName: 'You',
            toUserId: 'bob',
            toUserName: 'Bob Smith',
            amount: 50.00,
            note: 'Coffee beans',
            timestamp: DateTime.now().subtract(const Duration(days: 1)),
            type: TransactionType.sent,
          ),
          Transaction(
            id: _uuid.v4(),
            fromUserId: 'carol',
            fromUserName: 'Carol Williams',
            toUserId: 'me',
            toUserName: 'You',
            amount: 320.00,
            note: 'Rent share',
            timestamp: DateTime.now().subtract(const Duration(days: 2)),
            type: TransactionType.received,
          ),
          Transaction(
            id: _uuid.v4(),
            fromUserId: 'me',
            fromUserName: 'You',
            toUserId: 'dave',
            toUserName: 'Dave Brown',
            amount: 15.99,
            note: 'Lunch',
            timestamp: DateTime.now().subtract(const Duration(days: 3)),
            type: TransactionType.sent,
          ),
        ]);

  void sendMoney({
    required String toUserId,
    required String toUserName,
    required double amount,
    required String note,
  }) {
    final user = _ref.read(currentUserProvider);
    if (user == null) return;

    final transaction = Transaction(
      id: _uuid.v4(),
      fromUserId: user.id,
      fromUserName: user.name,
      toUserId: toUserId,
      toUserName: toUserName,
      amount: amount,
      note: note,
      timestamp: DateTime.now(),
      type: TransactionType.sent,
    );

    state = [transaction, ...state];
    _ref.read(currentUserProvider.notifier).updateBalance(user.balance - amount);
  }
}
