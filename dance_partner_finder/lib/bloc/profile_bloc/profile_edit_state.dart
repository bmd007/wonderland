import 'package:equatable/equatable.dart';

class ProfileEditState extends Equatable {
  final bool isLoading;
  final String profilePicUrl;

  const ProfileEditState(this.isLoading, this.profilePicUrl);

  @override
  List<Object?> get props => [isLoading];

  static ProfileEditState loading() {
    return const ProfileEditState(true, "");
  }

  static ProfileEditState loaded(String profilePicUrl) {
    return ProfileEditState(false, profilePicUrl);
  }
}
