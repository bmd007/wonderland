import 'package:equatable/equatable.dart';

class ProfileEditState extends Equatable {
  final bool isLoading;

  const ProfileEditState(this.isLoading);

  @override
  List<Object?> get props => [isLoading];

  static ProfileEditState loading() {
    return const ProfileEditState(true);
  }

  static ProfileEditState loaded() {
    return const ProfileEditState(false);
  }
}
