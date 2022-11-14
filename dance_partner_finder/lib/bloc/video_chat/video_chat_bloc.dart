import 'dart:convert';
import 'dart:developer';

import 'package:bloc/bloc.dart';
import 'package:dance_partner_finder/client/api_gateway_client_holder.dart';
import 'package:dance_partner_finder/client/rabbitmq_websocket_stomp_chat_client.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:rxdart/rxdart.dart';
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
      _peerConnection = await _createPeerConnection();
      await prepareLocalVideo();
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

    on<AnswerReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.answer);
      String sdp = write(session, null);
      RTCSessionDescription description = RTCSessionDescription(sdp, 'answer');
      await _peerConnection.setRemoteDescription(description);
      printWithTime('remote description after receiving answer');
    });

    on<OfferReceivedEvent>((event, emit) async {
      _peerConnection = await _createPeerConnection();
      await prepareLocalVideo();
      dynamic session = await jsonDecode(event.offer);
      String sdp = write(session, null);
      RTCSessionDescription description = RTCSessionDescription(sdp, 'offer');
      await _peerConnection.setRemoteDescription(description);
      printWithTime('remote description after receiving offer');
      add(const CreateAnswerRequestedEvent());
    });

    localVideoRenderer.initialize().asStream().forEach((element) {
      printWithTime('remoteVideoRenderer ready');
    });
    remoteVideoRenderer.initialize().asStream().forEach((element) {
      printWithTime('remoteVideoRenderer ready');
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
    await _peerConnection.addStream(mediaStream);
    localVideoRenderer.srcObject = _peerConnection.getLocalStreams().first;
    printWithTime("local video added as stream to peer connection");
    return;
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
        // printWithTime(' onIceCandidate  ${json.encode({
        //   'candidate': e.candidate.toString(),
        //   'sdpMid': e.sdpMid.toString(),
        //   'sdpMlineIndex': e.sdpMLineIndex,
        // })}');
      }
    };

    pc.onIceConnectionState = (e) {
      printWithTime('onIceConnectionState $e');
    };

    pc.onRenegotiationNeeded = () {
      printWithTime("onRenegotiationNeeded");
      // add(const OfferCreationRequestedEvent());
    };

    pc.onTrack = (event) {
      printWithTime("on track event");
      remoteVideoRenderer.srcObject = event.streams.last;
    };
    pc.onAddStream = (event) {
      printWithTime(" onAddStream event ownerTag ${event.ownerTag}");
      // remoteVideoRenderer.srcObject = event;
    };
    pc.onAddTrack = (stream, track) {
      printWithTime(" onAddTrack stream ownerTag ${stream.ownerTag}");
      // remoteVideoRenderer.srcObject = stream;
    };

    return pc;
  }

  void printWithTime(String s) {
    print("${DateTime.now()} $s");
  }
}
