package com.example.seyoung.happytoytv.viewer;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.webrtc.EglBase;
import org.webrtc.VideoRenderer;

/**
 * Created by nhancao on 7/20/17.
 */

public interface ViewerView extends MvpView {
    void stopCommunication();

    void logAndToast(String msg);

    void disconnect();

    EglBase.Context getEglBaseContext();

    void sendserver();

    void setChatRoom(String chatRoom);

    VideoRenderer.Callbacks getRemoteProxyRenderer();
}
