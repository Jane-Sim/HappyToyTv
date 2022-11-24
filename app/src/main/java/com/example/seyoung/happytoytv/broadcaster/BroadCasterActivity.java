package com.example.seyoung.happytoytv.broadcaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.seyoung.happytoytv.Adapter.chatAdapter;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.main.MyService;
import com.example.seyoung.happytoytv.model.Chat;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.nhancv.npermission.NPermission;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;

/**
 * webrtc로 실시간 방송을 할 수 있는 액티비티입니다.
 * 또한 방송 중에 유저들과 채팅을 나눌 수 있습니다.
 * webrtc의 node서버에 연결이 되면, 방송자의 카메라 화면을 서버로 송출하게 되며 방송자의 핸드폰화면에도 카메라 화면이 그려집니다.
 * 실시간 방송에 연결되면, 채팅할 수 있는 네티서버로 접속되며 채팅방이 생성됩니다.
 */
@SuppressLint("Registered")
@EActivity(R.layout.activity_broadcaster)
public class  BroadCasterActivity extends MvpActivity<BroadCasterView, BroadCasterPresenter>
        implements BroadCasterView, NPermission.OnPermissionResult {
    private static final String TAG = BroadCasterActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;   //방송자의 카메라를 계속햏서 그려줄 Surfaceview 뷰입니다.
    RecyclerView chatRoomListView;                      //실시간 방송에서 채팅 내용을 나열해 보여줄 리싸이클러뷰
    ImageButton sendMsgBtn;                             //메세지 보내기 버튼
    EditText msgEditText;                               //사용자가 적을 메세지 에딧텍트뷰입니다.
    private NPermission nPermission;                    //카메라와 음성을 사용할 수 있도록 퍼미션을 체크.
    private EglBase rootEglBase;                        //Opengl로 화면을 그려낸다
    private ProxyRenderer localProxyRenderer;
    private Toast logToast;                             //사용자에게 webrtc 연결이 되었는지 알려줄 토스트창
    private boolean CameraisGranted;                    //카메라 퍼미션
    private boolean audioisGranted;                     //오디오 퍼미션
    private ArrayList<Chat> chats;                      //채팅 데이터를 넣을 ArrayList
    private chatAdapter adapter;                        //동적으로 추가되는 메세지데이터를 추가할 어댑터
    private final Handler handler = new Handler();      //서비스와 데이터를 주고받게 만드는 핸들러
    private String roomid,userid,profilepic,who,roomname;               // 네티로 주고 받을 메세지와 유저 닉네임, 프로필 사진입니다.
    public String chatroomid="";
    //private static final int CHATROOM = R.layout.activity_broadcaster;

    String data;                                        // 서비스에서 받은 메세지를 담을 String입니다.
    JSONObject json = null;                             // 네티서버에서 받은 json을 담을 JSONObject


    private Messenger mService=null;                    // 서비스와 액티비티를 연결시킬 수 있도록
    private final Messenger mMessenger = new Messenger(new IncomingHandler());                      // 해당 액티비티를 서비스에 추가시킬 수 있도록, 즉 연결할 수 있도록 해주는 Messenger입니다.
                                                                                                    //이를 통해 클라이언트와 IBinder를 공유할 수 있으므로, 액티비티가 서비스와 메세지를 주고받을 수 있습니다.

    //바인드 연결 유무
    private static boolean mIsBound = false;

    //서비스에서 현재 액티비티에 데이터를 보내서 받았을 때, 처리할 내용입니다.
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyService.MSG_SET_INT_VALUE:
                    //  textIntValue.setText("Int Message: " + msg.arg1);
                    break;

                case MyService.MSG_SET_STRING_VALUE:                                                //서비스에서 메세지를 보냈을 경우
                    data = msg.getData().getString("chating");
                    try {
                        json = new JSONObject(data);                                                // 서버에서 받은 json 형태의 메세지를 받습니다.
                        Log.e("receive", "msg :" + data);

                        //내 아이디와 받은 메세지의 아이디를 가지고 누구의 메세지인지 판단합니다.
//                        if (json.getString("active").equals("/roomin")) {
//                            chats.add(new Chat(json.getString("userid"), json.getString("message"), "1", json.getString("imagepath")));
//                            adapter.notifyDataSetChanged();
//                        } //채팅방이 만들어진 경우 로그에 남긴다. 또한 채팅방 이름을 지정해준다.
                        if (json.getString("active").equals("/message")) {                    // 서비스에서 받은 메세지가 온 경우, 해당 액티비티에 메세지를 추가합니다.
                            // String username;      친구 닉네임
                            // String chat;        채팅 내용
                            // String who;         누가 메세지를 보냈는 지 판단합니다.
                            // String profile;     사진 경로
                            // String time;        시간
                            if(json.getString("userid").equals(userid)){                      // 현재 유저와 메세지 보낸 사람의 아이디가 같다면, 자신이 보낸 것이라고 1로 체크해줍니다.
                                who = "1";
                            }else {
                                who = "0";
                            }
                            chats.add(new Chat(json.getString("userid"), json.getString("message"), who, json.getString("imagepath"))); // 채팅 리스트에 넣고, 어댑터를 새로고침해줍니다.
                            adapter.notifyDataSetChanged();
                            chatRoomListView.scrollToPosition(chats.size() - 1);                    //유저들이 계속 새로운 채팅을 볼 수 있게, 채팅 리스트 가장 아래롤 화면을 내려줍니다.
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    @AfterViews
    protected void init() {
        Log.e("BroadCasterActivity","실시간 방송 화면에 들어왔다");

        nPermission = new NPermission(true);                                           //현재 앱의 퍼미션을 체크해줍니다.

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);                                     // 현재 방송의 소리를 조절할 수 있게 설정.

        //config peer
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);                 // 쿠렌토 서버에서 받은 비디오 영상을 서피스뷰로 그려줍니다.
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);                // 풀 화면으로 가득 채워서 보여줍니다.
        vGLSurfaceViewCall.setEnableHardwareScaler(true);                                               // 표면에 고정 된 크기를 사용합니다. 사용자의 화면 크기에 맞춰 영상을 보여줍니다.
        vGLSurfaceViewCall.setMirror(true);                                                             // 방송자의 화면을 미러링으로 볼 수 있도록 합니다.
        localProxyRenderer.setTarget(vGLSurfaceViewCall);                                               //

        Intent intent = getIntent();                                                                    // 해당 방송자 아이디를 받아옵니다.
        roomid = intent.getStringExtra("userid");
        roomname = intent.getStringExtra("roomname");
        intent.getStringExtra("userid");
        presenter.initPeerConfig(roomid,roomname);                                                               //방송을 시작할 때, 해당 유저의 아이디를 서버와 같이 넘겨줍니다.

        Log.e("BroadCasterActivity","oncreate가 돌아간다");

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);                     // 현재 유저의 아이디와 프로필 사진 경로를 가져옵니다. 채팅 서버에 보낼 예정.
        userid= pref.getString("ing", "s");
        profilepic= pref.getString(userid, "s");

        chats = new ArrayList<Chat>();                                                                  //채팅 내용을 담을 리스트입니다.

        chatRoomListView = findViewById(R.id.chatRoomListView);                                         // 채팅 리싸이클러뷰
        sendMsgBtn = findViewById(R.id.sendMsgBtn);                                                     // 메세지를 보낼 버튼
        msgEditText = findViewById(R.id.msgEditText);                                                   // 메세지를 쓸 에딧텍스트
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false); //리싸이클뷰를 리니어레이아웃으로 지정 후 세로로 나열시킨다.
        chatRoomListView.setLayoutManager(manager);
        chatRoomListView.setItemAnimator(new DefaultItemAnimator());
        chatRoomListView.setNestedScrollingEnabled(false);      //리싸이클러뷰 안쪽에서만 스크롤하는 것이아닌, 데이터만큼 리사이클러큐 세로 스크롤을 길게 만들어줍니다.

        adapter = new chatAdapter(chats, getApplicationContext());
        chatRoomListView.setAdapter(adapter);

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBound) {
                    if(msgEditText.getText().toString().length()>0) {
                        final String return_msg = msgEditText.getText().toString();
                        sendMessageToService(return_msg);                                                 //서비스로 현재 적은 메세지를 보낸다.

                        chats.add(new Chat(userid, return_msg, "1", profilepic));                   //적은 메세지를 사용자의 화면에도 띄운다
                        adapter.notifyDataSetChanged();
                        msgEditText.setText(null);                                                          //적었던 메세지는 없애준다.
                        chatRoomListView.scrollToPosition(chats.size() - 1);
                    }
                }
            }
        });

    }


    public void setChatRoom(String chatRoom){
        chatroomid = chatRoom;
        Intent inten = new Intent(this, MyService.class);
        startService(inten);
        mIsBound = true;
        //서비스가 실행중인지 확인한다.
        CheckIfServiceIsRunning();
    }



    //채팅방 연결 유무를 나타낸다.
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.e("챗룸액티비티","서비스 연결시킴");
            //   textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);                                                                 //해당 액티비티를 서비스에 추가시켜서 연동하게 만든다.
                Log.e("serviceConnection","");
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {                                //유저가 해당 액티비티를 나가면, 서비스와의 연동을 끊는다.
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Log.e("챗룸액티비티","서비스 연결안되어잇움");
            // textStatus.setText("Disconnected.");
        }
    };


    //서비스가 실행 중인지 판단 여부.
    public void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MyService.isRunning()) {
            Log.e("BroadCasterActivity","서비스가 실행된다");
            doBindService();
        }
    }


    //실행되고 있으면 서비스에 현재 채팅방 이름을 보낸다.
    public void doBindService() {
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.e("바인드 연결됌","OK");
        handler.post(new Runnable() {
            public void run() {
                sendChatroomToService(chatroomid);
                Log.e("서버에서 보내는가?",chatroomid);
            }
        });
    }

    //메세지를 서비스에 보내준다.
    private void sendMessageToService(String chat) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("chating", chat);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_VALUE);
                    msg.setData(b);
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
    //채팅방 이름을 서비스에 보낸다.
    private void sendChatroomToService(String chatroom) {
        Log.e("바운드",mIsBound+"");
        if (mIsBound) {
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    if(chatroom.equals("out")){                                                     // 유저가 해당 방송방을 나갔을 때, 네티 서버에서도 나간처리를 해준다.
                        b.putString("chatroom", chatroomid);
                        b.putString("active", "/exit");
                    }else if(chatroom.equals("presenterexit")){                                     // 방송자가 방송을 종료하고 나갈때, 네티에서도 해당 채팅방을 없앤다.
                        b.putString("chatroom", chatroomid);
                        b.putString("active", "/presenterexit");
                    }else {                                                                         // 방송자가 해당 방송을 시작할때, 채팅방을 만들어준다.
                        b.putString("chatroom", chatroomid);
                        b.putString("active", "/made");
                    }
                    Log.e("진짜 방이름 보낸다",chatroomid);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_CHATROOM);
                    msg.setData(b);
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    //바인드를 해제한다.
    public void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //  textStatus.setText("Unbinding.");
        }
    }

    @Override
    public void disconnect() {                                                                      //방송자가 방송을 종료할 때, 쿠렌토 서버에서 해당 방송을 없애고, 사용자들의 영상 서버도 종료시킨다.
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
            Log.e("BroadCasterActivity","disconnect");
        }
    }

    @Override
    public void onStart() {                                                                         //해당 액티비티가 연결될때, 서비스가 실행중인지 확인하자
        super.onStart();
        Log.e("BroadCasterActivity","onstart가 돌아간다");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < 23 || CameraisGranted || audioisGranted) {
            presenter.startCall();
        } else if  (!CameraisGranted){
            nPermission.requestPermission(this, Manifest.permission.CAMERA);
        } else if  (!audioisGranted){
            nPermission.requestPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        Intent inten = new Intent(this, MyService.class);
        startService(inten);
        CheckIfServiceIsRunning();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        nPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(String permission, boolean isGranted) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                this.CameraisGranted = isGranted;                                                   //카메라, 오디오, 저장 퍼미션이 허락되지 않으면 수락해달라고 작은 창을 띄운다
                if (!CameraisGranted) {
                    nPermission.requestPermission(this, Manifest.permission.CAMERA);
                    nPermission.requestPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
                    nPermission.requestPermission(this, Manifest.permission.RECORD_AUDIO);
                }
            case Manifest.permission.RECORD_AUDIO:                                                  //
                this.audioisGranted= isGranted;
                if (!audioisGranted) {
                    nPermission.requestPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
                    nPermission.requestPermission(this, Manifest.permission.RECORD_AUDIO);
                } else {
                    presenter.startCall();                                                          // 퍼미션을 다 주었을 경우, 방송을 시작할 수 있게 쿠렌토 서버와 연결시킨다.
                }
                break;
            default:
                break;
        }
    }

    @NonNull
    @Override
    public BroadCasterPresenter createPresenter() {
        return new BroadCasterPresenter(getApplication());
    }


    @Override
    public void onBackPressed() {                                                                    //뒤로가기 버튼을 누를경우, 서비스와 연결을 끊고, 쿠렌토 서버와도 종료.
        super.onBackPressed();
        sendChatroomToService("presenterexit");
        doUnbindService();
        presenter.disconnect();
    }

    @Override
    public void logAndToast(String msg) {                                                           // 쿠렌토 서버와의 모든 처리를 보여줄 토스트창.
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override
    public void sendserver(){                                                                       // 서비스를 시작시킨다
/*        Intent inten = new Intent(this, MyService.class);
        startService(inten);
        mIsBound = true;
        //서비스가 실행중인지 확인한다.
        CheckIfServiceIsRunning();*/
    }

    @Override
    public VideoCapturer createVideoCapturer() {                                                    //카메라의 영상을 계속해서 가져오게 한다
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && presenter.getDefaultConfig().isUseCamera2();
    }

    private boolean captureToTexture() {
        return presenter.getDefaultConfig().isCaptureToTexture();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {                                                                       //뒤로가기 버튼을 누르거나 해당 앱을 강제종료할 경우, 서비스와 연결을 끊고, 쿠렌토 서버와도 종료.
        super.onBackPressed();
        super.onDestroy();
        try {
            sendChatroomToService("presenterexit");
            doUnbindService();
            Log.e("BroadCasterActivity","ondestroy가 돌아간다");
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }


}
