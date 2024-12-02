import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/ship/ship_type_state.dart';

class ShipTypeSelectionWidget extends StatefulWidget {

  const ShipTypeSelectionWidget({super.key});

  @override
  State<ShipTypeSelectionWidget> createState() => ShipTypeSelectionWidgetState();
}

class ShipTypeSelectionWidgetState extends State<ShipTypeSelectionWidget> {
  String? dropdownValue;
  
  @override
  Widget build(BuildContext context) {
    final shipTypes = Provider.of<ShipTypeState>(context).shipTypes;
    return DropdownButton<String>(
      value: dropdownValue,
      icon: const Icon(Icons.arrow_downward),
      elevation: 16,
      style: const TextStyle(color: Colors.deepPurple),
      onChanged: (String? value) {
        setState(() {
          dropdownValue = value!;
        });
      },
      items: shipTypes.map<DropdownMenuItem<String>>((String value) {
        return DropdownMenuItem<String>(
          value: value,
          child: Text(value),
        );
      }).toList(),
    );
  }

}