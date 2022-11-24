package com.example.seyoung.happytoytv.main;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * 서비스로 네티 서버와 연결해서 값을 주고 받습니다.
 * 채팅으로 받은 메세지를 서비스와 연결된 모든 액티비티에 보내줄 수 있습니다.
 * 만약 채팅방의 대기방일 경우, 채팅방의 리스트를 만들어줄 수 있습니다.
 * 채팅방일 경우, 메세지를 받거나, 다른 방의 채팅글을 받을 수 있습니다.
 */

public class MyService extends Service {
    String chat = "", chatroom = "null";  //채팅 내용과 채팅방 이름입니다.
    private static boolean isRunning = false;   //서비스가 실행 중인지 판단

    ArrayList<Messenger> mClients = new ArrayList<>(); //액티비티들을 넣을 어레이리스트입니다.
    public static final int MSG_REGISTER_CLIENT = 1;    // 액티비티들을 서비스에 등록시킬 때 사용
    public static final int MSG_UNREGISTER_CLIENT = 2;  // 액티비티가 종료 됐을 때, 서비스에서 해당 액티비티를 삭제해주는 값
    public static final int MSG_SET_INT_VALUE = 3;      //현재는 사용 x
    public static final int MSG_SET_STRING_VALUE = 4;   // 자바서버에서 받아온 메세지를 액티비티에 보낼 때 사용
    public static final int MSG_SET_STRING_CHATROOM = 5;// 사용자가 채팅방에 들어갔을 때, 해당 방 이름을 서비스에 보내줄 때 사용
    public static final int MSG_SET_STRING_GROUPROOM = 6;

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // 액티비티와 서비스 간 데이터를 전달할 수 있도록 해주는 Messenger입니다.
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "13.209.17.1";       // 서버에 연결하는 IP주소
    private static final int PORT = 5001;


    //OutputStream out;
    static String userId = "", userimage = ""; //유저 이름과 유저 이미지
    private final Handler handler = new Handler();  //액티비티와 서비스와의 데이터를 주고받게 해주는 핸들러.

    JSONObject json = null;                                 // 네티로 주고받을 josn
    static int counter = 0;                                 // 메세지를 주고 받을 때 늘어날 시간입니다.
    TimerTask timestamp;

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    //액티비티에서 전달받은 데이터를 자바서버에 보내줍니다.
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //서비스와 주고받는 액티비티들을 추가시키는 것.
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                //해당 액티비티를 목록에서 지웁니다.
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                //현재 안쓴다.
                case MSG_SET_INT_VALUE:
                    //  incrementby = msg.arg1;
                    break;
                //액티비티에서 채팅방이름을 전달받았을 때입니다.
                case MSG_SET_STRING_CHATROOM:
                    chatroom = msg.getData().getString("chatroom");
                    String active = msg.getData().getString("active");
                    Log.e("액티비티에서 방이름을 보내는가",chatroom+":"+active);
                    //액티비티가 대기방이나 다른 액티비티화면에 있지 않았을 경우에 보낸 데이터일 때,
                    //사용자가 채팅방을 만들었다라고 서비스에 요청했을 때 자바서버에 방을 만듭니다.
                    //채팅 대기화면이거나 어플에 있지 않은 경우.
                    if(active.equals("/made") || active.equals("/roomin")|| active.equals("/exit")|| active.equals("/presenterexit")) {
                        String makeroom = active;                              // 현재 스트리밍하는 스트리머의 이름으로 방을 만들어줍니다.
                        String roomid = chatroom;                                   // 방 이름과 현재 유저의 이름을 서버에 보내줍니다.
                        String usrid = userId;
                        new SendmsgTask().execute(makeroom, roomid, usrid);
                    } else if(chatroom.equals("out")){
                        String makeroom = "/exit";                              // 현재 스트리밍하는 스트리머의 이름으로 방을 만들어줍니다.
                        String roomid = chatroom;                                   // 방 이름과 현재 유저의 이름을 서버에 보내줍니다.
                        String usrid = userId;
                        new SendmsgTask().execute(makeroom, roomid, usrid);
                    }

