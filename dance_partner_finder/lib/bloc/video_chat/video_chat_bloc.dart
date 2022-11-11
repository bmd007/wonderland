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
            print(element);
            emit(state.offered(offerString));
            _peerConnection!.setLocalDescription(description);
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
            print(element);
            emit(state.answered(answerString));
            _peerConnection!.setLocalDescription(description).asStream().forEach((value) => {});
          });
    });

    on<AnswerReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.answer);
      print(session['candidate']);
      dynamic candidate = RTCIceCandidate(session['candidate'], session['sdpMid'], session['sdpMlineIndex']);
      await _peerConnection!.addCandidate(candidate);
      emit(state.answered(event.answer));
    });

    on<OfferReceivedEvent>((event, emit) async {
      dynamic session = await jsonDecode(event.offer);
      String sdp = write(session, null);
      _peerConnection = await _createPeerConnection();
      RTCSessionDescription description = RTCSessionDescription(sdp, 'answer');
      print(description.toMap());
      _peerConnection!.setRemoteDescription(description)
          .asStream().forEach((value) => add(const CreateAnswerRequestedEvent()));
      emit(state.offered(event.offer));
    });

    localVideoRenderer.initialize();
    remoteVideoRenderer.initialize();

    chatClient = RabbitMqWebSocketStompChatClient(thisDancerName, (StompFrame stompFrame) {
      String body = stompFrame.body!;
      if (stompFrame.headers.containsKey("type")) {
        if (stompFrame.headers["type"] == "WebRtcAnswer") {
          print("bmd received answer in $body");
          add(AnswerReceivedEvent(body));
        } else if (stompFrame.headers["type"] == "WebRtcOffer") {
          print("bmd received offer in $body");
          add(OfferReceivedEvent(body));
        }
      }
    });
  }

  _createPeerConnection() async {
    Map<String, dynamic> configuration = {
      "iceServers": [
        {'url':'stun:stun.l.google.com:19302'},
        {'url':'stun:stun1.l.google.com:19302'},
        {'url':'stun:stun2.l.google.com:19302'},
        {'url':'stun:stun3.l.google.com:19302'},
        {'url':'stun:stun4.l.google.com:19302'},
      ]
    };

    final Map<String, dynamic> offerSdpConstraints = {
      "mandatory": {
        "OfferToReceiveAudio": true,
        "OfferToReceiveVideo": true,
      },
      "optional": [],
    };

    _localStream = await _getUserMedia();

    RTCPeerConnection pc = await createPeerConnection(configuration, offerSdpConstraints);

    pc.addStream(_localStream!);

    pc.onIceCandidate = (e) {
      if (e.candidate != null) {
        print(json.encode({
          'candidate': e.candidate.toString(),
          'sdpMid': e.sdpMid.toString(),
          'sdpMlineIndex': e.sdpMLineIndex,
        }));
      }
    };

    pc.onIceConnectionState = (e) {
      print(e);
    };

    pc.onAddStream = (stream) {
      print('addStream: ${stream.id}');
      remoteVideoRenderer.srcObject = stream;
    };

    return pc;
  }

  _getUserMedia() async {
    final Map<String, dynamic> mediaConstraints = {
      'audio': true,
      'video': {
        'facingMode': 'user',
      }
    };

    MediaStream stream = await navigator.mediaDevices.getUserMedia(mediaConstraints);

    localVideoRenderer.srcObject = stream;
    return stream;
  }
}
