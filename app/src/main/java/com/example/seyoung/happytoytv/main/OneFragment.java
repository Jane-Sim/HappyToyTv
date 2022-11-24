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

import com.example.seyoung.happytoytv.Adapter.liveAdapter;
import com.example.seyoung.happytoytv.Base.RetroFitApiClient;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.listener.getstream;
import com.example.seyoung.happytoytv.model.liveitem;
import com.example.seyoung.happytoytv.viewer.ViewerActivity_;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * 실시간 방송 목록을 보여주는 프레그먼트 화면입니다.
 * 실시간 방송의 1. 썸네일과 2. 방송 제목 3. 유저의 아이디값을 지정합니다.
 */

public class OneFragment  extends Fragment {
    public static OneFragment newInstance() {
        return new OneFragment();
    }
    liveAdapter liveadapter;                                                // 서버에서 받아온 실시간 방송 데이터를 넣어줄 어댑터입니다.
    RecyclerView recyclerView;                                              // xml과 현재 화면과 데이터를 연동해줄 RecyclerView
    ArrayList<liveitem> liveList = new ArrayList<liveitem>();                      // 서버에서 받은 데이터를 넣어줄 리스트.
    String userid;                                                          // 현재 유저의 아이디
    Parcelable recyclerViewState;                                           // 유저가 다른 화면을 띄웠어두 현재 스크롤 뷰로 이동시켜줄 스크롤뷰 위치 저장값.
    private SwipeRefreshLayout mSwipeRefreshLayout;                         // 현재 화면에서 유저가 새로고침을 할 때 띄워줄 스와이프 다이얼로그입니다.

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.onefragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        SharedPreferences pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "");
        Log.e("Onegragment",userid);
        getliveList();

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getliveList();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        liveadapter = new liveAdapter(liveList, getActivity());

        liveadapter.setOnItemClickListener(new liveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(liveitem item) {
                //Toast.makeText(getActivity(), item.ged_name(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(),ViewerActivity_.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("userid", item.getStreamer());                                    // 방송자의 아이디와 방송제목을 보내줍니다.

                startActivity(intent);                                                              // 방송보기 화면으로 넘어갑니다.
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext(),LinearLayoutManager.VERTICAL,false);     //리싸이클뷰를 행에 한줄씩 나열합니다
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(),new LinearLayoutManager(this.getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(liveadapter);


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)       // 멈췄을 때 리싸이클뷰의 저장한 값이 있을 경우.
            getliveList();                  // 서버에 다시 실시간 방송 데이터를 불러와서 방송 리스트를 갱신시킵니다.
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

    //서버에서 실시간 방송 데이터를 가져오는 메소드입니다.
    public void getliveList() {
        getstream apiInterface = RetroFitApiClient.getClient().create(getstream.class);           //서버와 연결을 시킨 후 유저의 아이디를 서버에 보냅니다
        Call<List<liveitem>> call = apiInterface.getLive(userid);
        call.enqueue(new Callback<List<liveitem>>() {                                        //서버와 연결하고 나서 받아온 결과입니다.
            @Override
            public void onResponse(Call<List<liveitem>> call, Response<List<liveitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우. 오류 알림창을 띄웁니다.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    liveList.clear();                                   // 방송 데이터를 넣을 리스트를 비워줍니다. 데이터가 중복되지 않도록,
                    liveadapter.clear();                                // 어댑터에 추가되었던 방송데이터들도 비워줍니다.

                    for (liveitem taste : response.body()) {           // 받아온 실시간 방송의 갯수만큼 리스트에 넣어줍니다.
                        liveList.add(taste);
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
                liveadapter.notifyDataSetChanged();                     // 리스트에 추가된 데이터가 어댑터에 추가되었으니,
                mSwipeRefreshLayout.setRefreshing(false);
            }                                                           // 어댑터를 새로고침해서 리싸이클뷰가 변경된 것을 유저에게 보여줍니다.

            @Override
            public void onFailure(Call<List<liveitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
/*                if (progressDoalog.isShowing())
                    progressDoalog.dismiss();*/
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }



}
