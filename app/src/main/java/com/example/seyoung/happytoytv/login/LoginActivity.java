package com.example.seyoung.happytoytv.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.main.MainActivity;


/**
 * 사용자가 로그인 할 때 사용하는 로그인화면입니다.
 * 사용자의 아이디값을 받아서 쉐어드에 아이디를 저장합니다.
 * 아이디를 적고 로그인을 누르면, 메인 화면으로 이동시킵니다.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    EditText et_id;
    Button login_btn;
    VideoView videoview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.IdText);              //아이디를 적을 에딧텍스트
        login_btn = findViewById(R.id.loginbutton);     // 로그인 버튼

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.loginbutton:
                String id = et_id.getText().toString();
                if (!id.equals(null)) {                                         // 아이디가 널값이 아닐 때, 로그인되게 합니다.
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();                  //그리고 다음부턴 로그아웃을 하기 전까지
                    editor.putString("ing", id);                            //로그인 창을 안 보도록 자동로그인으로 만듭니다.
                    editor.putString("nova", "http://13.209.17.1/img/0129fe62ace70b265493481fffe279ea.jpg");
                    editor.putString("simse", "http://13.209.17.1/img/0a5372eba3018c80b97d3c248a0b9438.jpg");
                    editor.putString("Toys", "http://13.209.17.1/img/134817fb301b7230bfe72b04d18b39cf.jpg");

                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         //그리고 메인 화면으로 넘겨줍니다
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);               // 화면이 여러개가 뜨지 않도록 인텐트를 설정
                    startActivity(intent);                                          //한개만 뜨도록, 중복된 액티비티는 만들지 않고 제일 위해 하나만 띄우게하기
                    finish();
                } else {                                                        // 아이디를 적지 않으면 적어달라고 요청합니다.
                    Toast.makeText(this,"아이디를 입력해주세요",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
