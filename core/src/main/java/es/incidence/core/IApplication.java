package es.incidence.core;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.e510.commons.BaseApplication;
import com.e510.commons.utils.DateUtils;
import com.e510.commons.utils.Prefs;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.location.DefaultLocationProvider;
import com.segment.analytics.Analytics;

import org.greenrobot.eventbus.EventBus;

import dev.b3nedikt.restring.Restring;
import dev.b3nedikt.reword.RewordInterceptor;
import dev.b3nedikt.viewpump.ViewPump;
import es.incidence.core.activity.ISplashActivity;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.beacon.BeaconManager;
import es.incidence.core.manager.beacon.BeaconService;

public class IApplication extends BaseApplication
{
    private static final String TAG = makeLogTag(IApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Constants.setBaseUrl(this);

        // Create an analytics client with the given context and Segment write key.
        Analytics analytics = new Analytics.Builder(this, Constants.KEY_SEGMENT)
                .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
                .recordScreenViews() // Enable this to record screen views automatically!
                .build();

        // Set the initialized instance as a globally accessible instance.
        Analytics.setSingletonInstance(analytics);


        //Localizables
        Restring.init(this);
        ViewPump.init(RewordInterceptor.INSTANCE);

        Core.init(this);

        //Beacons
        BeaconManager.init(this);

        //MapBox
        MapboxSearchSdk.initialize(this, getString(R.string.mapbox_access_token), new DefaultLocationProvider(this));
    }

//    private void initBeaconDetect()
//    {
//        try
//        {
//            BeaconManager.updateLang(getCurrentActivity());
//
//            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (mBluetoothAdapter == null) {
//                // Device does not support Bluetooth
//            } else if (!mBluetoothAdapter.isEnabled()) {
//                // Bluetooth is not enabled :)
//            } else {
//                // Bluetooth is enabled
//
//                Intent serviceIntent = new Intent(this, BeaconService.class);
//
//                String initiated = Prefs.loadData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED);
//                if (initiated != null)
//                {
//                    Prefs.removeData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED);
//                    // Paramos el servicio si estuviera arrancado
//                    stopService(serviceIntent);
//                }
//        /*
//        Intent serviceIntent2 = new Intent(this, BeaconService2.class);
//        // Paramos el servicio si estuviera arrancado
//        stopService(serviceIntent2);
//        */
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Correct If
//                    startForegroundService(serviceIntent); // Correct If
//                } else {
//                    startService(serviceIntent);
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//
//    }

    private void stopBeaconDetect()
    {
        try
        {
            Intent serviceIntent = new Intent(this, BeaconService.class);
            // Paramos el servicio si estuviera arrancado

            String initiated = Prefs.loadData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED);
            if (initiated != null) {
                Prefs.removeData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED);
                stopService(serviceIntent);
            }

        /*
        Intent serviceIntent = new Intent(this, BeaconService2.class);
        // Paramos el servicio si estuviera arrancado
        stopService(serviceIntent);
        */
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Class getStartActivityClass() {
        return ISplashActivity.class;
    }

    @Override
    public void activityStarted()
    {
        if (isApplicationOnBackground())
        {
            Core.updateApiLang();
            Core.registerDevice();
            Core.getGeneralData();
            Core.trackGeoposition();
            Core.manualAddressSearchResult = null;
            EventBus.getDefault().post(new Event(EventCode.APP_DID_BECOME_ACTIVE));

            stopBeaconDetect();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }

        super.activityStarted();
    }

    @Override
    public void activityStopped() {
        super.activityStopped();
        Core.activityStopped();
//        if (isApplicationOnBackground())
//        {
//            Prefs.removeData(this, Constants.NOTIFICATION_PUSH_ACTION_OPEN_INCIDENCE);
//
//            EventBus.getDefault().post(new Event(EventCode.APP_DID_RESIGN_ACTIVE));
//
//            if (Core.getUser() != null)
//            {
//                boolean initDetection = Core.hasAnyVehicleWithBeacon();
//
//                if (initDetection)
//                {
//                    //Chequeamos si hay incidencia activa
//                    String fechaMillis = Core.loadData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE);
//                    if (fechaMillis != null)
//                    {
//                        long fechaInci = Long.parseLong(fechaMillis);
//                        long ahora = DateUtils.getCurrentDate().getTimeInMillis();
//                        long diff = ahora - fechaInci;
//                        long dhours = diff / (60 * 60 * 1000);
//                        if (dhours < 1)
//                        {
//                            initDetection = false;
//                        }
//                    }
//                }
//
//                if (initDetection) {
//                    initBeaconDetect();
//                }
//            }
//        }
    }
}
