import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/contacts_provider.dart';
import '../widgets/contact_avatar.dart';

class ContactsScreen extends ConsumerWidget {
  const ContactsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final contacts = ref.watch(contactsProvider);
    final theme = Theme.of(context);

    return SafeArea(
      child: CustomScrollView(
        slivers: [
          const SliverAppBar(
            floating: true,
            title: Text('Contacts'),
          ),
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) {
                final contact = contacts[index];
                return ListTile(
                  contentPadding:
                      const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                  leading: ContactAvatar(name: contact.name),
                  title: Text(
                    contact.name,
                    style: const TextStyle(fontWeight: FontWeight.w500),
                  ),
                  subtitle: Text(
                    contact.email,
                    style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
                  ),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      IconButton(
                        icon: Icon(
                          Icons.chat_bubble_outline,
                          color: theme.colorScheme.primary,
                        ),
                        onPressed: () => context.go(
                          '/contacts/chat/${contact.id}?name=${Uri.encodeComponent(contact.name)}',
                        ),
                      ),
                      IconButton(
                        icon: Icon(
                          Icons.send,
                          color: theme.colorScheme.tertiary,
                        ),
                        onPressed: () => context.go(
                          '/contacts/send/${contact.id}?name=${Uri.encodeComponent(contact.name)}',
                        ),
                      ),
                    ],
                  ),
                );
              },
              childCount: contacts.length,
            ),
          ),
        ],
      ),
    );
  }
}
