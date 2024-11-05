
import 'package:flutter/foundation.dart';
import 'package:stapleui/ship/ship_message.dart';

class ShipState with ChangeNotifier {

  List<ShipMessageData> shipData = [];

  void updateShipData(List<ShipMessageData> newShipData) {
    shipData = newShipData;
    notifyListeners();
  }

}