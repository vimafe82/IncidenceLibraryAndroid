package es.incidence.core.manager.auto;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.car.app.CarContext;

import com.e510.commons.utils.Prefs;

import es.incidence.core.manager.beacon.BeaconManager;

public class AutoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String intentAction = intent.getAction();

        if (BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR.equals(intentAction))
        {
            Log.e("AutoReceiver", "onReceive");

            String beaconKey = Prefs.loadData(context, BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR, null);
            if (beaconKey != null)
            {
                Prefs.saveData(context, BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED, beaconKey);
            }

            CarContext.startCarApp(
                    intent,
                    new Intent(Intent.ACTION_VIEW)
                            .setComponent(new ComponentName(context, CarService.class))
                            .setData(Uri.fromParts("MY_URI_SCHEME", "MY_URI_HOST", intentAction)));
        }
    }
}