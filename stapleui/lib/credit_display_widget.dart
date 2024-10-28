import 'package:flutter/material.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:web_socket_channel/io.dart';

class CreditDisplayWidget extends StatefulWidget {

  const CreditDisplayWidget({super.key});

  @override
  State<StatefulWidget> createState() => CreditDisplay();

}

class CreditDisplay extends State<CreditDisplayWidget> {

  final WebSocketChannel channel = IOWebSocketChannel.connect('ws://localhost:8080/credit-update');
  int currentCredits = 0;

  @override
  void initState() {
    super.initState();
    // Listen for messages from the WebSocket server
    channel.stream.listen((message) {
      setState(() {
        currentCredits = int.tryParse(message) ?? currentCredits;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Text('$currentCredits');
  }

  @override
  void dispose() {
    channel.sink.close();
    super.dispose();
  }
  
}