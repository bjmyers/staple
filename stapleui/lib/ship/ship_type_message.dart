class ShipTypeMessage {
  final String type;
  final List<String> shipTypes;

  ShipTypeMessage({required this.type, required this.shipTypes});

  factory ShipTypeMessage.fromJson(Map<String, dynamic> json) {
    return ShipTypeMessage(
      type: json['type'] as String,
      shipTypes: List<String>.from(json['shipTypes']),
    );
  }
}