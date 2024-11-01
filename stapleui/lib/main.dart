import 'package:flutter/material.dart';
import 'package:stapleui/credit_display_widget.dart';
import 'package:web_socket_channel/io.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

void main() {
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final titleStyle = theme.textTheme.displayMedium!.copyWith(
      color: theme.colorScheme.primary,
    );
    final WebSocketChannel channel = IOWebSocketChannel.connect('ws://localhost:8080/credit-update');

    return MaterialApp(
      home: Scaffold(
        body: Column(
          children: [
            Text('Space Traders Automated PLanning Engine', style: titleStyle),
            Row(
              children: [
                const Text('Total Credits: '),
                CreditDisplayWidget(channel: channel),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
