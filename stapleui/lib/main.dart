import 'package:flutter/material.dart';
import 'package:stapleui/credit_display_widget.dart';

void main() {
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final titleStyle = theme.textTheme.displayMedium!.copyWith(
      color: theme.colorScheme.primary,
    );

    return MaterialApp(
      home: Scaffold(
        body: Column(
          children: [
            Text('Space Traders Automated PLanning Engine', style: titleStyle),
            const Row(
              children: [
                Text('Total Credits: '),
                CreditDisplayWidget(),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
