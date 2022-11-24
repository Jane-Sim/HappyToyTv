package com.example.seyoung.happytoytv.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by seyoung on 2017-12-23.
 * 서버에서 해당 유저가 검색한 친구목록을 받아왔을 때 데이터를 넣을 클래스입니다.
 * 친구 아이디와 닉네임, 사진과 추가를 했는 지 안했는 지 유무를 받아옵니다.
 */

public class addFriendItem {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("userid")
    @Expose
    private String userid;
    @SerializedName("nicname")
    @Expose
    private String nicname;
    @SerializedName("pic")
    @Expose
    private String pic;
    private String zero = "0";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getNicname() {
        return nicname;
    }

    public void setNicname(String nicname) {
        this.nicname = nicname;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getZero() {
        return zero;
    }

    public void setZero(String zero) {
        this.zero = zero;
    }

}