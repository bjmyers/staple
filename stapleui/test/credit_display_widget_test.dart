import 'package:stapleui/credit_display_widget.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import 'package:flutter/material.dart';
import 'utils/mock_websocket_channel.dart';

void main() {

  group('Credit Display Widget Tests', () {
  
    late MockWebSocketChannel mockChannel;

    setUp(() {
      mockChannel = MockWebSocketChannel();
    });

    tearDown(() {
      mockChannel.dispose();
    });

    testWidgets('Display Widget receives data over websocket', (WidgetTester tester) async {

      // Build the widget with the mock WebSocket channel
      await tester.pumpWidget(
        MaterialApp(
          home: CreditDisplayWidget(channel: mockChannel),
        ),
      );

      mockChannel.addMessage('5');
      await tester.pumpAndSettle();

      expect(find.text('5'), findsOneWidget);

      mockChannel.addMessage('10');
      await tester.pumpAndSettle();

      expect(find.text('10'), findsOneWidget);
    });

  });
}