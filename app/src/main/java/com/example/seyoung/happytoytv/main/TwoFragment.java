package com.example.seyoung.happytoytv.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.seyoung.happytoytv.Adapter.vodAdapter;
import com.example.seyoung.happytoytv.Base.RetroFitApiClient;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.listener.getstream;
import com.example.seyoung.happytoytv.model.voditem;
import com.example.seyoung.happytoytv.video.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * VOD를 보여주는 프래그먼트입니다.
 * 최근에 올라온 VOD가 제일 위로 올라오며, 저장한 실시간 방송을 보여줍니다.
 *
 */

public class TwoFragment extends Fragment {
    public static TwoFragment newInstance() {
        return new TwoFragment();
    }
    vodAdapter vodadapter;                                                                          // 현재 화면에서 vod 데이터를 담을 어댑터입니다.
    RecyclerView recyclerView;                                                                      // xml과 현재 화면과 데이터를 연동해줄 RecyclerView
    ArrayList<voditem> vodList = new ArrayList<voditem>();                                                // 서버에서 받은 데이터를 넣어줄 리스트.

    String userid;                                                                                  //유저의 아이디
    Parcelable recyclerViewState;                                                                   // 유저가 다른 화면을 띄워서 돌아와도, 현재 스크롤 뷰의 위치로 이동시켜줄 스크롤뷰 위치 저장값.
    private SwipeRefreshLayout mSwipeRefreshLayout;                                                 // 현재 화면에서 유저가 새로고침을 할 때 띄워줄 스와이프 다이얼로그입니다.

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.twofragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        SharedPreferences pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);     // 유저의 아이디를 가져옵니다.
        userid= pref.getString("ing", "");
        Log.e("Twogragment",userid);
        getvodList();                                                                               // 서버에서 vod 목록을 불러옵니다.

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getvodList();                                                                       //새로고침을 할 때마다 서버에서 vod 데이터를 다시 불러옵니다.
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,               //사용자가 새로고침을 할 때 나타날 스와이프입니다.
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        vodadapter = new vodAdapter(vodList, getActivity());

        vodadapter.setOnItemClickListener(new vodAdapter.OnItemClickListener() {                    // VOD에서 원하는 vod를 누르면, 해당 영상을 볼 수 있도록
            @Override                                                                               // exoplayer 화면으로 넘겨줍니다.
            public void onItemClick(voditem item) {
                Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("vodpath", item.getDuration());                               // 이때 채팅 방제목과 동영상의 주소를 같이 넘겨서 재생시키도록 합니다.
                intent.putExtra("roomid", item.getRoomid());
                startActivity(intent);
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(),new LinearLayoutManager(this.getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(vodadapter);


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)       // 멈췄을 때 리싸이클뷰의 저장한 값이 있을 경우.
            getvodList();                  // 서버에 다시 VOD 데이터를 불러와서 퍵 리스트를 갱신시킵니다.
        Log.e("멈췄나요","?");
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);  //리싸이클뷰의 저장되었던 위치를 불러와 다시 지정해줍니다.
        // feedadapter.notifyDataSetChanged();                                      // 스크롤한 값을 잃어버리지 않아서 사용자가 다시 스크롤을 할 필요x
    }

    @Override
    public void onStop() {                   //멈췄을 때 리싸이클뷰의 현재 스크롤 위치를 저장해준다.
        super.onStop();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

    }

    @Override
    public void onPause() {                 //멈췄을 때 리싸이클뷰의현재 스크롤 위치를 저장해준다.
        super.onPause();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    //서버에서 VOD 데이터를 가져오는 메소드입니다.
    public void getvodList() {
        getstream apiInterface = RetroFitApiClient.getClient().create(getstream.class);           //서버와 연결을 시킨 후 VOD의 리스트를 받아옵니다
        Call<List<voditem>> call = apiInterface.getVod(userid);
        call.enqueue(new Callback<List<voditem>>() {                                        //서버와 연결하고 나서 받아온 결과입니다.
            @Override
            public void onResponse(Call<List<voditem>> call, Response<List<voditem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우. 오류 알림창을 띄웁니다.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    vodList.clear();                                   // VOD 데이터를 넣을 리스트를 비워줍니다. 데이터가 중복되지 않도록,
                    vodadapter.clear();                                // 어댑터에 추가되었던 VOD데이터들도 비워줍니다.

                    for (voditem taste : response.body()) {           // 받아온 데이터의 갯수만큼 리스트에 넣어줍니다.
                        vodList.add(taste);
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
                vodadapter.notifyDataSetChanged();                     // 리스트에 추가된 데이터가 어댑터에 추가되었으니,
                mSwipeRefreshLayout.setRefreshing(false);
            }                                                           // 어댑터를 새로고침해서 리싸이클뷰가 변경된 것을 유저에게 보여줍니다.

            @Override
            public void onFailure(Call<List<voditem>> call, Throwable t) {        //서버와 연결 실패 할 경우
/*                if (progressDoalog.isShowing())
                    progressDoalog.dismiss();*/
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

}
