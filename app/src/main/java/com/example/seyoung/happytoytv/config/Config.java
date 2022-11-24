package com.example.seyoung.happytoytv.config;

import android.annotation.SuppressLint;
import android.app.Activity;

/**
 * Created by seyoung on 2018-08-16.
 */

@SuppressLint("Registered")
public class Config extends Activity {

    private String addressethnode= "https://ropsten.infura.io/v3/c24bb9b2b01240e4bd2741a7177d3692";
    private String addresssmartcontract = "0xc45ba41f64b3d21c2d99e90c5cb538f5e1e2afd2";
    private String passwordwallet="";
    private String transaction;

    public String addressethnode(){
        return addressethnode;
    }
    public String addresssmartcontract(){
        return addresssmartcontract;
    }
    public String passwordwallet(){
        return passwordwallet;
    }
    public void setpasswordwallet(String pass){
        this.passwordwallet= pass;
    }
    public String gettransaction() {return transaction;}
    public void settransaction(String transaction){
        this.transaction= transaction;
    }


}
