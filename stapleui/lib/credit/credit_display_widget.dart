import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:stapleui/credit/credit_state.dart';

class CreditDisplayWidget extends StatelessWidget {

  const CreditDisplayWidget({super.key});
  
  @override
  Widget build(BuildContext context) {
    final totalCredits = Provider.of<CreditState>(context).currentCredits;
    return Text('$totalCredits');
  }

}