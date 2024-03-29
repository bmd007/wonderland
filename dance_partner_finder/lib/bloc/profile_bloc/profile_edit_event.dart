import 'package:equatable/equatable.dart';

abstract class ProfileEditEvent extends Equatable {
  const ProfileEditEvent();
}

class ProfileLoadedEvent extends ProfileEditEvent {

  const ProfileLoadedEvent();

  @override
  List<Object?> get props => [];
}

class ProfileLoadingEvent extends ProfileEditEvent {
  const ProfileLoadingEvent();

  @override
  List<Object?> get props => [];
}
