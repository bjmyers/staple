import 'package:flutter_test/flutter_test.dart';
import 'package:stapleui/credit/credit_message.dart';

void main() {

  test('deserialization', () {
    final Map<String, dynamic> json = {"type": "Credit", "totalCredits": 5};

    final CreditMessage message = CreditMessage.fromJson(json);

    expect(message.type, "Credit");
    expect(message.totalCredits, 5);
  });
}