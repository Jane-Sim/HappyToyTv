package com.example.seyoung.happytoytv.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.seyoung.happytoytv.Adapter.anotherAdapter;
import com.example.seyoung.happytoytv.Base.RetroFitApiClient;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.ar.WikitudeAr;
import com.example.seyoung.happytoytv.listener.getstream;
import com.example.seyoung.happytoytv.model.anotheritem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


/**
 * 사용자가 여러가지 기능을 사용할 수 있도록 해주는 세번째 프래그먼트입니다.
 * 해당 기능들을 누르면 액티비티로 전환됩니다. -> ex) 검색하는 콩순이를 누르면 curlactivity로 이동된다.
 */

public class ThrFragment extends Fragment {
    public static ThrFragment newInstance() {
        return new ThrFragment();
    }
    anotherAdapter atadapter;                                                                       //서버에서 파싱한 정보를 담을 어댑터입니다.
    RecyclerView recyclerView;                                                                      // xml과 현재 화면과 데이터를 연동해줄 RecyclerView
    ArrayList<anotheritem> atList = new ArrayList<anotheritem>();                                   // 서버에서 받은 데이터를 넣어줄 리스트.

    String userid;                                                                                  // 현재 유저의 아이디
    Parcelable recyclerViewState;                                                                   // 유저가 다른 화면을 띄웠어두 현재 스크롤 뷰로 이동시켜줄 스크롤뷰 위치 저장값.
    ProgressDialog progressDoalog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.thrfragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        SharedPreferences pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "");
        Log.e("thrfragment",userid);

        atadapter = new anotherAdapter(atList, getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());              // 리싸이클뷰의 리니어 레이아웃 형식으로 데이터들을 세로로 나열합니다
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());                                         // 리싸이클뷰의 애니메이션은 기본으로 설정하며
        recyclerView.addItemDecoration(new GridSpacingdecoration(1, dpToPx(10), true));     // 리싸이클뷰의 중간의 구분선을 설정하며 아이템끼리의 패딩을 설정해줍니다
        recyclerView.setAdapter(atadapter);                                                              // 그리고 어댑터와 리스트를 연결해줍니다.
        getatList();

        //리싸이클뷰에 추가된 데이터들의 레이아웃을 누를 때 리스너입니다.
        atadapter.setOnItemClickListener(new anotherAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(anotheritem item) {                                                 //불러온 데이터가 1일 경우에 wikitude 액티비티로 이동합니다.
                if(Objects.equals(item.getId(), "1")) {
                    Intent intent = new Intent(getContext(),WikitudeAr.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                if(Objects.equals(item.getId(), "2")) {                                               // 2일 경우, 그룹영상통화를 할 수 있는 야누스 액티비티로 이동합니다.
                    //Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("computician.janusclient");
                    Intent intent = new Intent(getContext(),GroupCallActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("roomid", "1234");
                  //  intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                if(Objects.equals(item.getId(), "3")) {                                               // 3일 경우, 슈퍼 마리오 대전 게임 유니티를 띄워줍니다.
                    Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("com.Seyoung.Maria");
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                if(Objects.equals(item.getId(), "4")) {                                               // 4일 경우, 크롤링 하는 액티비티로 이동합니다.
                    Intent intent = new Intent(getContext(),curlactivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)       // 멈췄을 때 리싸이클뷰의 저장한 값이 있을 경우.
        Log.e("멈췄나요","?");
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);  //리싸이클뷰의 저장되었던 위치를 불러와 다시 지정해줍니다.
                                                                                   // 스크롤한 값을 잃어버리지 않아서 사용자가 다시 스크롤을 할 필요x
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

    /**
     * 격자의 항목 사이에 간격을 추가합니다. 의미, 외부에 여백이 추가되지 않습니다.
     * 모서리의 가장자리.
     */
    public class GridSpacingdecoration extends RecyclerView.ItemDecoration {
        private int span;
        private int space;
        private boolean include;

        public GridSpacingdecoration(int span,int space, boolean include){
            this.span = span;
            this.space = space;
            this.include = include;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int posion =parent.getChildAdapterPosition(view);
            int column = posion % span;

            if(include){                //true일 때 리싸이클뷰의 가장자리를 만들어줍니다. ex) 리사이클뷰의 위와 양쪽의 여백을 만들어준다
                outRect.left = space -column * space / span;
                outRect.right = (column + 1)* space / span;

                if(posion<span){
                    outRect.top = space;
                }
                outRect.bottom = space;
            } else {                     //flase일 때 리싸이클뷰의 가장자리를 없애줍니다. ex) 리사이클뷰의 위와 양쪽의 여백을 만들지 않는다
                outRect.left = column * space / span;
                outRect.right = space - (column + 1) * space / span;
                if(posion>=span){
                    outRect.top = space;
                }
            }
        }
    }

    private int dpToPx(int dp){             // DP 를 픽셀로 변환하는 메소드. 기기마다 핸드폰 해상도가 다르고 화면 크기가 다르기에
        Resources r = getResources();       // 다른 기기들도 화면의 비율을 맞춰서 보여주기 위한 것이다.
        return  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics()));
    }

    //서버에서 크롤링 데이터를 가져오는 메소드입니다.
    public void getatList() {
        getstream apiInterface = RetroFitApiClient.getClient().create(getstream.class);           //서버와 연결을 시킨 후
        Call<List<anotheritem>> call = apiInterface.getanother(userid);             //사용자의 입력값을 서버에 보내줍니다.
        call.enqueue(new Callback<List<anotheritem>>() {                                        //서버와 연결하고 나서 받아온 결과입니다.
            @Override
            public void onResponse(Call<List<anotheritem>> call, Response<List<anotheritem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우. 오류 알림창을 띄웁니다.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                } else {
                    atList.clear();                                   // 데이터를 넣을 리스트를 비워줍니다. 데이터가 중복되지 않도록,
                    atadapter.clear();                                // 어댑터에 추가되었던 데이터들도 비워줍니다.

                    for (anotheritem at : response.body()) {           // 받아온 데이터 갯수만큼 리스트에 넣어줍니다.
                        atList.add(at);
                        Log.i("RESPONSE: ", "" + at.toString());
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                }
                atadapter.notifyDataSetChanged();                     // 리스트에 추가된 데이터가 어댑터에 추가되었으니,
            }                                                           // 어댑터를 새로고침해서 리싸이클뷰가 변경된 것을 유저에게 보여줍니다.

            @Override
            public void onFailure(Call<List<anotheritem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                if (progressDoalog.isShowing())
                    progressDoalog.dismiss();
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }


}
