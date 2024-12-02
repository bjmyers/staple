import 'package:flutter/foundation.dart';

class ShipTypeState with ChangeNotifier {

  List<String> shipTypes = [];

  void updateShipTypes(List<String> newShipTypes) {
    shipTypes = newShipTypes;
    notifyListeners();
  }

}