package com.example.seyoung.happytoytv.ethereum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.seyoung.happytoytv.R;

/**
 * 사용자가 이더스캔 홈페이지를 웹뷰로 보는 액티비티입니다.
 * 해당 모바일지갑의 계좌정보를 보여주는 홈페이지로 이동하거나
   토큰 전송 시, 해당 토큰 전송 완료 홈페이지를 보여주게됩니다.
 */

public class Etherwebview extends AppCompatActivity {
    private WebView mWebView;
    private String myUrl = ""; // 접속 URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etherwebview);
        mWebView = findViewById(R.id.webView);                          // 웹뷰를 지정해줍니다.
        mWebView.getSettings().setJavaScriptEnabled(true);              // 해당 홈페이지에 자바스크립트가 있을 때, 사용하도록 설정

        Intent intent = getIntent();
        myUrl = intent.getStringExtra("url");                     // 접속하고자 하는 홈페이지의 url을 적습니다.

        mWebView.loadUrl(myUrl); // 접속 URL                            // 웹뷰에 원하는 url을 적어 홈페이지를 불러옵니다.
        mWebView.setWebChromeClient(new WebChromeClient());             // 크롬을 사용할 때 넣어줍니다.
        mWebView.setWebViewClient(new WebViewClientClass());            // 사용자가 해당 웹뷰에서 다른 홈페이지로 이동할 수 있도록 동적으로 이동시킵니다.
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }


}