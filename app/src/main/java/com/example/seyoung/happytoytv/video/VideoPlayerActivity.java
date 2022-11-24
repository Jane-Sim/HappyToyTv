package com.example.seyoung.happytoytv.video;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.seyoung.happytoytv.Adapter.chatAdapter;
import com.example.seyoung.happytoytv.Base.RetroFitApiClient;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.listener.getstream;
import com.example.seyoung.happytoytv.model.Chat;
import com.example.seyoung.happytoytv.model.chatitem;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 사용자가 찍은 영상을 다시보기로 재생할 수 있는 엑소플레이어입니다.
 * 영상의 파일을 불러와, 계속해서 다음 영상의 데이터를 가져옵니다.
 * 또한 방송중에 채팅한 내용을 서버에서 받아와, 영상과 채팅의 시간대를 맞춰
 * 채팅을 띄워줍니다.
 */
public class VideoPlayerActivity extends Activity implements ExoPlayer.EventListener{

    private SimpleExoPlayerView simpleExoPlayerView;            // 엑소플레이어의 영상 재생 뷰를 불러옵니다.
    private SimpleExoPlayer player;

    private Timeline.Window window;
    private DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private boolean shouldAutoPlay;
    private BandwidthMeter bandwidthMeter;
    private Handler mainHandler;
    ExampleThread thread;
    HashMap<String, chatitem> chatlist = new HashMap<String, chatitem>();
    private String roomid,userid,profilepic,who,vodpath;                           // 네티로 주고 받을 메세지와 유저 닉네임, 프로필 사진, 영상의 주소입니다.
    RecyclerView chatRoomListView;
    private ArrayList<Chat> chats;
    chatAdapter adapter;
    private final Handler handler = new Handler();
    private long playbackPosition;
    private int currentWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mainHandler = new Handler();
        shouldAutoPlay = true;
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "s");
        profilepic= pref.getString(userid, "s");
        chats = new ArrayList<>();

        //리싸이클뷰를 리니어레이아웃으로 지정 후 세로로 나열시킨다.
        chatRoomListView = findViewById(R.id.chatRoomListView);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        chatRoomListView.setLayoutManager(manager);
        chatRoomListView.setItemAnimator(new DefaultItemAnimator());
        chatRoomListView.setNestedScrollingEnabled(false);      //스크롤을 사용하지 않게해서, 마이페이지에서 스크롤할 때 데이터만큼 스크롤을 길게 만들어줍니다.

        adapter = new chatAdapter(chats, getApplicationContext());
        chatRoomListView.setAdapter(adapter);

        Intent intent = getIntent();
        vodpath = intent.getStringExtra("vodpath");                 //해당 영상의 주소를 가져옵니다.
        roomid = intent.getStringExtra("roomid");                   //해당 채팅방의 제목을 가져옵니다.

    }

    //서버에서 VOD 데이터를 가져오는 메소드입니다.
    public void getchatList(String roomid) {
        getstream apiInterface = RetroFitApiClient.getClient().create(getstream.class);           //서버와 연결을 시킨 후 선택한 VOD의 채팅들을 받아옵니다.
        Call<List<chatitem>> call = apiInterface.getChat(roomid);             // 선택한 채팅방의 이름을 서버에 보냅니다.
        call.enqueue(new Callback<List<chatitem>>() {                                        //서버와 연결하고 나서 받아온 결과입니다.
            @Override
            public void onResponse(Call<List<chatitem>> call, Response<List<chatitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우. 오류 알림창을 띄웁니다.
                    Toast.makeText(VideoPlayerActivity.this, "오류", Toast.LENGTH_SHORT).show();
                } else {
                    chatlist.clear();                               // 채팅 데이터를 넣을 리스트를 비워줍니다. 데이터가 중복되지 않도록,
                    for (chatitem chat : response.body()) {           // 받아온 채팅 데이터의 갯수만큼 리스트에 넣어줍니다.
                       // chatList.add(chat);
                        chatlist.put(chat.getTimestamp(),chat);
                       // list.add(Integer.valueOf(chat.getId()));
                        Log.i("RESPONSE: ", "" + chat.toString());
                    }
                }
                initializePlayer();
            }

            @Override
            public void onFailure(Call<List<chatitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
/*                if (progressDoalog.isShowing())
                    progressDoalog.dismiss();*/
                Toast.makeText(VideoPlayerActivity.this, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

    private void initializePlayer() {

        simpleExoPlayerView =  findViewById(R.id.vodPlayerView);


        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, new DefaultLoadControl());
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.showController();
        player.setPlayWhenReady(shouldAutoPlay);
        player.seekTo(currentWindow, playbackPosition);

        HlsMediaSource hlsMediaSource  = new HlsMediaSource(Uri.parse(vodpath),
                mediaDataSourceFactory, mainHandler,  null);

        player.prepare(hlsMediaSource);
        thread = new ExampleThread();
        thread.start();
    }


    private class ExampleThread extends Thread {
        private static final String TAG = "ExampleThread";

        public ExampleThread() {
            // 초기화 작업
        }

        public void run() {


            while (true) {
                try {
                    if(!shouldAutoPlay){Thread.interrupted();break;}
                    else if(player!=null){
                        int currentSecond = (int) (player.getCurrentPosition() / 1000);
                        String currentSeconds = String.valueOf(currentSecond);
                        if (!empty(chatlist.get(currentSeconds))) {
                            if (chatlist.get(currentSeconds).getTimestamp().equals(currentSeconds)) {
                                Log.e("VideoPlayerActivity :", currentSecond + "");
                                // String name;       닉네임
                                // String chat;        채팅 내용
                                // String who;         누가 보냈는 지 판단해주는 스트링
                                // String profile;     사진 경로
                                // String time;        시간
                                if (chatlist.get(currentSeconds).getUserid().equals(userid)) {
                                    who = "1";
                                } else {
                                    who = "0";
                                }
                                chats.add(new Chat(chatlist.get(currentSeconds).getUserid(), chatlist.get(currentSeconds).getMessage(), who, chatlist.get(currentSeconds).getImagepath()));
                                handler.post(new Runnable() {
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                        chatRoomListView.scrollToPosition(chats.size() - 1);
                                    }
                                });

                            }
                        }
                        // 스레드에게 수행시킬 동작들 구현
                        Thread.sleep(1000); //  엑소플레이어의 시간대에 맞춰, 1초마다 채팅방의 시간대를 찾고, 해당 시간대에 한 메세지가 있다면 리스트에 추가시키낟.
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void releasePlayer() {                                      // 해당 영상을 종료시키면 플레이어를 없애고, 쓰레드를 종료시킨다.
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
            trackSelector = null;
            thread.interrupt();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
           // getchatList(roomid);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            getchatList(roomid);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
         //   thread.interrupt();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if(player != null){
            player.setPlayWhenReady(false); // 플레이 중지
        }
    }

    public static Boolean empty(Object obj) {
        if (obj instanceof String) return obj == null || "".equals(obj.toString().trim());
        else if (obj instanceof List) return obj == null || ((List) obj).isEmpty();
        else if (obj instanceof Map) return obj == null || ((Map) obj).isEmpty();
        else if (obj instanceof Object[]) return obj == null || Array.getLength(obj) == 0;
        else return obj == null;
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}