import 'package:flutter/material.dart';

class ContactAvatar extends StatelessWidget {
  final String name;
  final double radius;
  const ContactAvatar({super.key, required this.name, this.radius = 22});

  @override
  Widget build(BuildContext context) {
    final initials = name
        .split(' ')
        .take(2)
        .map((w) => w.isNotEmpty ? w[0].toUpperCase() : '')
        .join();
    final hash = name.hashCode;
    final hue = (hash % 360).abs().toDouble();
    return CircleAvatar(
      radius: radius,
      backgroundColor: HSLColor.fromAHSL(1, hue, 0.4, 0.85).toColor(),
      child: Text(
        initials,
        style: TextStyle(
          color: HSLColor.fromAHSL(1, hue, 0.6, 0.35).toColor(),
          fontWeight: FontWeight.w600,
          fontSize: radius * 0.7,
        ),
      ),
    );
  }
}
