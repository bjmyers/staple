import 'package:flutter/material.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class CreditDisplayWidget extends StatefulWidget {

  final WebSocketChannel channel;

  const CreditDisplayWidget({super.key, required this.channel});

  @override
  State<StatefulWidget> createState() => CreditDisplay();

}

class CreditDisplay extends State<CreditDisplayWidget> {

  late WebSocketChannel channel;
  int currentCredits = 0;

  @override
  void initState() {
    super.initState();
    channel = widget.channel;
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