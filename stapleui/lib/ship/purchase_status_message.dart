class PurchaseStatusMessage {
  final String type;
  final String message;

  PurchaseStatusMessage({required this.type, required this.message});

  factory PurchaseStatusMessage.fromJson(Map<String, dynamic> json) {
    return PurchaseStatusMessage(
      type: json['type'] as String,
      message: json['message'] as String,
    );
  }
}