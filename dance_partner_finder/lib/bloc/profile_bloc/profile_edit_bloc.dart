import 'package:bloc/bloc.dart';

import 'profile_edit_event.dart';
import 'profile_edit_state.dart';


class ProfileEditBloc extends Bloc<ProfileEditEvent, ProfileEditState> {
  ProfileEditBloc() : super(ProfileEditState.loading()) {
    on<ProfileEditEvent>((event, emit) {
      // TODO: implement event handler
    });
  }
}
