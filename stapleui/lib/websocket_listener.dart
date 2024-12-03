import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_state.dart';
import 'package:stapleui/credit/credit_message.dart';
import 'package:stapleui/ship/event_message.dart';
import 'package:stapleui/ship/event_state.dart';
import 'package:stapleui/ship/purchase_status_message.dart';
import 'package:stapleui/ship/ship_message.dart';
import 'package:stapleui/ship/ship_state.dart';
import 'package:stapleui/ship/ship_type_message.dart';
import 'package:stapleui/ship/ship_type_state.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class WebsocketListener {

  final WebSocketChannel channel;

  const WebsocketListener({required this.channel});

  void listen(BuildContext context) {
    channel.stream.listen((message) {
      final Map<String, dynamic> json = jsonDecode(message);
      final String type = json["type"];
      final CreditState creditState = Provider.of<CreditState>(context, listen: false);
      final ShipState shipState = Provider.of<ShipState>(context, listen: false);
      final ShipEventState shipEventState = Provider.of<ShipEventState>(context, listen: false);
      final ShipPurchaseState shipPurchaseState = Provider.of<ShipPurchaseState>(context, listen: false);

      switch (type) {
        case "Credit":
          final creditMessage = CreditMessage.fromJson(json);
          creditState.updateCredits(creditMessage.totalCredits);
          break;
        case "Ships":
          final shipMessage = ShipMessage.fromJson(json);
          shipState.updateShipData(shipMessage.ships);
          break;
        case "ShipEvent":
          final shipEventMessage = ShipEventMessage.fromJson(json);
          shipEventState.addMessage(shipEventMessage);
          break;
        case "ShipTypes":
          final shipTypeMessage = ShipTypeMessage.fromJson(json);
          shipPurchaseState.updateShipTypes(shipTypeMessage.shipTypes);
          break;
        case "purchaseStatus":
          final purchaseStatusMessage = PurchaseStatusMessage.fromJson(json);
          shipPurchaseState.updateMessage(purchaseStatusMessage.message);
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