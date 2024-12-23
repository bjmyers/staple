import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_display_widget.dart';
import 'package:stapleui/credit/credit_state.dart';
import 'package:stapleui/ship/event_display_widget.dart';
import 'package:stapleui/ship/event_state.dart';
import 'package:stapleui/ship/ship_display_widget.dart';
import 'package:stapleui/ship/ship_state.dart';
import 'package:stapleui/ship/ship_purchase_widget.dart';
import 'package:stapleui/ship/ship_type_state.dart';
import 'package:stapleui/websocket_listener.dart';
import 'package:web_socket_channel/io.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

void main() {
  final WebSocketChannel channel = IOWebSocketChannel.connect('ws://localhost:8080/staple-update');
  runApp(
    MultiProvider(providers: [
      ChangeNotifierProvider(create: (_) => CreditState()),
      ChangeNotifierProvider(create: (_) => ShipState()),
      ChangeNotifierProvider(create: (_) => ShipEventState()),
      ChangeNotifierProvider(create: (_) => ShipPurchaseState()),
      Provider(create: (_) => WebsocketListener(channel: channel)),
    ],
    child: const MainApp()),
  );
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final titleStyle = theme.textTheme.displayMedium!.copyWith(
      color: theme.colorScheme.primary,
    );

    final websocketListener = Provider.of<WebsocketListener>(context, listen: false);
    websocketListener.listen(context);

    return MaterialApp(
      home: Scaffold(
        body: Column(
          children: [
            Text('Space Traders Automated PLanning Engine', style: titleStyle),
            const Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text('Total Credits: ',
                  style: TextStyle(fontSize: 26),
                ),
                CreditDisplayWidget(),
              ],
            ),
            const ShipPurchaseWidget(),
            const Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: ShipDisplayWidget()
                ),
                Expanded(
                  child: ShipEventDisplayWidget()
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
