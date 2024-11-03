import 'package:flutter_test/flutter_test.dart';
import 'package:stapleui/credit/credit_state.dart';

void main() {

  test('Current Credits can be Updated', () {

    final CreditState state = CreditState();
    state.updateCredits(10);

    expect(state.currentCredits, 10);
  });
}