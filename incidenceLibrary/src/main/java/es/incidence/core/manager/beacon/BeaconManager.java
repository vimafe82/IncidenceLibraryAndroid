package es.incidence.core.manager.beacon;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

import es.incidence.core.Core;
import es.incidence.core.domain.Beacon;
import es.incidence.core.domain.Vehicle;

public class BeaconManager implements RangeNotifier, MonitorNotifier
{
    private static final String TAG = makeLogTag(BeaconManager.class);
    public static final int NOTIFICATION_ID = 5758;
    public static final String NOTIFICATION_EXTRA_BEACON = "incidence_beacon_detected";
    public static final String CHANNEL_ID_BEACON_SCAN = "incidence_app_channel_beacon_scan";
    public static final String CHANNEL_ID_BEACON_DETECTED = "incidence_app_channel_beacon_detected";

    public static final String NOTIFICATION_EXTRA_BEACON_CAR = "incidence_beacon_detected_car";
    public static final String NOTIFICATION_EXTRA_BEACON_CAR_CLICKED = "incidence_beacon_detected_car_clicked";

    private static final String BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private static final String REGION_DEVICES_1 = "75631298-a7eb-407c-8c0e-ba6b8920edad";
    private static final String REGION_DEVICES_2 = "ba9a3092-a4b6-4ef7-aeb3-ea118ee5ee5d";

    private Context context;

    private static BeaconManager instance;
    //private org.altbeacon.beacon.BeaconManager beaconManager;
    private org.altbeacon.beacon.BeaconManager beaconManagerInApp;

    private NotificationCompat.Builder builder;
    private Notification notification;

    private BeaconListener scanBeaconsListener;
    private BeaconListener regionBootstrapListener;
    private Region regionIn;

    private String STR_BEACON_SEARCHING_TITLE = "";
    private String STR_BEACON_SEARCHING_MESSAGE = "";
    private String STR_BEACON_DETECTED_TITLE = "";
    private String STR_BEACON_DETECTED_MESSAGE = "";

    private BeaconManager(Context context) {
        this.context = context;
    }
    public static void init(Context context)
    {
        if (BeaconManager.instance == null)
        {
            BeaconManager.instance = new BeaconManager(context);

            //Beacon para búsqueda al añadir balizas
            initBeaconManagerInApp(context);

            //Beacon para búsqueda en foreground
            //initBeaconManager(context);
        }
    }

    private static void initBeaconManagerInApp(Context context)
    {
        BeaconManager.instance.beaconManagerInApp = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context);
        BeaconManager.instance.beaconManagerInApp.addMonitorNotifier(BeaconManager.instance);
        BeaconManager.instance.beaconManagerInApp.addRangeNotifier(BeaconManager.instance);
        BeaconManager.instance.beaconManagerInApp.setDebug(true);

