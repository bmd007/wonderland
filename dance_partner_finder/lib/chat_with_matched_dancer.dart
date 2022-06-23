import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/dance_partner_finder/dance_partner_finder_bloc.dart';

class ChatWithMatchedDancerWidget extends StatelessWidget {
  final String matchedDancerName;

  const ChatWithMatchedDancerWidget({super.key, required this.matchedDancerName});

  @override
  Widget build(BuildContext context) {
    var dancerBloc = context.watch<DancePartnerFinderBloc>();//todo create a user account/authentication bloc for holding this dance name
    return Card(child: Text(dancerBloc.state.thisDancerName),);
  }
}
