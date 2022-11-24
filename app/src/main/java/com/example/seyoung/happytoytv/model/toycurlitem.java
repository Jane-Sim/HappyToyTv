package com.example.seyoung.happytoytv.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 장난감이나 사용자가 원하는 검색어로 긁어온 데이터를 넣을 model
 */

public class toycurlitem {
    @SerializedName("toyid")
    @Expose
    private String toyid;               // 해당 데이터의 인덱스

    @SerializedName("price")
    @Expose
    private String price;               // 가격

    @SerializedName("picpath")
    @Expose
    private String picpath;             //사진의 주소

    @SerializedName("apppath")
    @Expose
    private String apppath;             // 해당 티몬 물건의 상세페이지 주소

    public String getToyid() {
        return toyid;
    }

    public void setToyid(String toyid) {
        this.toyid = toyid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPicpath() {
        return picpath;
    }

    public void setPicpath(String picpath) {
        this.picpath = picpath;
    }

    public String getApppath() {
        return apppath;
    }

    public void setApppath(String apppath) {
        this.apppath = apppath;
    }

}