import 'package:equatable/equatable.dart';

abstract class ProfileEditEvent extends Equatable {
  const ProfileEditEvent();
}

class ProfileLoadedEvent extends ProfileEditEvent {
  final String profilePicDownloadableUrl;

  const ProfileLoadedEvent(this.profilePicDownloadableUrl);

  @override
  List<Object?> get props => [this.profilePicDownloadableUrl];
}

class ProfileLoadingEvent extends ProfileEditEvent {
  const ProfileLoadingEvent();

  @override
  List<Object?> get props => [];
}
