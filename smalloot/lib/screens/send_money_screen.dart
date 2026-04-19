import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/auth_provider.dart';
import '../providers/transactions_provider.dart';
import '../widgets/contact_avatar.dart';

class SendMoneyScreen extends ConsumerStatefulWidget {
  final String contactId;
  final String contactName;

  const SendMoneyScreen({
    super.key,
    required this.contactId,
    required this.contactName,
  });

  @override
  ConsumerState<SendMoneyScreen> createState() => _SendMoneyScreenState();
}

class _SendMoneyScreenState extends ConsumerState<SendMoneyScreen> {
  final _amountController = TextEditingController();
  final _noteController = TextEditingController();
  bool _sending = false;

  @override
  void dispose() {
    _amountController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final amount = double.tryParse(_amountController.text);
    if (amount == null || amount <= 0) return;
    final user = ref.read(currentUserProvider);
    if (user == null || amount > user.balance) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Insufficient balance')),
      );
      return;
    }

    setState(() => _sending = true);
    await Future.delayed(const Duration(milliseconds: 800));

    ref.read(transactionsProvider.notifier).sendMoney(
          toUserId: widget.contactId,
          toUserName: widget.contactName,
          amount: amount,
          note: _noteController.text.trim().isEmpty
              ? 'Transfer'
              : _noteController.text.trim(),
        );

    if (mounted) {
      setState(() => _sending = false);
      _showSuccessSheet(amount);
    }
  }

  void _showSuccessSheet(double amount) {
    showModalBottomSheet(
      context: context,
      isDismissible: false,
      builder: (ctx) {
        final theme = Theme.of(ctx);
        return Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 72,
                height: 72,
                decoration: BoxDecoration(
                  color: Colors.green.shade50,
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.check, size: 40, color: Colors.green.shade700),
              ),
              const SizedBox(height: 20),
              Text(
                'Sent \$${amount.toStringAsFixed(2)}',
                style: theme.textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'to ${widget.contactName}',
                style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
              ),
              const SizedBox(height: 28),
              FilledButton(
                onPressed: () {
                  Navigator.pop(ctx);
                  context.go('/');
                },
                child: const Text('Done'),
              ),
              const SizedBox(height: 16),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(currentUserProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Send Money')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            const SizedBox(height: 12),
            ContactAvatar(name: widget.contactName, radius: 36),
            const SizedBox(height: 12),
            Text(
              widget.contactName,
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 36),
            TextField(
              controller: _amountController,
              keyboardType:
                  const TextInputType.numberWithOptions(decimal: true),
              inputFormatters: [
                FilteringTextInputFormatter.allow(RegExp(r'^\d*\.?\d{0,2}')),
              ],
              style: theme.textTheme.displaySmall?.copyWith(
                fontWeight: FontWeight.w700,
              ),
              textAlign: TextAlign.center,
              decoration: InputDecoration(
                hintText: '0.00',
                prefixText: '\$ ',
                prefixStyle: theme.textTheme.displaySmall?.copyWith(
                  fontWeight: FontWeight.w700,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                border: InputBorder.none,
                filled: false,
              ),
            ),
            if (user != null)
              Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Text(
                  'Balance: \$${user.balance.toStringAsFixed(2)}',
                  style: TextStyle(
                    color: theme.colorScheme.onSurfaceVariant,
                    fontSize: 14,
                  ),
                ),
              ),
            const SizedBox(height: 28),
            TextField(
              controller: _noteController,
              decoration: const InputDecoration(
                hintText: 'Add a note...',
                prefixIcon: Icon(Icons.note_outlined),
              ),
              textInputAction: TextInputAction.done,
            ),
            const SizedBox(height: 36),
            FilledButton(
              onPressed: _sending ? null : _send,
              child: _sending
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: Colors.white,
                      ),
                    )
                  : const Text('Send'),
            ),
          ],
        ),
      ),
    );
  }
}
