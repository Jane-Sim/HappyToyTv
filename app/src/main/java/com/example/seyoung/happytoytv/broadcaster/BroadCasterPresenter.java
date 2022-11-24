package com.example.seyoung.happytoytv.broadcaster;

import android.app.Application;
import android.util.Log;

import com.example.seyoung.happytoytv.rtc_peer.kurento.KurentoPresenterRTCClient;
import com.example.seyoung.happytoytv.rtc_peer.kurento.models.CandidateModel;
import com.example.seyoung.happytoytv.rtc_peer.kurento.models.response.ServerResponse;
import com.example.seyoung.happytoytv.rtc_peer.kurento.models.response.TypeResponse;
import com.example.seyoung.happytoytv.utils.RxScheduler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.DefaultSocketService;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionClient;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionParameters;
import com.nhancv.webrtcpeer.rtc_peer.SignalingEvents;
import com.nhancv.webrtcpeer.rtc_peer.SignalingParameters;
import com.nhancv.webrtcpeer.rtc_peer.StreamMode;
import com.nhancv.webrtcpeer.rtc_peer.config.DefaultConfig;
import com.nhancv.webrtcpeer.rtc_plugins.RTCAudioManager;

import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 실시간 방송을 하기위해, webrtc 연결과 쿠렌토 미디어 서버와 연결하는 액티비티입니다.
 */

