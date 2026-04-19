enum TransactionType { sent, received }

class Transaction {
  final String id;
  final String fromUserId;
  final String fromUserName;
  final String toUserId;
  final String toUserName;
  final double amount;
  final String note;
  final DateTime timestamp;
  final TransactionType type;

  const Transaction({
    required this.id,
    required this.fromUserId,
    required this.fromUserName,
    required this.toUserId,
    required this.toUserName,
    required this.amount,
    required this.note,
    required this.timestamp,
    required this.type,
  });
}
