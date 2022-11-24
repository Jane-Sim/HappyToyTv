package com.example.seyoung.happytoytv.main;

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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.seyoung.happytoytv.Adapter.friendArrayAdapter;
import com.example.seyoung.happytoytv.Base.RetroFitApiClient;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.listener.getstream;
import com.example.seyoung.happytoytv.model.addFriendItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seyoung on 2018-10-06.
 */

public class GroupCallActivity extends AppCompatActivity {
    String userId;
    List<addFriendItem> friendList;
    friendArrayAdapter friendArrayAdapter;
    RecyclerView recyclerView;
    Button startcall;
    HashMap<String,String> friendlist = new HashMap<String, String>();
    private final Handler handler = new Handler();  //서비스와 데이터를 주고받게 만드는 핸들러
    private Messenger mService=null;                    // 서비스와 액티비티를 연결시킬 수 있도록
    private final Messenger mMessenger = new Messenger(new IncomingHandler());                      // 해당 액티비티를 서비스에 추가시킬 수 있도록, 즉 연결할 수 있도록 해주는 Messenger입니다.
    //이를 통해 클라이언트와 IBinder를 공유할 수 있으므로, 액티비티가 서비스와 메세지를 주고받을 수 있습니다.

    //바인드 연결 유무
    private static boolean mIsBound = false;

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
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);    //해당 유저의 아이디값을 가져옵니다.
        userId = pref.getString("ing", "");

        startcall = findViewById(R.id.startcall);
        //서버에서 받아온 친구데이터를 담을 리스트
        friendList = new ArrayList<addFriendItem>();
        //리싸이클뷰와 어댑터
        recyclerView = findViewById(R.id.recycler_view);
        friendArrayAdapter = new friendArrayAdapter(friendList, this);                              //받아온 맛집의 리뷰데이터를 리싸이클뷰의 아이템과 연결시켜준다.
        CheckIfServiceIsRunning();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);          //리싸이클뷰를 리니어레이아웃으로 지정해준다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);                         //리니어 매니저는 세로로 리싸이클뷰를 나열시킨다.

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(friendArrayAdapter);

        getallfriend(userId);


        startcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Iterator<String> keys = friendlist.keySet().iterator();
                while( keys.hasNext() ){
                    String key = keys.next();
                    System.out.println( String.format("키 : %s, 값 : %s", key, friendlist.get(key)) );
                    sendGroupCallToService(friendlist.get(key));
                    Toast.makeText(GroupCallActivity.this, friendlist.get(key), Toast.LENGTH_SHORT).show();
                }

                Intent intent = getPackageManager().getLaunchIntentForPackage("computician.janusclient");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("roomid", "1234");
                  intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        friendArrayAdapter.setOnItemClickListener(new friendArrayAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(addFriendItem item) {
                if(Objects.equals(item.getZero(), "0")) {
                    String friendid = item.getUserid();
                    friendlist.put(friendid+"", friendid+"");
                   // Toast.makeText(GroupCallActivity.this, friendid, Toast.LENGTH_SHORT).show();
                    Toast.makeText(GroupCallActivity.this, friendlist.size()+"", Toast.LENGTH_SHORT).show();
                }else if(Objects.equals(item.getZero(), "1")) {
                    String friendid = item.getUserid();
                    friendlist.remove(friendid+"");
                   // Toast.makeText(GroupCallActivity.this, friendid, Toast.LENGTH_SHORT).show();
                    Toast.makeText(GroupCallActivity.this, friendlist.size()+"", Toast.LENGTH_SHORT).show();
                }
            }

        });


     }

    @Override
    public void onPause() {
        super.onPause();
        CheckIfServiceIsRunning();

    }

    @Override
    public void onResume() {
        super.onResume();
        CheckIfServiceIsRunning();

    }
    //서비스가 실행 중인지 판단 여부.
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MyService.isRunning()) {
            doBindService();
        }
    }
    //실행되고 있으면 서비스와 현재 액티비티를 연결시킨다.
    void doBindService() {
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.e("바인드 연결됌","OK");
        // textStatus.setText("Binding.");
    }


    //메세지를 서비스에 보낸다.
    private void sendGroupCallToService(String groupcallid) {
        Log.e("바운드",mIsBound+"");
        if (mIsBound) {
            //    Log.e("서비스",mService.toString());
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("groupcallid", groupcallid);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_GROUPROOM);
                    msg.setData(b);
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    //바인드를 해제한다.
    void doUnbindService() {
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
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("waitfacetimeActivity", "Failed to unbind from the service", t);
        }
    }

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

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Log.e("챗룸액티비티","서비스 연결안되어잇움");
            // textStatus.setText("Disconnected.");
        }
    };

    //사용자가 해당 액티비티에서 응답과 거절 버튼만 사용하도록 한다.
    //뒤로가기 버튼을 막아서 해당 액티비티 종료 x
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    //친구들을 불러옵니다.
    public void getallfriend(String userId){
        getstream apiInterface = RetroFitApiClient.getClient().create(getstream.class);            //서버와 연결을 시킨다.
        Call<List<addFriendItem>> call = apiInterface.getUser(userId);        //서버에 를 보낸다
        call.enqueue(new Callback<List<addFriendItem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<addFriendItem>> call, Response<List<addFriendItem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    for (addFriendItem Friend : response.body()) { // 유저들의 정보를 리스트에 담는다.
                        friendList.add(Friend);
                        friendArrayAdapter.notifyDataSetChanged();
                        Log.i("RESPONSE: ", "" + Friend.getUserid());
                    }
                }

                Toast.makeText(getApplicationContext(), friendList.size()+"", Toast.LENGTH_SHORT).show();
                friendArrayAdapter.notifyDataSetChanged();       //추가한 리싸이클뷰를 새로고침해서 유저에게 보여준다
            }

            @Override
            public void onFailure(Call<List<addFriendItem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getApplicationContext(), "결과값이 없습니다. " , Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });
    }
}
