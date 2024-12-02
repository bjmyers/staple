import 'package:flutter/foundation.dart';
import 'package:stapleui/bounded_queue.dart';
import 'package:stapleui/ship/event_message.dart';

class ShipEventState with ChangeNotifier {

  final int maxMessagesToDisplay = 10;
  final Map<String, BoundedQueue<String>> _messagesByShip = {};
  // Start with an empty ID, no ship will match this ID so we won't display anything
  String _selectedShip = "";

  void addMessage(ShipEventMessage message) {
    _messagesByShip.putIfAbsent(message.shipId, () => BoundedQueue<String>(maxMessagesToDisplay));
    _messagesByShip[message.shipId]!.add(message.message);
    notifyListeners();
  }

  void selectShip(String shipId) {
    _selectedShip = shipId;
    notifyListeners();
  }

  List<String> getMessages() => _messagesByShip[_selectedShip]?.items ?? [];
}