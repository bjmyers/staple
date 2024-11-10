class ShipEventMessage {
  final String type;
  final String shipId;
  final String message;

  ShipEventMessage({required this.type, required this.shipId, required this.message});

  factory ShipEventMessage.fromJson(Map<String, dynamic> json) {
    return ShipEventMessage(
      type: json['type'] as String,
      shipId: json['shipId'] as String,
      message: json['message'] as String,
    );
  }

    @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    if (other is! ShipEventMessage) return false;
    return other.type == type && other.shipId == shipId && other.message == message;
  }

  @override
  int get hashCode => type.hashCode ^ shipId.hashCode ^ message.hashCode;
}