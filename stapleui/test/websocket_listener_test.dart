import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_state.dart';
import 'package:stapleui/ship/ship_message.dart';
import 'package:stapleui/ship/ship_state.dart';
import 'package:stapleui/websocket_listener.dart';

import 'utils/mock_websocket_channel.dart';

class MockCreditState extends Mock implements CreditState {}
class MockShipState extends Mock implements ShipState {}

void main() {

  group('Listen function', () {

    late MockCreditState mockCreditState;
    late MockShipState mockShipState;
    late MockWebSocketChannel mockChannel;
    late WebsocketListener instance;

    setUp(() {
      mockCreditState = MockCreditState();
      mockShipState = MockShipState();
      mockChannel = MockWebSocketChannel();
      instance = WebsocketListener(channel: mockChannel);
    });

    testWidgets('updates CreditState when receiving Credit message', (WidgetTester tester) async {
      final creditMessage = jsonEncode({"type": "Credit", "totalCredits": 100});

      mockChannel.addMessage(creditMessage);

      await tester.pumpWidget(
        MultiProvider(
          providers: [
            ChangeNotifierProvider<CreditState>.value(value: mockCreditState),
            ChangeNotifierProvider<ShipState>.value(value: mockShipState),
          ],
          child: Builder(
            builder: (context) {
              instance.listen(context);
              return Container(); 
            },
          ),
        ),
      );

      verify(mockCreditState.updateCredits(100)).called(1);
    });

    testWidgets('updates ShipState when receiving Ships message', (WidgetTester tester) async {
      final shipMessage = jsonEncode({
        "type": "Ships",
        "ships": [
          {"symbol": "SHIP1", "role": "TRADE"},
          {"symbol": "SHIP2", "role": "MINING"}
        ]
      });

      mockChannel.addMessage(shipMessage);

      await tester.pumpWidget(
        MultiProvider(
          providers: [
            ChangeNotifierProvider<CreditState>.value(value: mockCreditState),
            ChangeNotifierProvider<ShipState>.value(value: mockShipState),
          ],
          child: Builder(
            builder: (context) {
              instance.listen(context);
              return Container();
            },
          ),
        ),
      );

      final expectedShipData = [ShipMessageData(symbol: "SHIP1", role: "TRADE"), ShipMessageData(symbol: "SHIP2", role: "MINING")];

      verify(mockShipState.updateShipData(expectedShipData)).called(1);
    });
  });
}