import 'package:intl/intl.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_state.dart';

class CreditDisplayWidget extends StatelessWidget {

  const CreditDisplayWidget({super.key});
  
  @override
  Widget build(BuildContext context) {
    NumberFormat formatter = NumberFormat.decimalPatternDigits(
      locale: 'en_us',
    );
    final totalCredits = Provider.of<CreditState>(context).currentCredits;
    return Text(formatter.format(totalCredits),
      style: const TextStyle(fontSize: 26),
    );
  }

}