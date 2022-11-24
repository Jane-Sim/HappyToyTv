package com.example.seyoung.happytoytv.viewer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;


/**
 * webrtc로 실시간 방송을 할 수 있는 액티비티입니다.
 * 또한 방송 중에 유저들과 채팅을 나눌 수 있습니다.
 * webrtc의 node서버에 연결이 되면, 방송자의 카메라 화면을 시청자가 볼 수 있게 되며
 * 실시간 방송에 연결되면, 채팅할 수 있는 네티서버로 접속되며 채팅방에 입장하게 됩니다.
 */
@SuppressLint("Registered")
@EActivity(R.layout.activity_viewer)
public class ViewerActivity extends MvpActivity<ViewerView, ViewerPresenter> implements ViewerView {
    private static final String TAG = ViewerActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;
    RecyclerView chatRoomListView;
    ImageButton sendMsgBtn ;
    EditText msgEditText;
    private EglBase rootEglBase;
    private ProxyRenderer remoteProxyRenderer;
    private Toast logToast;
    String msg, roomid, userid, profilepic, roomname;                           // 네티로 주고 받을 메세지와 유저 닉네임, 프로필 사진입니다.
    private ArrayList<Chat> chats;
    private chatAdapter adapter;        //동적으로 추가되는 메세지데이터를 추가할 어댑터
    String data;
    JSONObject json = null;                                 // 네티로 주고받을 josn
    private final Handler handler = new Handler();  //서비스와 데이터를 주고받게 만드는 핸들러
    public String chatroomid;

