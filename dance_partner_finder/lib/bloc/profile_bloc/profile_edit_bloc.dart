import 'package:bloc/bloc.dart';

import 'profile_edit_event.dart';
import 'profile_edit_state.dart';


class ProfileEditBloc extends Bloc<ProfileEditEvent, ProfileEditState> {

  ProfileEditBloc() : super(ProfileEditState.loaded()) {
    on<ProfileLoadedEvent>((event, emit) {
      emit(ProfileEditState.loaded());
    });
    on<ProfileLoadingEvent>((event, emit) {
      emit(ProfileEditState.loading());
    });
  }

  static String profilePicUrl(String dancerEmail){
    return "https://firebasestorage.googleapis.com/v0/b/wonderland-007.appspot.com/o/$dancerEmail.jpeg?alt=media";
  }
}
