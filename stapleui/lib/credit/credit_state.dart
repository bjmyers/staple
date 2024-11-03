import 'package:flutter/foundation.dart';

class CreditState with ChangeNotifier {

  int currentCredits = 0;

  void updateCredits(int newCredits) {
    currentCredits = newCredits;
    notifyListeners();
  }

}