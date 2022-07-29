import 'package:file_picker/file_picker.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/login/login_cubit.dart';
import 'bloc/profile_bloc/profile_edit_bloc.dart';
import 'bloc/profile_bloc/profile_edit_event.dart';
import 'bloc/profile_bloc/profile_edit_state.dart';
import 'dance_partner_select_widget.dart';

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
              appBar: AppBar(centerTitle: true, title: const Text("Change your profile pic"), actions: [
                IconButton(
                  onPressed: () =>
                      Navigator.push(context, MaterialPageRoute(builder: (context) => DancePartnerSelectWidget())),
                  icon: Image.asset('images/match.png', height: 40, width: 40),
                )
              ]),
              body: body(context, loginCubit));
        },
      ),
    );
  }

  Widget body(BuildContext context, LoginCubit loginCubit) {
    var profileEditBloc = context.watch<ProfileEditBloc>();

    if (profileEditBloc.state.profilePicUrl.isEmpty){
      storage.ref().child("${loginCubit.state.email}.jpeg")
          .getDownloadURL()
          .asStream()
          .where((event) => event.isNotEmpty)
          .forEach((element) => profileEditBloc.add(ProfileLoadedEvent(element)));
    }
    return loginCubit.state.email.isNotEmpty
        ? Stack(
            fit: StackFit.expand,
            children: [
              Image.network(profileEditBloc.state.profilePicUrl, fit: BoxFit.fitHeight),
              Column(
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    loginCubit.state.email,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 50,
                    ),
                  ),
                  Text(loginCubit.state.name,
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 50,
                      )),
                  TextButton(
                      onPressed: () => uploadProfileImage(loginCubit.state.email, profileEditBloc),
                      child: const Text("change it")),
                ],
              )
            ],
          )
        : Image.asset('images/wait.png');
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
          break;
        case TaskState.paused:
          print("Upload is paused.");
          break;
        case TaskState.canceled:
          print("Upload was canceled");
          break;
        case TaskState.error:
          print("Upload has failed");
          break;
        case TaskState.success:
          taskSnapshot.ref
              .getDownloadURL()
              .asStream()
              .forEach((element) => profileEditBloc.add(ProfileLoadedEvent(element)));
          break;
      }
    }));
  }
}
