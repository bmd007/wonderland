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

  @override
  Widget build(BuildContext context) {
    var loginCubit = context.watch<LoginCubit>();
    return BlocProvider(
      create: (context) => ProfileEditBloc(),
      child: BlocBuilder<ProfileEditBloc, ProfileEditState>(
        builder: (context, state) {
          var profileEditBloc = context.watch<ProfileEditBloc>();
          if (state.isLoading) {
            return Image.asset('images/wait.png');
          }
          return Scaffold(
              appBar: AppBar(centerTitle: true, title: const Text("Change your profile pic"), actions: [
                IconButton(
                  onPressed: () =>
                      Navigator.push(context, MaterialPageRoute(builder: (context) => DancePartnerSelectWidget())),
                  icon: Image.asset('images/match.png', height: 140, width: 140),
                )
              ]),
              body: body(profileEditBloc, loginCubit));
        },
      ),
    );
  }

  final storage = FirebaseStorage.instanceFor(bucket: "gs://wonderland-007.appspot.com");

  void uploadProfileImage(String dancerEmail, ProfileEditBloc profileEditBloc) async {
    profileEditBloc.add(const ProfileLoadingEvent());
    FilePickerResult? result = await FilePicker.platform
        .pickFiles(type: FileType.image, allowMultiple: false, lockParentWindow: true, withData: true);
    if (result != null && result.files.isNotEmpty) {
      final storageRef = storage.ref();
      final uploadTask = storageRef.child("$dancerEmail.jpeg").putData(result.files.first.bytes!);

      uploadTask.snapshotEvents.listen((TaskSnapshot taskSnapshot) {
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
      });
    }
  }

  Widget body(ProfileEditBloc profileEditBloc, LoginCubit loginCubit) {
    if (loginCubit.state.email.isNotEmpty){
      storage.ref().child("${loginCubit.state.email}.jpeg").getDownloadURL()
          .asStream()
          .forEach((element) => profileEditBloc.add(ProfileLoadedEvent(element)));
    }

    return loginCubit.state.email.isNotEmpty && !profileEditBloc.state.isLoading
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
            Text(
              loginCubit.state.name,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 50,
              ),
            ),
            TextButton(
                onPressed: () => uploadProfileImage(loginCubit.state.email, profileEditBloc),
                child: const Text("to change it")),
          ],
        )
      ],
    )
        : Image.asset('images/wait.png');
  }
}
