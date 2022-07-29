import 'package:bloc/bloc.dart';
import 'package:firebase_storage/firebase_storage.dart';

import 'profile_edit_event.dart';
import 'profile_edit_state.dart';


class ProfileEditBloc extends Bloc<ProfileEditEvent, ProfileEditState> {

  ProfileEditBloc() : super(ProfileEditState.loading()) {
    on<ProfileLoadedEvent>((event, emit) {
      emit(ProfileEditState.loaded(event.profilePicDownloadableUrl));
    });
    on<ProfileLoadingEvent>((event, emit) {
      emit(ProfileEditState.loading());
    });
  }
}
