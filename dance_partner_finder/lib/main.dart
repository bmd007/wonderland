import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dance Partner Finder',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const DancePartnerSelectWidget(),
      // BlocProvider(
      //   create: (_) => DancePartnerBloc(),
      //   child: DancePartnerSelectWidget(),
      // ),
    );
  }
}

class DancePartnerSelectWidget extends StatelessWidget {
  const DancePartnerSelectWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Stack(
     fit: StackFit.expand,
      children: [
        Image.asset('images/tom.jpg', fit: BoxFit.fitHeight),
        Column(
          verticalDirection: VerticalDirection.down,
          children: [
            Text(
              "Tom",
              textAlign: TextAlign.center,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.stop, textDirection: TextDirection.ltr, color: Colors.red),
                const Icon(Icons.one_k, textDirection: TextDirection.rtl, color: Colors.green),
              ],
            )
          ],
        )
      ],
    );
  }
}