    private Messenger mService=null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    //바인드 연결 유무
    private static boolean mIsBound = false;
    //서비스에서 현재 액티비티에 데이터를 보낼 때,
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyService.MSG_SET_INT_VALUE:
                    //  textIntValue.setText("Int Message: " + msg.arg1);
                    break;
                //서비스에서 메세지를 보냈을 경우
                case MyService.MSG_SET_STRING_VALUE:
                    data = msg.getData().getString("chating");
                    try {
                        json = new JSONObject(data);                                                // 서버에서 받은 메세지를 json에 넣어줍니다.
                        Log.d("receive", "msg :" + data);

                        //내 아이디와 받은 메세지의 아이디를 가지고 누가 보낸 것인지 판단한다.
                        if (json.getString("active").equals("/roomin")) {
                            chats.add(new Chat(json.getString("userid"), json.getString("message"), "1", json.getString("imagepath")));
                            adapter.notifyDataSetChanged();
                        } //채팅방이 만들어진 경우 로그에 남긴다. 또한 채팅방 이름을 지정해준다.
                        else if (json.getString("active").equals("/message")) {
                            // String name;      친구 닉네임
                            // String chat;        채팅 내용
                            // String who;         누가 보냈는 지 판단해주는 스트링
                            //String profile;     사진 경로
                            // String time;        시간
                            chats.add(new Chat(json.getString("userid"), json.getString("message"), "1", json.getString("imagepath")));
                            adapter.notifyDataSetChanged();
                            chatRoomListView.scrollToPosition(chats.size() - 1);
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
        //config peer
        remoteProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);             // 쿠렌토 서버에서 받은 비디오 영상을 서피스뷰로 그려줍니다.
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);            // 풀 화면으로 가득 채워서 보여줍니다.
        vGLSurfaceViewCall.setEnableHardwareScaler(true);                                           // 표면에 고정 된 크기를 사용합니다. 사용자의 화면 크기에 맞춰 영상을 보여줍니다.
        vGLSurfaceViewCall.setMirror(true);
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);

        Intent intent = getIntent();
        roomid = intent.getStringExtra("userid");                                               // 해당 방송자 아이디를 받아옵니다.
        roomname = pref.getString("ing", "s");
        presenter.initPeerConfig();
        presenter.startCall(roomid,roomname);                                                                    // 해당 실시간 방송에 연결시킵니다.

        //서비스가 실행중인지 확인한다.

        Log.e("BroadCasterActivity","oncreate가 돌아간다");

        userid= pref.getString("ing", "s");                                           // 해당 유저의 아이디와 프로필 사진을 받아옵니다.
        profilepic= pref.getString(userid, "s");

        chats = new ArrayList<>();
        //리싸이클뷰를 리니어레이아웃으로 지정 후 세로로 나열시킨다.
        chatRoomListView = findViewById(R.id.chatRoomListView);                                     //채팅 내역을 넣을 리스트뷰
        sendMsgBtn = findViewById(R.id.sendMsgBtn);                                                 //채팅을 보낼 채팅 보내기 버튼
        msgEditText = findViewById(R.id.msgEditText);                                               //채팅 내용을 적을 에딧텍스트뷰
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        chatRoomListView.setLayoutManager(manager);
        chatRoomListView.setItemAnimator(new DefaultItemAnimator());
        chatRoomListView.setNestedScrollingEnabled(false);      //스크롤을 리싸이클 뷰 안쪽에서만 사용하지 않고, 데이터만큼 스크롤을 길게 만들어줍니다.

        //서비스에 현재 채팅방 이름을 보내준다.
        adapter = new chatAdapter(chats, getApplicationContext());
        chatRoomListView.setAdapter(adapter);

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBound) {
                    if(msgEditText.getText().toString().length()>0) {
                        final String return_msg = msgEditText.getText().toString();     // 작성한 메세지를 가져와 서버에 보냅니다.
                        sendMessageToService(return_msg);
                        chats.add(new Chat(userid, return_msg, "1", profilepic));   //또한 현재 채팅 리스트에 메세지를 추가해 메세지가 보이도록 한다.
                        adapter.notifyDataSetChanged();
                        msgEditText.setText(null);                                        // 적었던 채팅 내용은 지워준다.
                        chatRoomListView.scrollToPosition(chats.size() - 1);
                    }
                }
            }
        });

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
    public void setChatRoom(String chatRoom){
        chatroomid = chatRoom;
        Log.e(TAG, "setChatRoom: "+chatroomid);
        Intent inten = new Intent(this, MyService.class);
        startService(inten);
        mIsBound = true;
        //서비스가 실행중인지 확인한다.
        CheckIfServiceIsRunning();
    }

    //채팅방 연결 유무를 나타낸다.
    private ServiceConnection mConnection = new ServiceConnection() {                   // 서비스와 액티비티가 연결되면, 현재 채팅방 제목을 서비스로 보낸다.
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.e("챗룸액티비티","서비스 연결시킴");
            try {
                Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }

        public void onServiceDisconnected(ComponentName className) {                    // 현재 액티비티를 나가면, 서비스를 종료시켜준다.
            mService = null;
            Log.e("챗룸액티비티","서비스 연결안되어잇움");
        }
    };


    //서비스가 실행 중인지 판단 여부.
    public void CheckIfServiceIsRunning() {
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
                if(chatroomid!=null) {
                    sendChatroomToService(chatroomid);
                    Log.e("서버에서 보내는가?", chatroomid);
                }
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
                    mService.send(msg);                                 // 사용자가 적은 메세지를 서비스로 보내서 서비스가 서버에 보내도록 한다.
                } catch (RemoteException e) {
                }
            }
        }
    }
    //채팅방 이름을 서비스에 보낸다.
    private void sendChatroomToService(String chatroom) {
        Log.e("바운드",mIsBound+"");
        if (mIsBound) {
            //    Log.e("서비스",mService.toString());
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    if(chatroom.equals("out")){                     //유저가 현재 액티비티에서 나갈 경우, 채팅방에서도 나간 처리를 해준다.
                        b.putString("chatroom", chatroomid);
                        b.putString("active", "/exit");
                    }else {
                        b.putString("chatroom", chatroomid);
                        b.putString("active", "/roomin");           //유저가 현재 액티비티에 들어올 경우, 네티에서도 채팅방에 추가시킨다.
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
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);                         //서비스에 해당 액티비티를 지운다. 연결 끊음
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
    public void onStart() {
        super.onStart();
        Log.e("BroadCasterActivity","onstart가 돌아간다");
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent inten = new Intent(this, MyService.class);
        startService(inten);
        CheckIfServiceIsRunning();
        Log.e("BroadCasterActivity","onResume가 돌아간다");
    }

    @Override
    public void disconnect() {                                  // 해당 액티비티를 나갈 때, 화면에 방송자의 카메라 화면을 그만 그려준다.
        remoteProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }

    @NonNull
    @Override
    public ViewerPresenter createPresenter() {
        return new ViewerPresenter(getApplication());
    }

    @Override
    public void onBackPressed() {               // 뒤로 가기를 누를 때, 네티에 방 나가기 처리와, 쿠렌토 연결을 끊는다.
        super.onBackPressed();
        sendChatroomToService("out");
        doUnbindService();
        presenter.disconnect();
    }
    @Override
    public void onDestroy() {                   // 해당 액티비티가 종료될 때도 마찬가지.
        super.onDestroy();
        try {
            sendChatroomToService("out");
            doUnbindService();
            Log.e("BroadCasterActivity","ondestroy가 돌아간다");
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }
    @Override
    public void stopCommunication() {
        onBackPressed();
    }

    @Override
    public void logAndToast(String msg) {               // 쿠렌토를 통해서 온 메세지들을 토스트로 보여주거나 로그로 남긴다.
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getRemoteProxyRenderer() {
        return remoteProxyRenderer;
    }

}