public class BroadCasterPresenter extends MvpBasePresenter<BroadCasterView>
        implements SignalingEvents, PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = BroadCasterPresenter.class.getSimpleName();

    public static final String STREAM_HOST = "wss://13.209.17.1:8444/one2many";                     // 쿠렌토 서버가 깔린 IP주소를 적는다.

    private Application application;                                                                // 해당 앱을 불러온다.
    private SocketService socketService;                                                            // 쿠렌토 node socket서버와 안드로이드를 연동하기 위한 Socket service
    private Gson gson;                                                                              // 쿠렌토와의 메세지를 주고 받을 때, Gson으로 주고받는다.
    private String userid, roomname;
    private PeerConnectionClient peerConnectionClient;                                              // webrtc에 연결할 수 있도록 peerconnection을 생성합니다.
    private KurentoPresenterRTCClient rtcClient;                                                    // webrtc로 서로를 연결시켜줄 수 있는 쿠렌토 서버입니다. webrtc의 SDP및 ICE후보를 클라이언트와 서버간에 교환합니다.
    private PeerConnectionParameters peerConnectionParameters;                                      // webrtc의 비디오, 오디오 코덱 크기 등 설정값입니다.
    private DefaultConfig defaultConfig;                                                            // webrtc의 설정값을 확인할 수 있는 기본 설정 & 확인 값.
    private RTCAudioManager audioManager;                                                           // webrtc로 음성을 보낼 수 있도록 설정
    private SignalingParameters signalingParameters;                                                // webrtc 연결을 위해 필요한 ice와 sdp, 쿠렌토 소켓 통신에 필요한 값들을 담습니다.
    private boolean iceConnected;                                                                   // webrtc연결을 위해 필요한 ice서버가 연결되었는지 확인하는 iceconnnected 값

    public BroadCasterPresenter(Application application) {
        this.application = application;
        this.socketService = new DefaultSocketService(application);
        this.gson = new Gson();
    }

    public void initPeerConfig(String userid, String roomname) {
        this.userid =userid;
        this.roomname = roomname;
        rtcClient = new KurentoPresenterRTCClient(socketService);                                   // 쿠렌토에 해당 안드로이드앱을 연결시킵니다. 클라이언트로 저장.
        defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.SEND_ONLY);  // 쿠렌토로 해당 앱의 서버 정보들을 보냅니다. 방송자이기 때문에 스트림을 서버에 보내기만 합니다.
        peerConnectionClient = PeerConnectionClient.getInstance();                                  // webrtc를 연결시킵니다.
        peerConnectionClient.createPeerConnectionFactory(                                           // 현재 앱의 카메라 화면 크기, 코덱 등을 보낸다.
                application.getApplicationContext(), peerConnectionParameters, this);
    }

    public void disconnect() {                                                                      // 방송이 종료되면, 연결했던 쿠렌토 클라이언트를 지운다.
        if (rtcClient != null) {
            rtcClient = null;
        }
        if (peerConnectionClient != null) {                                                         // webrtc와의 연결도 종료한다.
            peerConnectionClient.close();
            peerConnectionClient = null;
        }

        if (audioManager != null) {                                                                 // 오디오도 멈춘다.
            audioManager.stop();
            audioManager = null;
        }

        if (socketService != null) {                                                                // 쿠렌토 소켓통신도 닫는다.
            socketService.close();
        }

        if (isViewAttached()) {                                                                     // 카메라 화면도 그만 가져온다.
            getView().disconnect();
        }
    }

    public void startCall() {                                                                       // 유저가 방송을 시작하게 되면
        if (rtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }

        rtcClient.connectToRoom(STREAM_HOST, userid, roomname, new BaseSocketCallback() {                     // 해당 미디어 쿠렌토로 연결시킨다.
            @Override
            public void onOpen(ServerHandshake serverHandshake) {                                   //보안 처리를 위해 핸드쉐이크를 실행하고
                super.onOpen(serverHandshake);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast("Socket connected");
                    }
                });
                SignalingParameters parameters = new SignalingParameters(                           // 클라이언트와 서버의 빠른 연결, 보안을 위해, stun서버와 turn서버를 추가시켜준다.
                        new LinkedList<PeerConnection.IceServer>() {
                            {
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
                                add(new PeerConnection.IceServer("turn:13.209.17.1:3478?transport=udp","simse","2362com"));
                                add(new PeerConnection.IceServer("stun:stun.services.mozilla.com"));
                                add(new PeerConnection.IceServer("turn:turn.bistri.com:80", "homeo", "homeo"));
                                add(new PeerConnection.IceServer("turn:turn.anyfirewall.com:443?transport=tcp", "webrtc", "webrtc"));

                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);                                                      // ice서버로 클라이언트와 서버간의 빠른 연결 길을 찾아준다.
            }

            @Override
            public void onMessage(String serverResponse_) {                                         //쿠렌토에서 클라이언트로 답을 줄 때, 실행되는 메서드이다.
                super.onMessage(serverResponse_);
                try {
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);

                    switch (serverResponse.getIdRes()) {
                        case PRESENTER_RESPONSE:
                            if (serverResponse.getTypeRes() == TypeResponse.REJECTED) {             // 쿠렌토와 연결이 잘 되었을 경우, 쿠렌토로부터 답장을 받습니다.
                                RxScheduler.runOnUi(o -> {
                                    if (isViewAttached()) {
                                        getView().logAndToast(serverResponse.getMessage());         // 서버로 부터 받은 답장을 로그와 토스트로 남깁니다.
                                        Log.e("BroadCasterMessage: ",serverResponse.getMessage());
                                    }
                                });
                            } else {
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
                                                                                serverResponse.getSdpAnswer());
                                onRemoteDescription(sdp);
                            }

                            break;

                        case ICE_CANDIDATE:                                                         //쿠렌토 서버에서 클라이언트와 빠른 연결을 하기위해 ICE를 안드로이드에 보내줍니다.
                            CandidateModel candidateModel = serverResponse.getCandidate();
                            onRemoteIceCandidate(                                                   // 받은 ICE의 세션값을 받습니다.
                                    new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(),
                                                     candidateModel.getSdp()));
                            break;
                        case PRESENTER_ROOMID:
                            getView().setChatRoom(serverResponse.getRoomid());
                            break;

                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {                                       // 쿠렌토와 연결이 꾾기면 소켓통신이 종료되었다고 알려줍니다. (BroadCasterActivity 화면에.)
                super.onClose(i, s, b);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast("Socket closed");
                    }
                    disconnect();                                                                   // 화면과 쿠렌토, webrtc를 종료합니다.
                });
            }

            @Override
            public void onError(Exception e) {                                                      //에러가 날 경우에도 마찬가지.
                super.onError(e);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast(e.getMessage());
                    }
                    disconnect();
                });
            }

        });

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = RTCAudioManager.create(application.getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start((audioDevice, availableAudioDevices) ->
                                   Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                                              + "selected: " + audioDevice));
    }

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    private void callConnected() {
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    @Override
    public void onSignalConnected(SignalingParameters params) {                                         // 쿠렌토와 연결을 할 때 사용하는 메소드입니다.
        RxScheduler.runOnUi(o -> {
            if (isViewAttached()) {
                signalingParameters = params;
                VideoCapturer videoCapturer = null;
                if (peerConnectionParameters.videoCallEnabled) {                                        // 현재 방송자의 카메라 화면을 캡쳐합니다.
                    videoCapturer = getView().createVideoCapturer();
                }
                peerConnectionClient                                                                    // 쿠렌토 서버와 연결한 뒤에, 해당 카메라 화면을 전송합니다.
                        .createPeerConnection(getView().getEglBaseContext(), getView().getLocalProxyRenderer(),
                                              new ArrayList<>(), videoCapturer,
                                              signalingParameters);

                if (signalingParameters.initiator) {                                                    // stun서버와 turn 서버로 클라이언트와 서버가 연결되면, 해당 방송자의 화면을 그려줍니다.
                    if (isViewAttached()) getView().logAndToast("Creating OFFER...");
                    getView().sendserver();                                                             //또한 채팅을 할 수 있도록 네티서버와 연결 하며, 서비스도 돌아가게 합니다.
                    // Create offer. Offer SDP will be sent to answering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createOffer();                                                 // 쿠렌토에서 받아온 세션과 미디어로 webrtc를 연결시킵니다.
                } else {
                    if (params.offerSdp != null) {                                                      //쿠렌토에서 받은 세션이 널값이면 쿠렌토에서 답을 기다립니다.
                        peerConnectionClient.setRemoteDescription(params.offerSdp);
                        if (isViewAttached()) getView().logAndToast("Creating ANSWER...");
                        // Create answer. Answer SDP will be sent to offering client in
                        // PeerConnectionEvents.onLocalDescription event.
                        peerConnectionClient.createAnswer();
                    }
                    if (params.iceCandidates != null) {                                                 // 쿠렌토에서 받은 stun, turn이 널값이면, 쿠렌토로 연결하기 전에 지정했던 서버들로 연결시켜줍니다.
                        // Add remote ICE candidates from room.
                        for (IceCandidate iceCandidate : params.iceCandidates) {
                            peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                if (isViewAttached()) getView().logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        RxScheduler.runOnUi(o -> {
            if (isViewAttached()) getView().logAndToast("Remote end hung up; dropping PeerConnection");
            disconnect();
        });
    }

    @Override
    public void onChannelError(String description) {
        Log.e(TAG, "onChannelError: " + description);
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                if (signalingParameters.initiator) {

                    rtcClient.sendOfferSdp(sdp);
                } else {
                    rtcClient.sendAnswerSdp(sdp);
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {                                            //
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidateRemovals(candidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        RxScheduler.runOnUi(o -> {
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        RxScheduler.runOnUi(o -> {
            if (isViewAttached()) getView().logAndToast("ICE disconnected");
            iceConnected = false;
            disconnect();
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        RxScheduler.runOnUi(o -> {
            if (iceConnected) {
                Log.e(TAG, "run: " + reports);
            }
        });
    }

    @Override
    public void onPeerConnectionError(String description) {
        Log.e(TAG, "onPeerConnectionError: " + description);
    }
}