                    break;
                //채팅방에서 메세지를 서비스로 보냈을 경우, 메세지를 자바서버에 보내줍니다.
                case MSG_SET_STRING_VALUE:
                    chat = msg.getData().getString("chating");
                    //소켓이 연결된 경우에만 보내줍니다.
                    try {
                        if (!TextUtils.isEmpty(chat)) {
                            new SendmsgTask().execute("/message", chatroom, userId, userimage, chat, counter + "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case MSG_SET_STRING_GROUPROOM:
                    chat = msg.getData().getString("groupcallid");
                    //소켓이 연결된 경우에만 보내줍니다.
                    try {
                        if (!TextUtils.isEmpty(chat)) {
                            new SendmsgTask().execute("/showcall", userId, userimage, chat);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    // 액티비티들에 자바서버에서 받은 메세지를 보내줍니다.
    private void sendMessageToUI(String send) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {

                Bundle b = new Bundle();
                b.putString("chating", send);
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                Log.e("액티비티들에게 보냄",send);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyService", "Service Started.");

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);//검색한 값을 리스트에 추가하기 전에,
        userId = pref.getString("ing", "");
        userimage = pref.getString(userId, "");
        //해당 유저의 아이디와 이미지 경로를 서버에서 받아옵니다.
        //서비스와 자바 서버를 연결을 시킵니다.

        serviceThread serviceThread = new serviceThread();
        serviceThread.start();

        isRunning = true;


    }

    class serviceThread extends Thread {
        public void run() {
            try {
                socketChannel = SocketChannel.open();                       // 네티와 통신할 채널을 열어줍니다.
                socketChannel.configureBlocking(true);
                socketChannel.connect(new InetSocketAddress(HOST, PORT));   // 네티가 깔린 서버 IP, 네티 PORT 번호로 네티와 안드로이드를 TCP로 연결시킵니다.
                new SendmsgTask().execute("/adduser", userId);
            } catch (Exception ioe) {
                Log.d("asd", ioe.getMessage() + "a");
                ioe.printStackTrace();
            }
            checkUpdate CheckUpdate = new checkUpdate();
            CheckUpdate.start();
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        //  START_REDELIVER_INTENT; // run until explicitly stopped.
        return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);

    }

    public static boolean isRunning() {
        return isRunning;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("MyService", "Service Stopped.");
        isRunning = false;

    }

    private class SendmsgTask extends AsyncTask<String, Void, Void> {                       // 유저의 정보와 작성한 메세지를 네티로 보내는 클래스.
        @Override
        protected Void doInBackground(String... strings) {
            try {
                String json = "";
                JSONObject jsonObject = new JSONObject();                                   // json으로 구성 후 사용자 정보와 메세지를 key/value 로 만든다.
                if (strings[0].equals("/made") || strings[0].equals("/roomin")                 // 만약 방을 만들려고 하거나 들어가고 나가려할 경우입니다,.
                        || strings[0].equals("/exit") || strings[0].equals("/presenterexit")) {
                    jsonObject.accumulate("active", strings[0]);                       // 방이름과 유저의 이름을 적어 보내줍니다.
                    jsonObject.accumulate("roomid", strings[1]);
                    jsonObject.accumulate("userid", strings[2]);
                } else if (strings[0].equals("/message")) {                                     // 방에 들어간 유저가 메세지를 보낼 때 json에 담을 if문.
                    jsonObject.accumulate("active", strings[0]);
                    jsonObject.accumulate("roomid", strings[1]);                        // 해당 방 이름, 유저 아이디, 유저 프로필 사진, 메세지, 시간을 보냅니다.
                    jsonObject.accumulate("userid", strings[2]);
                    jsonObject.accumulate("imagepath", strings[3]);
                    jsonObject.accumulate("message", strings[4]);
                    jsonObject.accumulate("timestamp", strings[5]);
                }else if (strings[0].equals("/showcall")) {
                    jsonObject.accumulate("active", strings[0]);
                    jsonObject.accumulate("friendid", strings[1]);
                    jsonObject.accumulate("friendpic", strings[2]);
                    jsonObject.accumulate("userid", strings[3]);
                } else if (strings[0].equals("/adduser")) {
                    jsonObject.accumulate("active", strings[0]);
                    jsonObject.accumulate("userid", strings[1]);
                }

                    json = jsonObject.toString();                                               // json을 String 으로 변환해서, 네티에서 String 으로 받을 수 있게 만듭니다.

                socketChannel                                                               // 연결된 네티 채널의 소켓과 문자를 담아 보낼 수 있는 outputStream, write을 부릅니다.
                        .socket()
                        .getOutputStream()
                        .write(json.getBytes("UTF-8"));                         // 연결한 네티서버로 json String을 보냅니다. UTF-8로 한글이 깨지지 않도록 지정.
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


    }
    class checkUpdate extends Thread {
        checkUpdate() {
        }
        public void run () {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer);                         // 서버에서 보내준 데이터를 받습니다.
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();

                json = new JSONObject(data);
                if (json.getString("active").equals("/showcall")) {
                    json.getString("friendid");
                    json.getString("friendpic");
                    Intent intent;
                    PendingIntent pendingIntent;
                    intent = new Intent(MyService.this, wait_facetime.class);
                    //해당 액티비티에 채팅방 이름과 상대방 아이디, 닉네임을 보내줍니다.
                    intent.putExtra("friendid",json.getString("friendid"));
                    intent.putExtra("friendpic",json.getString("friendpic"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);

                }else sendMessageToUI(data);




                    //    json = new JSONObject(data);                                                // 서버에서 받은 메세지를 json에 넣어줍니다.

                Log.d("receive", "msg :" + data);
               // handler.post(showUpdate);                                                   // 서버에서 받은 메세지를 화면 UI에 추가할 수 있도록, 핸들러로 돌려줍니다.
            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}