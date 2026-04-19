import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:smalloot/main.dart';

void main() {
  testWidgets('App renders login screen', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(child: SmallootApp()),
    );
    await tester.pumpAndSettle();
    expect(find.text('smalloot'), findsOneWidget);
    expect(find.text('Get Started'), findsOneWidget);
  });
}