        BeaconManager.instance.beaconManagerInApp.getBeaconParsers().clear();
        BeaconManager.instance.beaconManagerInApp.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_LAYOUT));
    }

    public static void updateLang(Context context) //para que agarre los strings del ws.
    {
        if (context != null)
        {
            /*
            BeaconManager.instance.STR_BEACON_SEARCHING_TITLE = Core.getString(R.string.beacon_notif_title, context);
            BeaconManager.instance.STR_BEACON_SEARCHING_MESSAGE = Core.getString(R.string.beacon_notif_subtitle, context);
            BeaconManager.instance.STR_BEACON_DETECTED_TITLE = Core.getString(R.string.ask_beacon_activated, context);
            BeaconManager.instance.STR_BEACON_DETECTED_MESSAGE = Core.getString(R.string.ask_beacon_activated_desc, context);

            BeaconManager.instance.builder.setContentTitle(BeaconManager.instance.STR_BEACON_SEARCHING_TITLE);
            BeaconManager.instance.builder.setContentText(BeaconManager.instance.STR_BEACON_SEARCHING_MESSAGE);
            BeaconManager.instance.notification = BeaconManager.instance.builder.build();
            BeaconManager.instance.beaconManager.enableForegroundServiceScanning(BeaconManager.instance.notification, NOTIFICATION_ID);
            */
        }
    }

    public static void showNotificationBeaconSearch(Context context)
    {
        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.notify(NOTIFICATION_ID, getBeaconNotification(context));
    }

    public static void cancelNotificationBeaconSearch()
    {
        NotificationManager notificationManager = getNotificationManager(getInstance().context);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /*
    public static void showNotificationBeaconDetected(Context context, String region)
    {
        String beaconKey = region != null ? region : null;

        Intent pIntent = new Intent(context, ISplashActivity.class);
        pIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pIntent.setAction("beacon_detected");
        if (beaconKey != null) {
            pIntent.putExtra(NOTIFICATION_EXTRA_BEACON, beaconKey);

            //no hemos conseguido pasarlo por intent para Andorid Auto
            Prefs.saveData(context, NOTIFICATION_EXTRA_BEACON_CAR, beaconKey);
        }

        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, pIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, pIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        }else {
            pendingIntent = PendingIntent.getActivity(context, 0, pIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        NotificationManager notificationManager = getNotificationManager(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = notificationManager.getNotificationChannel(CHANNEL_ID_BEACON_DETECTED);

            if (mChannel == null)
            {
                CharSequence name = context.getString(R.string.channel_name_beacon);
                String description = context.getString(R.string.channel_description_beacon);

                mChannel = new NotificationChannel(CHANNEL_ID_BEACON_DETECTED, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.enableLights(true);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mChannel.enableLights(false);
                mChannel.setLightColor(Color.BLUE);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        //Intent carIntent = new Intent(Intent.ACTION_VIEW)
        //      .setComponent(new ComponentName(getApplicationContext(), CarService.class))
        //    .setData(Uri.fromParts(NOTIFICATION_EXTRA_BEACON, "MY_URI_HOST", beaconKey));
        //PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, carIntent,
        //      PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_BEACON_DETECTED);
        builder.setContentTitle(BeaconManager.instance.STR_BEACON_DETECTED_TITLE)       // required
                .setSmallIcon(R.drawable.ic_notif_beacon) // required
                .setContentText(BeaconManager.instance.STR_BEACON_DETECTED_MESSAGE)  // required
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(context.getString(R.string.ask_beacon_activated_desc) + " Ticker")
                .setLights(Color.BLUE, 500, 500)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .extend(
                        new CarAppExtender.Builder()
                                .setContentIntent(
                                        PendingIntent.getBroadcast(
                                                context,
                                                99955,
                                                new Intent(NOTIFICATION_EXTRA_BEACON_CAR)
                                                        .putExtra(NOTIFICATION_EXTRA_BEACON, beaconKey)
                                                        .setComponent(new ComponentName(context, AutoReceiver.class)),
                                                PendingIntent.FLAG_IMMUTABLE)).build()
                );

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    */

    private static NotificationManager getNotificationManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(NotificationManager.class);
        }
        else {
            return (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        }
    }

    public static Notification getBeaconNotification(Context context)
    {
        return BeaconManager.instance.notification;
    }

    public static BeaconManager getInstance()
    {
        return BeaconManager.instance;
    }

    public void bind(BeaconListener beaconListener)
    {
        if (beaconManagerInApp != null)
        {
            scanBeaconsListener = beaconListener;
            beaconManagerInApp.stopRangingBeacons(getRegion());
            beaconManagerInApp.startRangingBeacons(getRegion());
        }
    }

    public void unbind()
    {
        if (beaconManagerInApp != null)
        {
            beaconManagerInApp.stopRangingBeacons(getRegion());
        }
        scanBeaconsListener = null;
    }

    public Region getRegion()
    {
        Region region = new Region("AllBeaconsRegion", null, null, null);
        return region;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<org.altbeacon.beacon.Beacon> beacons, Region region)
    {
        if (beacons.size() > 0)
        {
            ArrayList<Beacon> beaconsFiltered = new ArrayList<>();
            for (org.altbeacon.beacon.Beacon beacon : beacons) {

                String uuid = beacon.getId1().toString();
                if (uuid.equalsIgnoreCase(REGION_DEVICES_1) || uuid.equalsIgnoreCase(REGION_DEVICES_2))
                {
                    beaconsFiltered.add(mapOfBeacon(beacon));
                }
            }

            if (beaconsFiltered.size() > 0)
            {
                Log.e(TAG, "didRangeBeaconsInRegion: " + beacons);

                if (scanBeaconsListener != null)
                {
                    scanBeaconsListener.onBeaconsDetected(beaconsFiltered);
                }
                else if (regionBootstrapListener != null)
                {
                    String identifier = (region.getId1() != null && region.getId1().toString() != null) ? region.getId1().toString() : "";
                    regionBootstrapListener.didEnterRegion(identifier);
                }
            }
        }
    }

    public void stopBootstrap()
    {
        /*
        Log.e(TAG, "stopBootstrap");

        ArrayList<Region> regions = getVehiclesRegions();
        if (regions.size() > 0)
        {
            Log.e(TAG, "stopMonitoring: " + regions);
            //regionBootstrap = new RegionBootstrap(this, regions);

            for (int i = 0; i < regions.size(); i++)
            {
                Region region = regions.get(i);
                beaconManager.stopMonitoring(region);
            }
        }
        */
    }

    public void startBootstrap(BeaconListener beaconListener)
    {
        startBootstrap(beaconListener, 0);
    }

    //BootstrapNotifier
    public void startBootstrap(BeaconListener beaconListener, final int retries)
    {
        int a = 0;
        /*
        regionBootstrapListener = beaconListener;
        regionIn = null;

        ArrayList<Region> regions = getVehiclesRegions();

        if (regions.size() > 0)
        {
            Log.e(TAG, "startBootstrap: " + regions);
            //regionBootstrap = new RegionBootstrap(this, regions);

            for (int i = 0; i < regions.size(); i++)
            {
                Region region = regions.get(i);
                beaconManager.stopMonitoring(region);
                beaconManager.startMonitoring(region);
            }
        }
        */
    }

    private ArrayList<Region> getVehiclesRegions()
    {
        ArrayList<Region> regions = new ArrayList<>();
        ArrayList<Vehicle> items = Core.getVehicles();
        if (items != null)
        {
            ArrayList<String> addeds = new ArrayList<>();
            for (int i = 0; i < items.size(); i++)
            {
                Vehicle vehicle = items.get(i);
                if (vehicle.beacon != null && vehicle.beacon.uuid != null && vehicle.beacon.major != null && vehicle.beacon.minor != null)
                {
                    String beaconKey = vehicle.beacon.getId();
                    if (beaconKey != null && !addeds.contains(beaconKey))
                    {
                        Identifier myBeaconNamespaceId = Identifier.parse(vehicle.beacon.major);
                        Identifier myBeaconInstanceId = Identifier.parse(vehicle.beacon.minor);
                        Region region = new Region(beaconKey, Identifier.parse(vehicle.beacon.uuid) , myBeaconNamespaceId, myBeaconInstanceId);

                        regions.add(region);
                        addeds.add(beaconKey);
                    }
                }
            }
        }

        return regions;
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
        Log.e(TAG, "Got a didDetermineStateForRegion call: " + arg1.getUniqueId() + " . status " + arg0);
    }

    @Override
    public void didExitRegion(Region arg0)
    {
        // Don't care
        Log.e(TAG, "Got a didExitRegion");
        Log.e(TAG, "Got a didExitRegion call: " + arg0.getUniqueId());
        regionIn = null;

        showNotificationBeaconSearch(context);
        //startForeground(BeaconManager.NOTIFICATION_ID, BeaconManager.getBeaconNotification(this));
        //Prefs.saveData(getApplicationContext(), Constants.KEY_SERVICE_BEACON_STARTED, "1");
    }

    @Override
    public void didEnterRegion(Region arg0) {

        Log.e(TAG, "Got a didEnterRegion");

        String beaconKey = (arg0.getId1() != null && arg0.getId1().toString() != null) ? arg0.getId1().toString() : "";
        beaconKey += (arg0.getId2() != null && arg0.getId2().toString() != null) ? "-"+arg0.getId2().toString() : "";
        beaconKey += (arg0.getId3() != null && arg0.getId3().toString() != null) ? "-"+arg0.getId3().toString() : "";

        if (regionIn == null)
        {
            regionIn = arg0;

            Log.e(TAG, "Got a didEnterRegion call: " + arg0.getUniqueId() + " -> " + beaconKey);
            // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
            // if you want the Activity to launch every single time beacons come into view, remove this call.
            //regionBootstrap.disable();

            if (regionBootstrapListener != null)
            {
                regionBootstrapListener.didEnterRegion(beaconKey);
            }
        }
        else
        {
            Log.e(TAG, "Got a didEnterRegion call diff null: " + arg0.getUniqueId() + " -> " + beaconKey);
        }
    }

    //Mapping
    private Beacon mapOfBeacon(org.altbeacon.beacon.Beacon region)
    {
        Beacon beacon = new Beacon();
        beacon.uuid = region.getId1().toString();
        beacon.major = region.getId2().toString();
        beacon.minor = region.getId3().toString();
        beacon.proximity = nameOfProximity(region.getDistance());

        // signal strength and transmission power
        beacon.rssi = region.getRssi();
        beacon.tx = region.getTxPower();

        // accuracy = rough distance estimate limited to two decimal places (in metres)
        // NO NOT ASSUME THIS IS ACCURATE - it is effected by radio interference and obstacles
        beacon.accuracy = Math.round(region.getDistance() * 100.0) / 100.0;

        return beacon;
    }

    private String nameOfProximity(double accuracy) {

        if (accuracy < 0) {
            return "ProximityUnknown";
            // is this correct?  does proximity only show unknown when accuracy is negative?  I have seen cases where it returns unknown when
            // accuracy is -1;
        }
        if (accuracy < 0.5) {
            return "ProximityImmediate";
        }
        // forums say 3.0 is the near/far threshold, but it looks to be based on experience that this is 4.0
        if (accuracy <= 4.0) {
            return "ProximityNear";
        }
        // if it is > 4.0 meters, call it far
        return "ProximityFar";
    }
}
