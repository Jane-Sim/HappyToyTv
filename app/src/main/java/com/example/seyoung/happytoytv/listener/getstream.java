package com.example.seyoung.happytoytv.listener;


import com.example.seyoung.happytoytv.model.addFriendItem;
import com.example.seyoung.happytoytv.model.anotheritem;
import com.example.seyoung.happytoytv.model.chatitem;
import com.example.seyoung.happytoytv.model.liveitem;
import com.example.seyoung.happytoytv.model.toycurlitem;
import com.example.seyoung.happytoytv.model.voditem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 레트로핏을 통해서, 어떤 php로 데이터를 보내고 받을 지 설정해주는 인터페이스 화면입니다.
 */

public interface getstream {
    //php서버에서 라이브 스트리밍 정보창을 불러옵니다. 해당 유저의 아이디를 보냅니다.
    @GET("getlive.php")
    Call<List<liveitem>> getLive(@Query("user_name") String user_name);

    //php에서 vod 데이터를 불러들입니다.
    @GET("getvod.php")
    Call<List<voditem>> getVod(@Query("user_name") String user_name);

    //사용자가 방송을 시작하면 현재 방이름을 db에 저장시킵니다. 현재 유저의 아이디와 방 이름을 보냅니다.
    @GET("addlive.php")
    Call<List<liveitem>> addlive(@Query("user_name") String user_name,
                                 @Query("text") String text);

    @GET("getuser.php")
    Call<List<addFriendItem>> getUser(@Query("userid") String user_name);

    //사용자들이 나눴던 채팅을 해당 방이름으로 가져옵니다. 서버에서 해당 방 이름으로 디비에서 가져올 수 있게끔, 방이름을 서버에 보냅니다.
    @GET("getchat.php")
    Call<List<chatitem>> getChat(@Query("roomid") String roomid);

    //사용자가 장난감을 보길 원할 때, 온라인에서 크롤링으로 긁어옵니다. 검색어를 서버에 보냅니다.
    @GET("toycurl.php")
    Call<List<toycurlitem>> gettoycurl(@Query("toyname") String toyname);

    //세번째 프레그먼트에서 보여줄 여러가지 기능 데이터들을 불러옵니다. ex) 그룹통화, ar, 크롤링 등
    @GET("another.php")
    Call<List<anotheritem>> getanother(@Query("userid") String userid);

}
