import 'package:file_picker/file_picker.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/login/login_cubit.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';
import 'bloc/profile_bloc/profile_edit_event.dart';
import 'bloc/profile_bloc/profile_edit_state.dart';

class DanceProfileEditWidget extends StatelessWidget {

  DanceProfileEditWidget({super.key});

  final storage = FirebaseStorage.instanceFor(bucket: "gs://wonderland-007.appspot.com");

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => ProfileEditBloc(),
      child: BlocBuilder<ProfileEditBloc, ProfileEditState>(
        builder: (context, state) {
          return Scaffold(
              appBar: AppBar(centerTitle: true, title: const Text("Edit your profile")),
              body: body(context, loginCubit));
        },
      ),
    );
  }

  Widget body(BuildContext context, LoginCubit loginCubit) {
    var profileEditBloc = context.watch<ProfileEditBloc>();

    return !profileEditBloc.state.isLoading
        ? Column(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Stack(children: [
                Image.network(ProfileEditBloc.profilePicUrl(loginCubit.state.email), fit: BoxFit.fitHeight),
                IconButton(
                    onPressed: () => uploadProfileImage(loginCubit.state.email, profileEditBloc),
                    icon: Image.asset('assets/images/edit_profile.png')),
              ]),
              Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const Text(
                    "Name: ",
                    style: TextStyle(
                      color: Colors.black,
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                  Text(loginCubit.state.email,
                      style: const TextStyle(
                        color: Colors.black54,
                        fontWeight: FontWeight.bold,
                        fontSize: 15,
                      )),
                ],
              ),
            ],
          )
        : Image.asset('assets/images/wait.gif');
  }

  void uploadProfileImage(String dancerEmail, ProfileEditBloc profileEditBloc) {
    profileEditBloc.add(const ProfileLoadingEvent());

    Future<FilePickerResult?> filePickerResults = FilePicker.platform
        .pickFiles(type: FileType.image, allowMultiple: false, lockParentWindow: true, withData: true);

    filePickerResults
        .asStream()
        .where((event) => event != null)
        .where((event) => event!.files.isNotEmpty)
        .map((event) => event?.files.first.bytes)
        .map((event) => storage.ref().child("$dancerEmail.jpeg").putData(event!))
        .forEach((event) => event.snapshotEvents.listen((TaskSnapshot taskSnapshot) {
              switch (taskSnapshot.state) {
                case TaskState.running:
                  final progress = 100.0 * (taskSnapshot.bytesTransferred / taskSnapshot.totalBytes);
                  print("Upload is $progress% complete.");
                  profileEditBloc.add(const ProfileLoadingEvent());
                  break;
                case TaskState.paused:
                  print("Upload is paused.");
                  profileEditBloc.add(const ProfileLoadedEvent());
                  break;
                case TaskState.canceled:
                  print("Upload was canceled");
                  profileEditBloc.add(const ProfileLoadedEvent());
                  break;
                case TaskState.error:
                  print("Upload has failed");
                  profileEditBloc.add(const ProfileLoadedEvent());
                  break;
                case TaskState.success:
                  print("Upload done");
                  imageCache.clear();
                  profileEditBloc.add(const ProfileLoadedEvent());
                  break;
              }
            }));
  }
}
