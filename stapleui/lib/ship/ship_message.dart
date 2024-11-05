class ShipMessage {
  final String type;
  final List<ShipMessageData> ships;

  ShipMessage({required this.type, required this.ships});

  factory ShipMessage.fromJson(Map<String, dynamic> json) {
    return ShipMessage(
      type: json['type'] as String,
      ships: (json['ships'] as List<dynamic>)
        .map((item) => ShipMessageData.fromJson(item as Map<String, dynamic>))
        .toList(),
    );
  }
}

class ShipMessageData {
  final String symbol;
  final String role;

  ShipMessageData({required this.symbol, required this.role});

  factory ShipMessageData.fromJson(Map<String, dynamic> json) {
    return ShipMessageData(
      symbol: json['symbol'] as String,
      role: json['role'] as String,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    if (other is! ShipMessageData) return false;
    return other.symbol == symbol && other.role == role;
  }

  @override
  int get hashCode => symbol.hashCode ^ role.hashCode;
}