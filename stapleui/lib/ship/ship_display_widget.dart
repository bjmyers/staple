
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/ship/event_state.dart';
import 'package:stapleui/ship/ship_state.dart';

class ShipDisplayWidget extends StatelessWidget {

  const ShipDisplayWidget({super.key});

  @override
  Widget build(BuildContext context) {
    final shipState = Provider.of<ShipState>(context);
    final shipEventState = Provider.of<ShipEventState>(context);
    return ListView.builder(
      scrollDirection: Axis.vertical,
      shrinkWrap: true,
      itemCount: shipState.shipData.length,
      itemBuilder: (content, index) {
        final shipData = shipState.shipData[index];
        return Card(
          child: ElevatedButton(
            onPressed: () => shipEventState.selectShip(shipData.symbol),
            child: Text('${shipData.symbol} : ${shipData.role}')
          )
        );
      },
    );
  }
  
}