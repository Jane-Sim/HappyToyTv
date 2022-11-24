package com.example.seyoung.happytoytv.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 실시간 방송에서 사용하는 채팅 모델입니다.
 */

public class chatitem {
    @SerializedName("id")
    @Expose
    private String id;                      // 해당 채팅방의 인덱스

    @SerializedName("roomid")
    @Expose
    private String roomid;                  // 해당 채팅방의 이름

    @SerializedName("userid")
    @Expose
    private String userid;                  // 채팅을 보낸 유저 아이디

    @SerializedName("imagepath")
    @Expose
    private String imagepath;               // 유저의 프로필 사진 주소

    @SerializedName("message")
    @Expose
    private String message;                 // 보낸 채팅 메세지 내용

    @SerializedName("timestamp")
    @Expose
    private String timestamp;               //채팅을 시작한 시간

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}

