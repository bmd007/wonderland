import 'dart:convert';

import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:sdp_transform/sdp_transform.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

import 'video_chat_event.dart';
import 'video_chat_state.dart';

class VideoChatBloc extends Bloc<VideoChatEvent, VideoChatState> {
  late final RabbitMqWebSocketStompChatClient chatClient;
  late RTCPeerConnection _peerConnection;
  late final localVideoRenderer = RTCVideoRenderer();
  late final remoteVideoRenderer = RTCVideoRenderer();

  VideoChatBloc(String thisDancerName, String chatParty)
      : super(VideoChatState.withThisDancerName(thisDancerName, chatParty)) {
    on<OfferCreationRequestedEvent>((event, emit) async {
      RTCSessionDescription description = await _peerConnection.createOffer({'offerToReceiveVideo': 1});
      var session = parse(description.sdp.toString());
      var offerString = json.encode(session);
      await _peerConnection.setLocalDescription(description);
      printWithTime('local description before sending offer');
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/video/chat/offer', data: {"sender": thisDancerName, "receiver": chatParty, "content": offerString})
          .asStream()
          .where((event) => event.statusCode == 200)
          .forEach((element) {
            printWithTime('offer sent to $chatParty');
          });
    });

    on<CandidatePropagationRequestedEvent>((event, emit) async {
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/video/chat/candidate',
              data: {"sender": thisDancerName, "receiver": chatParty, "content": event.candidate})
          .asStream()
          .where((event) => event.statusCode == 200)
          .forEach((element) {
            printWithTime('candidate sent to $chatParty');
          });
    });

    on<CreateAnswerRequestedEvent>((event, emit) async {
      RTCSessionDescription description = await _peerConnection.createAnswer({'offerToReceiveVideo': 1});
      var session = parse(description.sdp.toString());
      var answerString = json.encode(session);
      await _peerConnection.setLocalDescription(description);
      printWithTime('local description before sending answer');
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/video/chat/answer',
              data: {"sender": thisDancerName, "receiver": chatParty, "content": answerString})
          .asStream()
          .where((event) => event.statusCode == 200)
          .forEach((element) {
            printWithTime('answer sent to $chatParty');
          });
    });

    on<OfferReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.offer);
      String sdp = write(session, null);
      RTCSessionDescription description = RTCSessionDescription(sdp, 'offer');
      await _peerConnection.setRemoteDescription(description);
      printWithTime('remote description after receiving offer');
      add(const CreateAnswerRequestedEvent());
    });

    on<CandidateReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.candidate);
      RTCIceCandidate rtcIceCandidate =
          RTCIceCandidate(session['candidate'], session['sdpMid'], session['sdpMlineIndex']);
      await _peerConnection.addCandidate(rtcIceCandidate);
      printWithTime(' _peerConnection.addCandidate');
    });

    on<AnswerReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.answer);
      String sdp = write(session, null);
      RTCSessionDescription description = RTCSessionDescription(sdp, 'answer');
      await _peerConnection.setRemoteDescription(description);
      printWithTime('remote description after receiving answer');
    });

    localVideoRenderer.initialize().asStream().forEach((element) {
      printWithTime('localVideoRenderer ready');
    });
    remoteVideoRenderer.initialize().asStream().forEach((element) {
      printWithTime('remoteVideoRenderer ready');
    });
    _createPeerConnection().asStream().forEach((element) {
      _peerConnection = element;
      printWithTime("pc created");
    });

    chatClient = RabbitMqWebSocketStompChatClient(thisDancerName, (StompFrame stompFrame) {
      String body = stompFrame.body!;
      if (stompFrame.headers.containsKey("type")) {
        if (stompFrame.headers["type"] == "WebRtcAnswer") {
          printWithTime("received answer by $thisDancerName");
          add(AnswerReceivedEvent(body));
        } else if (stompFrame.headers["type"] == "WebRtcOffer") {
          printWithTime("received offer by $thisDancerName");
          add(OfferReceivedEvent(body));
        } else if (stompFrame.headers["type"] == "WebRtcCandidate") {
          printWithTime("received candidate by $thisDancerName");
          add(CandidateReceivedEvent(body));
        }
      }
    });
  }

  Future<void> prepareLocalVideo() async {
    final Map<String, dynamic> mediaConstraints = {
      'audio': true,
      'video': {
        'facingMode': 'user',
      }
    };
    MediaStream mediaStream = await navigator.mediaDevices.getUserMedia(mediaConstraints);
    return _peerConnection.addStream(mediaStream);
  }

  Future<RTCPeerConnection> _createPeerConnection() async {
    Map<String, dynamic> configuration = {
      "iceServers": [
        {'url': 'stun:stun.l.google.com:19302'},
        {
          'url': "turn:openrelay.metered.ca:80",
          'username': "openrelayproject",
          'credential': "openrelayproject",
        },
      ]
    };

    final Map<String, dynamic> offerSdpConstraints = {
      "mandatory": {
        "OfferToReceiveAudio": true,
        "OfferToReceiveVideo": true,
      },
      "optional": [],
    };

    RTCPeerConnection pc = await createPeerConnection(configuration, offerSdpConstraints);

    pc.onIceCandidate = (e) {
      if (e.candidate != null) {
        var encodedCandidate = json.encode({
              'candidate': e.candidate.toString(),
              'sdpMid': e.sdpMid.toString(),
              'sdpMlineIndex': e.sdpMLineIndex,
            });
        printWithTime(' onIceCandidate  $encodedCandidate');
        add(CandidatePropagationRequestedEvent(encodedCandidate));
      }
    };
    pc.onSignalingState = (state) => printWithTime('onSignalingState $state');
    pc.onConnectionState = (state) => printWithTime('onConnectionState $state');
    pc.onIceConnectionState = (state) => printWithTime('onIceConnectionState $state');
    pc.onIceGatheringState = (state) => printWithTime('onIceGatheringState $state');
    pc.onRenegotiationNeeded = () {
      printWithTime("onRenegotiationNeeded");
      add(const OfferCreationRequestedEvent());
    };
    // pc.onAddStream = (event) {
    //   printWithTime("onAddStream event ownerTag ${event.ownerTag}");
    //   printWithTime("onAddStream event id ${event.id}");
    //   printWithTime("onAddStream event getVideoTracks.length ${event.getVideoTracks().length}");
    //   printWithTime("onAddStream event pc.getRemoteStreams().length ${pc.getRemoteStreams().length}");
    //   printWithTime("onAddStream event pc.getLocalStreams().length ${pc.getLocalStreams().length}");
    //   localVideoRenderer.srcObject = pc.getLocalStreams().first;
    //   printWithTime("local video added");
    //   remoteVideoRenderer.srcObject = pc.getRemoteStreams().first;
    //   remoteVideoRenderer.srcObject!.addTrack(event.getTracks().first);
    //   printWithTime("remote video added");
    // };
    final Map<String, dynamic> mediaConstraints = {
      'audio': true,
      'video': {
        'facingMode': 'user',
      }
    };
    MediaStream mediaStream = await navigator.mediaDevices.getUserMedia(mediaConstraints);
    localVideoRenderer.srcObject = mediaStream;
    mediaStream.getTracks().forEach((track) {
      pc.addTrack(track, mediaStream);
      printWithTime("track ${track.id} added to pc");
    });
    pc.onTrack = (event) {
      remoteVideoRenderer.srcObject = event.streams.first;
    };
    return pc;
  }

  void printWithTime(String s) {
    print("${DateTime.now()} $s");
  }
}
