package com.example.seyoung.happytoytv.Base;


import com.example.seyoung.happytoytv.config.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 레트로핏을 사용할 수 있도록 설정합니다. AWS IP주소와 레트로핏이 Gson을 사용하도록 넣어줍니다.
 */

public class RetroFitApiClient{

        private static Retrofit retrofit = null;
        public static Retrofit getClient(){
            if(retrofit==null){
                retrofit = new Retrofit.Builder().baseUrl(Constant.URL_BASE)
                                                  .addConverterFactory(GsonConverterFactory
                                                  .create())
                                                  .build();
            }
            return retrofit;
        }
}