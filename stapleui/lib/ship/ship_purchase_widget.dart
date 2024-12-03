import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:provider/provider.dart';
import 'package:stapleui/ship/ship_type_state.dart';

class ShipPurchaseWidget extends StatefulWidget {

  const ShipPurchaseWidget({super.key});

  @override
  State<ShipPurchaseWidget> createState() => ShipPurchaseWidgetState();
}

class ShipPurchaseWidgetState extends State<ShipPurchaseWidget> {
  String? dropdownValue;
  
  @override
  Widget build(BuildContext context) {
    final shipPurchaseState = Provider.of<ShipPurchaseState>(context);
    return Row(
      children: [
        DropdownButton<String>(
          value: dropdownValue,
          icon: const Icon(Icons.arrow_downward),
          elevation: 16,
          style: const TextStyle(color: Colors.deepPurple),
          onChanged: (String? value) {
            setState(() {
              dropdownValue = value!;
            });
          },
          items: shipPurchaseState.shipTypes.map<DropdownMenuItem<String>>((String value) {
            return DropdownMenuItem<String>(
              value: value,
              child: Text(value),
            );
          }).toList(),
        ),
        ElevatedButton(
          onPressed: () {
            purchaseShip(dropdownValue);
            shipPurchaseState.updateMessage("Waiting on Ship to Start Purchase Job");
          },
          child: const Text("Purchase Ship"),
        ),
        Text(shipPurchaseState.message),
      ],
    );
  }

  Future<void> purchaseShip(String? shipType) async {
    if (shipType == null) {
      // No ship type has yet been selected
      return;
    }
    final url = Uri.parse('http://localhost:8080/purchase');

    await http.post(
      url,
      headers: {
        'Content-Type': 'text/plain',
      },
      body: dropdownValue,
    );
  }

}