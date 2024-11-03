class CreditMessage {
  final String type;
  final int totalCredits;

  CreditMessage({required this.type, required this.totalCredits});

  factory CreditMessage.fromJson(Map<String, dynamic> json) {
    return CreditMessage(
      type: json['type'] as String,
      totalCredits: json['totalCredits'] as int,
    );
  }
}