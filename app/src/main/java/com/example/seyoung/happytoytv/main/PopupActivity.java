package com.example.seyoung.happytoytv.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.broadcaster.BroadCasterActivity_;
import com.example.seyoung.happytoytv.config.Constant;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 실시간 방송을 하기 전에, 방송의 제목을 정하는 액티비티입니다.
 * 유저가 제목을 쓰고 방송하기 버튼을 누르면,
   해당 제목으로 실시간 방송 디비에 저장을 합니다.
 */

public class PopupActivity extends Activity {
    EditText EditText;                // 유저가 적을 방송 제목
    String userid;                    // 유저의 아이디
    ProgressDialog progressDialog;    // 서버에 저장될때까지 돌아갈 다이얼로그
    long mNow;                        // 현재 시간을 적을 변수
    Date mDate;                       // 현재 시간에서 오늘 날짜를 적을 변수
    SimpleDateFormat mFormat = new SimpleDateFormat("hhmmss", Locale.KOREA); // 오늘 날짜에서 가져올 시간. 시/분/초 로 정해준다

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기. 팝업창으로 띄울 액티비티라 타이틀바가 없어야한다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);

        //UI 객체생성
        EditText = (EditText)findViewById(R.id.txtText);

        //데이터 가져오기
        Intent intent = getIntent();                                // 유저의 아이디와 아이디에 시간을 붙여서 중복되지 않게 만든다. ex) ssiox041139
        String data = intent.getStringExtra("data");
        userid = data;                                              // 유저의 아이디를 받아온다.
        mNow = System.currentTimeMillis();                          // 현재 시간을 구해서, 원하는 시간대로 변경시킨다.
        mDate = new Date(mNow);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();                           // 팝업창의 확인 버튼을 누르면 서버로 해당 데이터가 저장된다.
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        Intent intent2 = new Intent(PopupActivity.this, BroadCasterActivity_.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         // 그리고 방송화면으로 넘겨줍니다.
        intent2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);               // 화면이 여러개가 뜨지 않도록 인텐트를 설정
        intent2.putExtra("userid", userid);
        intent2.putExtra("roomname", EditText.getText().toString());
        startActivity(intent2);
        finish();
        //액티비티(팝업) 닫기
    }
    //취소 버튼 클릭
    public void mOnFinish(View v){
        finish();
        //액티비티(팝업) 닫기
    }


    @SuppressLint("StaticFieldLeak")
    class addlive extends AsyncTask<String, Void, String> {             // 사용자가 실시간 방송하기를 원할 때, 해당 제목으로 저장합니다.

        @Override
        protected void onPreExecute() {                                 // 저장될때까지 프로그래스바를 돌립니다.
            progressDialog = new ProgressDialog(PopupActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();                                   // 서버에서 답을 받으면, 프로그래스바를 종료시킵니다.
            if (result != null) {


            } else {
                //실패할 경우 알림창을 통해 알려준다
                Toast.makeText(PopupActivity.this,"실패"+result,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String name = (String)params[0];
            String status = (String)params[1];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"/addlive.php";
            String postParameters = "user_name=" + name +"&text=" + status;     //유저의 아이디와 제목을 보냅니다.

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                return new String("Error: " + e.getMessage());
            }

        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
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
