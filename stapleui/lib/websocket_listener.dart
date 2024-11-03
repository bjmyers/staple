import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_state.dart';
import 'package:stapleui/credit/credit_message.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class WebsocketListener {

  final WebSocketChannel channel;

  const WebsocketListener({required this.channel});

  void listen(BuildContext context) {
    channel.stream.listen((message) {
      final Map<String, dynamic> json = jsonDecode(message);
      final String type = json["type"];
      final CreditState creditState = Provider.of<CreditState>(context, listen: false);

      switch (type) {
        case "Credit":
          final creditMessage = CreditMessage.fromJson(json);
          creditState.updateCredits(creditMessage.totalCredits);
          break;
        default:
          break;
      }
    });
  }

  void dispose() {
    channel.sink.close();
  }

}