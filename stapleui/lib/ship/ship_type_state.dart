import 'package:flutter/foundation.dart';

class ShipPurchaseState with ChangeNotifier {

  List<String> shipTypes = [];
  String message = "";

  void updateShipTypes(List<String> newShipTypes) {
    shipTypes = newShipTypes;
    notifyListeners();
  }

  void updateMessage(String newMessage) {
    message = newMessage;
    notifyListeners();
  }

}