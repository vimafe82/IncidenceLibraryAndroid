package es.incidence.core.manager.beacon;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.e510.commons.BaseApplication;
import com.e510.commons.utils.Prefs;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.IApplication;
import es.incidence.core.activity.ISplashActivity;
import es.incidence.core.domain.Beacon;

public class BeaconService extends Service
{
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("BeaconService", "onCreate");

        startScan();

        startForeground(BeaconManager.NOTIFICATION_ID, BeaconManager.getBeaconNotification(this));
        Prefs.saveData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED, "1");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e("BeaconService", "onStartCommand");

        startForeground(BeaconManager.NOTIFICATION_ID, BeaconManager.getBeaconNotification(this));
        Prefs.saveData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED, "1");

        return START_STICKY;
    }

    private void startScan()
    {
        Log.e("BeaconService", "startScan");

        BeaconManager.init(this);

        BeaconManager.getInstance().startBootstrap(new BeaconListener() {
            @Override
            public void didEnterRegion(String region)
            {
                Log.e("BeaconService", "didEnterRegion Open App1");

                IApplication app = (IApplication) getApplication();
                if (app.isApplicationOnBackground())
                {
                    Log.e("BeaconService", "didEnterRegion Open App2");


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        BeaconManager.showNotificationBeaconDetected(app, region);
                        /*
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (BaseApplication.INSTANCE.isApplicationOnBackground()) {
                                    BeaconManager.showNotificationBeaconSearch(app);
                                }
                            }
                        }, 60000);*/
                    }
                    else
                    {
                        Intent intent = new Intent(app, ISplashActivity.class);
                        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set android:launchMode="singleInstance" or you will get two instances
                        // created when a user launches the activity manually and it gets launched from here.
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        app.startActivity(intent);
                    }
                }
            }

            @Override
            public void onBeaconsDetected(ArrayList<Beacon> beacons) {
                Log.e("BeaconService", "onBeaconsDetected");
            }
        });
    }

    @Override
    public void onDestroy()
    {
        Log.e("BeaconService", "onDestroy");
        stopForeground(true);
        BeaconManager.getInstance().stopBootstrap();
        /*
        try
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(BeaconManager.CHANNEL_ID);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("BeaconService", "onBind");
        return null;
    }
}