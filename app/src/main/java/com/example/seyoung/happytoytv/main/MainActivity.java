package com.example.seyoung.happytoytv.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.happytoytv.R;


/**
 * 사용자가 로그인을 한 뒤에 들어오는 메인 화면입니다.
 * 총 4개의 프레그먼트로 구성되어있으며,
   1. 실시간 방송 목록을 보는 프레그먼트
   2. VOD 목록을 보는 프레그먼트
   3. curl을 통해 데이터를 긁어오는 프레그먼트
   4. 내 정보 프레그먼트
   로 구성되어있습니다.
 */

public class MainActivity extends FragmentActivity implements View.OnClickListener {
   // private static final String TAG = MainActivity.class.getName();
    private static final int FRAGMENT_ONE = 1;
    private static final int FRAGMENT_TWO = 2;
    private static final int FRAGMENT_THR = 3;
    private static final int FRAGMENT_FO = 4;
    ImageButton imagestream;                                                // 실시간 방송을 할 수 있도록 이동시키는 카메라 이미지 버튼.
    TextView text;
    String userid;                                                          // 현재 사용자 아이디
    int mCurrentFragmentIndex=1;                                            // 처음에 메인화면에서, 실시간 방송 프레그먼트가 먼저 오도록 설정합니다.
    ImageButton bt_fiveFragment;
    ImageButton bt_fiveFragment2;
    ImageButton bt_fiveFragment3;
    ImageButton bt_fiveFragment4;
    Fragment newFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent inten = new Intent(this, MyService.class);       //로그인을 하면, 서비스를 실행시킵니다.
        startService(inten);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);    //해당 유저의 아이디값을 가져옵니다.
        userid= pref.getString("ing", "");

        bt_fiveFragment = (ImageButton)findViewById(R.id.imageButton);          //실시간 방송 프레그먼트를 가져오도록 하는 버튼
        bt_fiveFragment.setOnClickListener(this);
        bt_fiveFragment2 = (ImageButton)findViewById(R.id.imageButton2);        // VOD 프레그먼트를 가져오도록 하는 버튼
        bt_fiveFragment2.setOnClickListener(this);
        bt_fiveFragment3 = (ImageButton)findViewById(R.id.imageButton3);        // curl 화면을 가져오는 버튼
        bt_fiveFragment3.setOnClickListener(this);
        bt_fiveFragment4 = (ImageButton)findViewById(R.id.imageButton4);        // 내 정보 버튼
        bt_fiveFragment4.setOnClickListener(this);
        bt_fiveFragment.setSelected(true);

        imagestream = findViewById(R.id.imagestream);                           // 실시간 방송 이미지버튼을 연결시킵니다.
        text = findViewById(R.id.thrtext);

        fragmentReplace(mCurrentFragmentIndex);                                 // 먼저 실시간 방송 목록 프래그먼트를 기본으로 메인화면에서 보여준다.

    }

    //프래그먼트를 바꿔주는 메소드
    public void fragmentReplace(int reqNewFragmentIndex) {      // 사용자가 원하는 화면의 버튼을 눌렀을 경우 ex)VOD 목록 프레그먼트
      //  Fragment newFragment = null;
        newFragment = getFragment(reqNewFragmentIndex);         //해당 프레그먼트를 가져온다.
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction(); //Activit에서 프래그먼트를 추가,삭제,교체를 해주는
        transaction.replace(R.id.ll_fragment, newFragment);
        transaction.commit();                                                                   //원하는 프래그먼트로 교체를 하고 난 뒤에 최종반영을 한다.
    }

    //원하는 화면을 가져오고 싶을 때, 원하는 화면의 프래그먼트를 보내줍니다.
    private Fragment getFragment(int idx){              //ex) 1을 넣으면 OneFragment를 호출합니다
        Fragment newFragment = null;
        if(idx==1) {
            newFragment = new OneFragment();
        }
        if(idx==2) {
            newFragment = new TwoFragment();
        }
        if(idx==3) {
            newFragment = new ThrFragment();
        }
        if(idx==4) {
            newFragment = new FourFragment();
        }
        return newFragment;             //결정한 프래그먼트를 돌려준다.
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButton :         //첫번째 실시간 방송 목록을 보는 버튼을 누르면
                mCurrentFragmentIndex = FRAGMENT_ONE;
                fragmentReplace(mCurrentFragmentIndex); // 해당 프레그먼트의 숫자값으로 프레그먼트를 바꿔준다
                bt_fiveFragment.setSelected(true);      // 누른 이미지의 색깔을 빨간색으로 변경시키며, 나머지는 검정색으로 변경.
                bt_fiveFragment2.setSelected(false);
                bt_fiveFragment3.setSelected(false);
                bt_fiveFragment4.setSelected(false);
                imagestream.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);
                break;

            case R.id.imageButton2 :
                mCurrentFragmentIndex = FRAGMENT_TWO;       //
                fragmentReplace(mCurrentFragmentIndex);
                bt_fiveFragment.setSelected(false);
                bt_fiveFragment2.setSelected(true);
                bt_fiveFragment3.setSelected(false);
                bt_fiveFragment4.setSelected(false);
                imagestream.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);
                break;
            case R.id.imageButton3 :
                mCurrentFragmentIndex = FRAGMENT_THR;       //
                fragmentReplace(mCurrentFragmentIndex);
                bt_fiveFragment.setSelected(false);
                bt_fiveFragment2.setSelected(false);
                bt_fiveFragment3.setSelected(true);
                bt_fiveFragment4.setSelected(false);
                imagestream.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);
                break;
            case R.id.imageButton4 :                        //
                mCurrentFragmentIndex = FRAGMENT_FO;
                fragmentReplace(mCurrentFragmentIndex);
                bt_fiveFragment.setSelected(false);
                bt_fiveFragment2.setSelected(false);
                bt_fiveFragment3.setSelected(false);
                bt_fiveFragment4.setSelected(true);
                imagestream.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);
                break;
            case R.id.imagestream:
                if (!userid.equals(null)) {
                    mOnPopupClick(this.getCurrentFocus());
                } else {
                    Toast.makeText(this,"아이디 설정 오류",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public void mOnPopupClick(View v){                                          // 실시간 방송 버튼을 누르면, 작은 팝업창을 띄우도록 호출합니다.
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, PopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("data", userid);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            int request = requestCode & 0xffff;
            fragmentReplace(4);
            // 프래그먼트에서 결과값을 받아야 한다면 아래와 같이...
            //   Fragment fragment =  getSupportFragmentManager().findFragmentById(R.id.ll_fragment);
            newFragment.onActivityResult(requestCode, resultCode, data);
    }

}
