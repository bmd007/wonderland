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
  late RTCPeerConnection? _peerConnection;
  MediaStream? _localStream;
  late final localVideoRenderer = RTCVideoRenderer();
  late final remoteVideoRenderer = RTCVideoRenderer();

  VideoChatBloc(String thisDancerName, String chatParty)
      : super(VideoChatState.withThisDancerName(thisDancerName, chatParty)) {
    on<OfferCreationRequestedEvent>((event, emit) async {
      _peerConnection = await _createPeerConnection();
      RTCSessionDescription description = await _peerConnection!.createOffer({'offerToReceiveVideo': 1});
      var session = parse(description.sdp.toString());
      var offerString = json.encode(session);
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/video/chat/offer', data: {"sender": thisDancerName, "receiver": chatParty, "content": offerString})
          .asStream()
          .where((event) => event.statusCode == 200)
          .forEach((element) {
            _peerConnection!
                .setLocalDescription(description)
                .asStream()
                .forEach((value) => {print('local description after sending offer')});
          });
    });

    on<CreateAnswerRequestedEvent>((event, emit) async {
      RTCSessionDescription description = await _peerConnection!.createAnswer({'offerToReceiveVideo': 1});
      var session = parse(description.sdp.toString());
      var answerString = json.encode(session);
      await ClientHolder.apiGatewayHttpClient
          .post('/v1/video/chat/answer',
              data: {"sender": thisDancerName, "receiver": chatParty, "content": answerString})
          .asStream()
          .where((event) => event.statusCode == 200)
          .forEach((element) {
        print('answer sent');
            _peerConnection!
                .setLocalDescription(description)
                .asStream()
                .forEach((value) => {print('local description after sending answer')});
          });
    });

    on<AnswerReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.answer);
      String sdp = write(session, null);
      RTCSessionDescription description = RTCSessionDescription(sdp, 'answer');
      await _peerConnection!.setRemoteDescription(description);
      print('remote description after receiving answer');

      MediaStream remoteMediaStream = _peerConnection!.getRemoteStreams().first!;
      remoteVideoRenderer.srcObject = remoteMediaStream;

      MediaStream localMediaStream = _peerConnection!.getLocalStreams().first!;
      localVideoRenderer.srcObject = localMediaStream;
    });

    on<OfferReceivedEvent>((event, emit) async {
      _peerConnection = await _createPeerConnection();

      dynamic session = await jsonDecode(event.offer);
      String sdp = write(session, null);
      RTCSessionDescription description = RTCSessionDescription(sdp, 'offer');
      await _peerConnection!.setRemoteDescription(description);
      add(const CreateAnswerRequestedEvent());
      print('remote description after receiving offer');

      MediaStream remoteMediaStream = _peerConnection!.getRemoteStreams().first!;
      remoteVideoRenderer.srcObject = remoteMediaStream;

      MediaStream localMediaStream = _peerConnection!.getLocalStreams().first!;
      localVideoRenderer.srcObject = localMediaStream;
    });

    localVideoRenderer.initialize();
    remoteVideoRenderer.initialize();

    chatClient = RabbitMqWebSocketStompChatClient(thisDancerName, (StompFrame stompFrame) {
      String body = stompFrame.body!;
      if (stompFrame.headers.containsKey("type")) {
        if (stompFrame.headers["type"] == "WebRtcAnswer") {
          print("received answer");
          add(AnswerReceivedEvent(body));
        } else if (stompFrame.headers["type"] == "WebRtcOffer") {
          print("received offer");
          add(OfferReceivedEvent(body));
        }
      }
    });
  }

  _createPeerConnection() async {
    Map<String, dynamic> configuration = {
      "iceServers": [
        {'url':'stun:stun.l.google.com:19302'},
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

    final Map<String, dynamic> mediaConstraints = {
      'audio': true,
      'video': {
        'facingMode': 'user',
      }
    };
    _localStream = await navigator.mediaDevices.getUserMedia(mediaConstraints);
    pc.addStream(_localStream!);

    pc.onIceCandidate = (e) {
      if (e.candidate != null) {
        // print(' onIceCandidate  ${json.encode({
        //   'candidate': e.candidate.toString(),
        //   'sdpMid': e.sdpMid.toString(),
        //   'sdpMlineIndex': e.sdpMLineIndex,
        // })}');
      }
    };

    pc.onIceConnectionState = (e) {
      print('onIceConnectionState $e');
    };

    return pc;
  }
}
