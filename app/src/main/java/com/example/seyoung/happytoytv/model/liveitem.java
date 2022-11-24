package com.example.seyoung.happytoytv.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 실시간 방송의 데이터를 담을 model
 */

public class liveitem {
    @SerializedName("id")
    @Expose
    private String id;                      // 해당 실시간 방송의 인덱스

    @SerializedName("streamer")
    @Expose
    private String streamer;                // 방송자의 아이디

    @SerializedName("text")
    @Expose
    private String text;                    // 방송 제목

    @SerializedName("roomid")
    @Expose
    private String roomid;                  // 방송하는 유저의 아이디+방송을 시작한 시간

    @SerializedName("thumpath")
    @Expose
    private String thumpath;                // 방송의 썸네일 주소

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStreamer() {
        return streamer;
    }

    public void setStreamer(String streamer) {
        this.streamer = streamer;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getThumpath() {
        return thumpath;
    }

    public void setThumpath(String thumpath) {
        this.thumpath = thumpath;
    }

}
