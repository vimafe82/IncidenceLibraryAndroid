package es.incidence.core.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.e510.commons.BaseApplication;
import com.e510.commons.utils.Prefs;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.manager.beacon.BeaconManager;

public class ISplashActivity extends IActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null)
        {
            String open = intent.getExtras().getString(BeaconManager.NOTIFICATION_EXTRA_BEACON, null);
            if (open != null && open.length() > 0)
            {
                Prefs.saveData(this, Constants.NOTIFICATION_PUSH_ACTION_OPEN_INCIDENCE, open);
            }
            else {
                String action = intent.getExtras().getString("action", null);
                if (action != null && action.equals(Constants.NOTIFICATION_PUSH_ACTION_OPEN_INCIDENCE)) {
                    String vehicleId = intent.getExtras().getString("vehicleId", null);
                    if (vehicleId != null)
                    {
                        Vehicle vehicle = Core.getVehicle(vehicleId);
                        if (vehicle != null && vehicle.beacon != null)
                        {
                            Prefs.saveData(this, Constants.NOTIFICATION_PUSH_ACTION_OPEN_INCIDENCE, vehicle.beacon.getId());
                        }
                    }
                }/*
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Log.e("xavi", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                    }
                }*/
            }
        }

        Core.updateLiterals(false);

        //Go to content
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (!BaseApplication.INSTANCE.isApplicationOnBackground())
                    goToContent();
            }
        }, 2000);
    }

    private void goToContent()
    {
        Core.startApp(this);
    }
}
