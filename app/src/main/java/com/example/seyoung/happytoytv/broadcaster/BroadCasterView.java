package com.example.seyoung.happytoytv.broadcaster;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

/**
 * Created by nhancao on 7/20/17.
 */

public interface BroadCasterView extends MvpView {

    void logAndToast(String msg);

    void sendserver();

    VideoCapturer createVideoCapturer();

    EglBase.Context getEglBaseContext();

    VideoRenderer.Callbacks getLocalProxyRenderer();

    void setChatRoom(String chatRoom);

    void disconnect();
}
