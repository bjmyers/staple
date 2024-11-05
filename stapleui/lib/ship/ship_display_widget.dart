
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/ship/ship_state.dart';

class ShipDisplayWidget extends StatelessWidget {

  const ShipDisplayWidget({super.key});

  @override
  Widget build(BuildContext context) {
    final shipState = Provider.of<ShipState>(context);
    return ListView.builder(
      scrollDirection: Axis.vertical,
      shrinkWrap: true,
      itemCount: shipState.shipData.length,
      itemBuilder: (content, index) {
        final shipData = shipState.shipData[index];
        return Card(
          child: ListTile(
            title: Text(shipData.symbol),
            subtitle: Text(shipData.role),
          )
        );
      },
    );
  }
  
}