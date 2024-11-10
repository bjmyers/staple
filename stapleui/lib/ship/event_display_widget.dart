import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/ship/event_state.dart';

class ShipEventDisplayWidget extends StatelessWidget {

  const ShipEventDisplayWidget({super.key});
  
  @override
  Widget build(BuildContext context) {
    final messages = Provider.of<ShipEventState>(context).getMessages();
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: messages.map((message) {
          return Container(
            margin: const EdgeInsets.symmetric(vertical: 4.0),
            padding: const EdgeInsets.all(8.0),
            color: Colors.grey[200],
            child: Text(
              message,
              style: const TextStyle(fontSize: 16),
            ),
          );
        }).toList(),
      ),
    );
  }

}