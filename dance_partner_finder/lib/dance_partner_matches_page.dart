import 'package:flutter/material.dart';

class DancePartnerMatchesWidget extends StatelessWidget {
  DancePartnerMatchesWidget({Key? key}) : super(key: key);
  final List<String> _matchedDancerPartnerNames = ["taylor", "jlo"];

  @override
  Widget build(BuildContext context) {
    return ListView(
        padding: const EdgeInsets.symmetric(vertical: 8.0),
        children: _matchedDancerPartnerNames
            .map((name) => DancePartnerMatchWidget(dancerName: name))
            .toList());
  }
}

class DancePartnerMatchWidget extends StatelessWidget {
  final String dancerName;

  const DancePartnerMatchWidget({super.key, required this.dancerName});

  TextStyle? _getTextStyle(bool newMessageAvailable) {
    if (newMessageAvailable) {
      return const TextStyle(
        color: Colors.redAccent,
        decoration: TextDecoration.underline,
      );
    }
    return const TextStyle(
      color: Colors.black54,
      decoration: TextDecoration.lineThrough,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
          onLongPress: () {},
          onTap: () {},
          leading: CircleAvatar(
          backgroundImage: AssetImage('images/${dancerName}.png')),
      title: Text(dancerName, style: _getTextStyle(true)),
    ),);
  }
}

