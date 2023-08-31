package com.e510.firebase;

import java.util.HashMap;

public class PushNotification {

    private String title;
    private String message;
    private String sound;
    private HashMap<String, String> data;
    private boolean showInternalBanner;

    public PushNotification(String pSound, String pTitle, String pMessage, HashMap<String, String> pData)
    {
        this(pSound, pTitle, pMessage, pData, false);
    }

    public PushNotification(String pSound, String pTitle, String pMessage, HashMap<String, String> pData, boolean pShowInternalBanner) {

        this.message = pMessage;
        this.sound = pSound;
        this.title = pTitle;
        this.data = pData;
        this.showInternalBanner = pShowInternalBanner;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public boolean isShowInternalBanner() {
        return showInternalBanner;
    }

    public void setShowInternalBanner(boolean showInternalBanner) {
        this.showInternalBanner = showInternalBanner;
    }
}
