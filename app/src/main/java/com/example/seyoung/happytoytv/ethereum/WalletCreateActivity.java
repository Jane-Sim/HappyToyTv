package com.example.seyoung.happytoytv.ethereum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.config.Config;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 사용자가 지갑을 생성할 때, 비밀번호를 적을 다이얼로그 액티비티창입니다.
 */

public class WalletCreateActivity extends Activity {
    android.widget.EditText EditText;                // 유저가 적을 비밀번호
    String userid;                    // 유저의 아이디
    ProgressDialog progressDialog;    // 월렛을 생성완료할 때까지 돌아갈 프로그래스
    File DataDir;                     // 지갑이 저장될 파일 위치
    String password;                  // 비밀번호 문자
    Config config = new Config();     // config.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기. 팝업창으로 띄울 액티비티라 타이틀바가 없어야한다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wallet_create_activity);
        DataDir = this.getExternalFilesDir("/keys/");

        //비밀번호를 담을 에딧택스트뷰를 가져온다.
        EditText = (android.widget.EditText)findViewById(R.id.txtText);
        EditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        userid = data;                                              // 유저의 아이디를 받아온다.

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //화면을 세로로 고정시킨다.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    //지갑이 없을 경우, 해당 앱의 파일위치에 지갑을 생성시키는 AsyncTask
    @SuppressLint("StaticFieldLeak")
    public class GenerateWalletThread extends AsyncTask<String,Integer,String> {

        private ProgressDialog mDialog;
        protected  void onPreExecute(){
            mDialog = ProgressDialog.show(WalletCreateActivity.this,"Please wait...", "Generate Wallet ...", true);
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                //해당 지갑의 이름을 가져온다.
                String fileName = WalletUtils.generateNewWalletFile(password, DataDir, false);
                Log.e("wallet ","FileName: " + DataDir.toString() +"/"+fileName);
                //해당 지갑의 주소를 저장한다.
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("adress", DataDir.toString()+"/"+fileName);
                editor.apply();
            } catch (NoSuchAlgorithmException
                    | NoSuchProviderException
                    | InvalidAlgorithmParameterException
                    | IOException
                    | CipherException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        protected void onPostExecute(String result){
            mDialog.dismiss();
            Toast.makeText(WalletCreateActivity.this, "지갑이 생성되었습니다", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();                           // 팝업창의 확인 버튼을 누르면 지갑이 생선된다.
            intent.putExtra("password",password);             // 지정한 비밀번호를 다시 돌려준다.
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        password = EditText.getText().toString();
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("password",password);
        editor.apply();
        config.setpasswordwallet(password);

        Log.e("설정한 비밀번호",password);

        new GenerateWalletThread().execute();
    }
    //취소 버튼 클릭
    public void mOnFinish(View v){
        finish();
        //액티비티(팝업) 닫기
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    @Override
    public void onDestroy() {           // 현재 화면이 파괴될 때, 프로그래스가 돌고있으면 제거해줍니다.
        super.onDestroy();
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
