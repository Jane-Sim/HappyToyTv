package com.example.seyoung.happytoytv.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.happytoytv.Adapter.curlAdapter;
import com.example.seyoung.happytoytv.Base.RetroFitApiClient;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.listener.getstream;
import com.example.seyoung.happytoytv.model.toycurlitem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seyoung on 2018-08-10.
 */

public class curlactivity extends AppCompatActivity{
    curlAdapter curladapter;                                                                        //서버에서 파싱한 정보를 담을 어댑터입니다.
    RecyclerView recyclerView;                                                                      // xml과 현재 화면과 데이터를 연동해줄 RecyclerView
    ArrayList<toycurlitem> curlList = new ArrayList<toycurlitem>();                                           // 서버에서 받은 데이터를 넣어줄 리스트.

    String userid;                                                                                  // 현재 유저의 아이디
    Parcelable recyclerViewState;                                                                   // 유저가 다른 화면을 띄웠어두 현재 스크롤 뷰로 이동시켜줄 스크롤뷰 위치 저장값.

    String toyname;         //사용자가 적은 글씨나 클릭한 리스트의 이름을 보여줄 텍스트뷰
    EditText search;                                        //유저가 검색할 때 쓰는 에딧입니다.

    ImageView maps;                                          //지도에서 장난감가게를 찾고자 하는 경우에 쓰는 이미지뷰입니다.
    String filterText;                                      //유저가 장난감을 검색할 값을 필터로 보내 줄 필터스트링입니다.
    TextView nulltext;                                      //유저가 아무값도 안넣었을 때 보여줄 텍스트입니다.
    @Nullable
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl);
        recyclerView = findViewById(R.id.recycler_view);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "");
        Log.e("thrfragment",userid);

        curladapter = new curlAdapter(curlList, this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);     //이때 리싸이클뷰를 행에 한줄씩이 아닌,
        recyclerView.setLayoutManager(mLayoutManager);                                                     //그리드뷰로 2개씩 표시하게 만든 뒤, 설정해줍니다.
        recyclerView.setItemAnimator(new DefaultItemAnimator());                                           // 리싸이클뷰의 애니메이션은 기본으로 설정하며
        recyclerView.addItemDecoration(new GridSpacingdecoration(2, dpToPx(10), true));       // 리싸이클뷰의 중간의 구분선을 설정하며 아이템끼리의 패딩을 설정해줍니다.
        recyclerView.setAdapter(curladapter);                                                              //그리고 어댑터와 리스트를 연결해줍니다.

        ImageView clear = findViewById(R.id.clear);                      // 검색한 값을 한번에 지울 수 있도록 하는 엑스이미지

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setText("");  //적었던 에딧텍스트를 지워준다
            }
        });
        //리싸이클뷰에 추가된 데이터들의 레이아웃을 누를 때 리스너입니다.
        curladapter.setOnItemClickListener(new curlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(toycurlitem item) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://toyplus.co.kr/goods"+item.getApppath().substring(1)));
                startActivity(intent);
            }
        });

        nulltext =findViewById(R.id.nulltext);
        search = findViewById(R.id.autoCompleteTextView);                // 서버에 보내 줄 사용자 에딧입니다.
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);                   // 엔터키를 검색으로 바꿔서 검색 엔터키를 누르면 리스너가 발동하게 만듭니다.

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch(i){
                    case EditorInfo.IME_ACTION_SEARCH:              // 사용자가 검색 엔터키를 누를 때 생기는 리스너입니다.
                        toyname = search.getText().toString();          //다음 결과값 확인 화면에 보여줍니다.
                        getcurlList();
                        break;

                    default:

                        return false;
                }

                return true;


            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)       // 멈췄을 때 리싸이클뷰의 저장한 값이 있을 경우.
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
    public void getcurlList() {
        getstream apiInterface = RetroFitApiClient.getClient().create(getstream.class);           //서버와 연결을 시킨 후
        Call<List<toycurlitem>> call = apiInterface.gettoycurl(toyname);             //사용자의 입력값을 서버에 보내줍니다.
        call.enqueue(new Callback<List<toycurlitem>>() {                                        //서버와 연결하고 나서 받아온 결과입니다.
            @Override
            public void onResponse(Call<List<toycurlitem>> call, Response<List<toycurlitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우. 오류 알림창을 띄웁니다.
                    Toast.makeText(curlactivity.this, "오류", Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    nulltext.setVisibility(View.VISIBLE);
                } else {
                    curlList.clear();                                   // 데이터를 넣을 리스트를 비워줍니다. 데이터가 중복되지 않도록,
                    curladapter.clear();                                // 어댑터에 추가되었던 데이터들도 비워줍니다.

                    for (toycurlitem curl : response.body()) {           // 받아온 크롤링 갯수만큼 리스트에 넣어줍니다.
                        curlList.add(curl);
                        Log.i("RESPONSE: ", "" + curl.toString());
                    }
                    nulltext.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                curladapter.notifyDataSetChanged();                     // 리스트에 추가된 데이터가 어댑터에 추가되었으니,
            }                                                           // 어댑터를 새로고침해서 리싸이클뷰가 변경된 것을 유저에게 보여줍니다.

            @Override
            public void onFailure(Call<List<toycurlitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(curlactivity.this, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
                recyclerView.setVisibility(View.GONE);
                nulltext.setVisibility(View.VISIBLE);
            }
        });

    }


}
