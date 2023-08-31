package com.e510.commons;

import android.app.Application;
import android.content.Intent;

import com.e510.commons.activity.BaseActivity;
import com.e510.commons.utils.banner.VXBannerManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";

    public static BaseApplication INSTANCE;
    protected int stateCounter;
    public boolean disabledCallBackground;
    private ArrayList<BaseActivity> activities;

    @Override
    public void onCreate()
    {
        super.onCreate();
        stateCounter = 0;
        activities = new ArrayList<>();

        BaseApplication.INSTANCE = this;

        VXBannerManager.getInstance().cleanPushNotifications(this);
        checkFolders();
    }

    public boolean isApplicationOnBackground()
    {
        return stateCounter == 0 && !disabledCallBackground;
    }

    public void activityCreated(BaseActivity activity)
    {
        if (activity != null && !activities.contains(activity))
            activities.add(activity);
    }
    public void activityDestroyed(BaseActivity activity)
    {
        if (activity != null)
            activities.remove(activity);
    }

    public BaseActivity getCurrentActivity()
    {
        BaseActivity baseActivity = null;
        if (activities.size() > 0) {
            baseActivity = activities.get(activities.size()-1);
        }

        return baseActivity;
    }

    public int getActivitiesCount()
    {
        return activities.size();
    }

    public void activityStarted()
    {
        stateCounter++;
    }

    public void activityStopped()
    {
        stateCounter--;
    }

    public int getStateCounter()
    {
        return stateCounter;
    }

    public void checkFolders()
    {
        /*
        try
        {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.DIRECTORY_NAME;
            File dir = new File(path);

            if(!dir.exists())
            {
                dir.mkdir();
            }

            File fNoMediaBD = new File(dir, ".nomedia");
            if (!fNoMediaBD.exists()) {
                fNoMediaBD.createNewFile();
            }
        }
        catch (Exception e)
        {
            LogUtil.logE(TAG, e.getMessage(), e);
        }
        */
    }

    //Función repetida en BaseActivity y algunos adapters
    public void registerEventBus()
    {
        if (!EventBus.getDefault().isRegistered(this))
        {
            EventBus.getDefault().register(this);
        }
    }

    //Función repetida en BaseActivity y algunos adapters
    public void unRegisterEventBus()
    {
        if (EventBus.getDefault().isRegistered(this))
        {
            EventBus.getDefault().unregister(this);
        }
    }

    public Class getStartActivityClass()
    {
        return BaseActivity.class;
    }

    public Class getStartTransparentActivityClass()
    {
        return BaseActivity.class;
    }

    public void changeIconTo(int appId)
    {
    }

    public void logout()
    {
    }

    public void restartApp()
    {
        restartApp(false);
    }

    public void restartApp(boolean transparentSplash)
    {
        Intent intent = new Intent(getApplicationContext(), transparentSplash ? getStartTransparentActivityClass() : getStartActivityClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
