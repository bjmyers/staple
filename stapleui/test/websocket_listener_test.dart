

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_state.dart';
import 'package:stapleui/websocket_listener.dart';

import 'utils/mock_websocket_channel.dart';

void main() {

  group('Websocket Listener Tests', () {
  
    late MockWebSocketChannel mockChannel;

    setUp(() {
      mockChannel = MockWebSocketChannel();
    });

    tearDown(() {
      mockChannel.dispose();
    });

    testWidgets('Credit Message', (WidgetTester tester) async {

      final creditState = CreditState();

      final websocketListener = WebsocketListener(channel: mockChannel);

      mockChannel.addMessage('{"type": "Credit", "totalCredits": 100}');

      await tester.pumpWidget(
        MaterialApp(
          home: ChangeNotifierProvider<CreditState>.value(
            value: creditState,
            child: Builder(
              builder: (context) {
                websocketListener.listen(context);
                return Container();
              },
            ),
          ),
        ),
      );


      expect(creditState.currentCredits, 100);
    });
  });
}