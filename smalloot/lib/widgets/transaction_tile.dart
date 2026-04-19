import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/transaction.dart';

class TransactionTile extends StatelessWidget {
  final Transaction transaction;
  const TransactionTile({super.key, required this.transaction});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isSent = transaction.type == TransactionType.sent;
    final peerName = isSent ? transaction.toUserName : transaction.fromUserName;
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 2),
      leading: CircleAvatar(
        backgroundColor: isSent
            ? Colors.red.shade50
            : Colors.green.shade50,
        child: Icon(
          isSent ? Icons.arrow_upward : Icons.arrow_downward,
          color: isSent ? Colors.red.shade700 : Colors.green.shade700,
          size: 20,
        ),
      ),
      title: Text(
        peerName,
        style: const TextStyle(fontWeight: FontWeight.w500),
      ),
      subtitle: Text(
        '${transaction.note}  ·  ${_formatDate(transaction.timestamp)}',
        style: TextStyle(
          fontSize: 12,
          color: theme.colorScheme.onSurfaceVariant,
        ),
      ),
      trailing: Text(
        '${isSent ? '-' : '+'}\$${transaction.amount.toStringAsFixed(2)}',
        style: TextStyle(
          fontWeight: FontWeight.w600,
          fontSize: 15,
          color: isSent ? Colors.red.shade700 : Colors.green.shade700,
        ),
      ),
    );
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final diff = now.difference(date);
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    if (diff.inDays < 7) return '${diff.inDays}d ago';
    return DateFormat('MMM d').format(date);
  }
}
