import 'package:dance_partner_finder/dance_partner_select_widget.dart';
import 'package:dance_partner_finder/dancer_profile_edit_widget.dart';
import 'package:flutter/material.dart';

class SubjectTableWidget extends StatelessWidget {
  const SubjectTableWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: GridView.count(
          crossAxisCount: (MediaQuery.of(context).size.width ~/ 250).toInt(),
          childAspectRatio: 1,
          crossAxisSpacing: 0.0,
          mainAxisSpacing: 5,
          shrinkWrap: true,
          padding: const EdgeInsets.symmetric(horizontal: 30),
          children: [
            Card(
              child: GestureDetector(
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (context) => DanceProfileEditWidget())),
                child: Container(
                  height: 300,
                  decoration: BoxDecoration(borderRadius: BorderRadius.circular(20)),
                  margin: const EdgeInsets.all(5),
                  padding: const EdgeInsets.all(5),
                  child: Stack(
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Expanded(
                            child: Image.asset('images/edit_profile.png'),
                          ),
                          Row(
                            children: const [
                              Text(
                                'Edit your profile',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                ),
                              ),
                            ],
                          )
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
            Card(
              child: GestureDetector(
                onTap: () =>
                    Navigator.push(context, MaterialPageRoute(builder: (context) => DancePartnerSelectWidget())),
                child: Container(
                  height: 300,
                  decoration: BoxDecoration(borderRadius: BorderRadius.circular(20)),
                  margin: const EdgeInsets.all(5),
                  padding: const EdgeInsets.all(5),
                  child: Stack(
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Expanded(
                            child: Image.asset('images/dancer2.png'),
                          ),
                          Row(
                            children: const [
                              Text(
                                'Find a dance partner',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                ),
                              ),
                            ],
                          )
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            )
          ],
        ),
      ),
    );
  }
}
