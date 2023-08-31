package com.e510.firebase;

import android.content.Intent;
import android.util.Log;

import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Prefs;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import es.incidence.core.Constants;
import es.incidence.core.manager.beacon.BeaconManager;
import es.incidence.core.manager.beacon.BeaconService;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        String refreshedToken = s;
        LogUtil.logD(TAG, "Refreshed token: " + refreshedToken);

        Firebase.getInstance().savePushToken(refreshedToken);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        try
        {
            LogUtil.logD(TAG, "From: " + remoteMessage.getFrom());

            /* No usamos notification porque cuando estás fuera de la app no entra por aqui
            https://wajahatkarim.com/2018/05/firebase-notifications-in-background--foreground-in-android/
            if (remoteMessage.getNotification() != null)
            {
                RemoteMessage.Notification notification = remoteMessage.getNotification();

                String title = notification.getTitle();
                String body = notification.getBody();
                String sound = notification.getSound();
                */

                if (remoteMessage.getData() != null)
                {
                    Map<String, String> tData = remoteMessage.getData();
                    LogUtil.logE(TAG, "tData: " + tData);

                    //Chat
                    if (tData.containsKey("typeMessage"))
                    {
                        String typeMessage = tData.get("typeMessage");

                        if (typeMessage.equals("CHAT_MESSAGE"))
                        {
                            String sound = tData.get("sound");
                            String body = tData.get("content");
                            String title = tData.get("title");
                            if (body != null && title != null)
                            {
                                String extraData = tData.get("threadId");

                                HashMap <String, String> data = new HashMap<>();
                                data.put("threadId", extraData);

                                receivedNotification(sound, title, body, data);
                                return;
                            }
                        }
                    }

                    String title = tData.get("title");
                    String body = tData.get("body");
                    String sound = tData.get("sound");

                    if (body == null) {
                        body = tData.get("text");
                    }

                    String objId = tData.get("objId");
                    String objJson = tData.get("objJson");
                    String dest = tData.get("dest");

                    HashMap <String, String> data = new HashMap<>();
                    data.putAll(tData);

                    if (objId != null)
                        data.put("objId", objId);
                    if (objJson != null)
                        data.put("objJson", objJson);
                    if (dest != null)
                        data.put("dest", dest);

                    if (title != null && body != null)
                    {
                        receivedNotification(sound, title, body, data);
                    }
                }
            //}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    // [END receive_message]

    protected void receivedNotification(String sound, String title, String message, HashMap<String, String> data)
    {
        boolean showInternalBanner = true;
        if (title.contains("Has activado tu baliza") || title.contains("Have you activated your beacon")) {
            BeaconManager.getInstance().cancelNotificationBeaconSearch();

            Intent serviceIntent = new Intent(this, BeaconService.class);

            String initiated = Prefs.loadData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED);
            if (initiated != null) {
                Prefs.removeData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED);
                // Paramos el servicio si estuviera arrancado
                stopService(serviceIntent);
            }

            showInternalBanner = false;
        }

        PushNotification pn = new PushNotification(sound, title, message, data, showInternalBanner);
        Firebase.getInstance().onReceivePush(pn);
    }
}
