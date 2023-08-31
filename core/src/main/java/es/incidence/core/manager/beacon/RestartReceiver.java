package es.incidence.core.manager.beacon;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.car.app.CarContext;

import es.incidence.core.Core;
import es.incidence.core.IApplication;
import es.incidence.core.manager.auto.CarService;

public class RestartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.e("RestartReceiver", "onRestartReciever");
        if (IApplication.INSTANCE != null && IApplication.INSTANCE.isApplicationOnBackground() && Core.getUser() != null)
        //if (Core.getUser() != null)
        {
            try
            {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                } else if (!mBluetoothAdapter.isEnabled()) {
                    // Bluetooth is not enabled :)
                } else {
                    // Bluetooth is enabled
                    Intent serviceIntent = new Intent(context, BeaconService.class);
                    // Paramos el servicio si estuviera arrancado
                    context.stopService(serviceIntent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Correct If
                        context.startForegroundService(serviceIntent); // Correct If
                    } else {
                        context.startService(serviceIntent);
                    }
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }
}
