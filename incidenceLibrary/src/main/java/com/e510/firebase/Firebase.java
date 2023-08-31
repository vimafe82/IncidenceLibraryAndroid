package com.e510.firebase;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.e510.commons.BaseApplication;
import com.e510.commons.activity.BaseActivity;
import com.e510.commons.utils.Prefs;

import java.util.HashMap;

import es.incidence.core.utils.banner.VXBanner;
import es.incidence.core.utils.banner.VXBannerManager;

public class Firebase {

    public static final String TAG = "E510Fire";
    public static final String PUSH_TOKEN_LAST = "Firebase_PUSH_TOKEN_LAST";

    //public BaseApplication application;
    public Application application;

    private static Firebase INSTANCE;
    public static Firebase getInstance()
    {
        return INSTANCE;
    }
    //public static Firebase init(BaseApplication application) {
    public static Firebase init(Application application) {
        INSTANCE = new Firebase();
        INSTANCE.application = application;

        return INSTANCE;
    }

    public void savePushToken(String token)
    {
        Prefs.saveData(application, PUSH_TOKEN_LAST, token);
        //if (BuildConfig.DEBUG)
        //{
            Log.e(TAG, token);
        //}
    }

    public String getPushToken()
    {
        return Prefs.loadData(application, PUSH_TOKEN_LAST);
    }

    public void onReceivePush(PushNotification pn)
    {
        /*
        final VXBanner banner = new VXBanner(pn.getTitle(), pn.getMessage(), pn.getData(), "");
        final VXBannerManager bannerManager = VXBannerManager.getInstance();

        if (application.isApplicationOnBackground())
        {
            bannerManager.showPushNotification(application, banner.title, banner.message, banner.data, application.getStartActivityClass());
        }
        else if (pn.isShowInternalBanner())
        {
            final BaseActivity baseActivity = application.getCurrentActivity();
            if (baseActivity != null)
            {
                baseActivity.runOnUiThread(new Runnable() { //importante UI thread
                    public void run() {
                        View notificationView = bannerManager.showNotification(baseActivity, banner.title, banner.message, new VXBannerManager.VXBannerListener() {
                            @Override
                            public void onBannerClicked() {

                                Intent intent = new Intent(baseActivity, application.getStartTransparentActivityClass());
                                VXBannerManager.getInstance().putIntentExtraNotification(intent, banner.data);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                baseActivity.startActivity(intent);
                            }
                            @Override
                            public void onBannerClosed() {
                            }
                        });

                        if (banner.imageUrl != null && banner.imageUrl.length() > 0)
                        {
                            //ImageManager.loadImg(this, imageView, banner.imageUrl, R.drawable.placeholder);
                        }
                    }
                });
            }
        }
        */
    }
}
