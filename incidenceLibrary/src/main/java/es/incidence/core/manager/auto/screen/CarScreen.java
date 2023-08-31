package es.incidence.core.manager.auto.screen;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;

import com.e510.commons.utils.Prefs;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.User;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.manager.beacon.BeaconManager;
import es.incidence.core.utils.view.INotification;

public class CarScreen extends BaseScreen
{
    public CarScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        //int blue = Utils.getColor(getCarContext(), R.color.incidence500);
        //int red = Utils.getColor(getCarContext(), R.color.error100);

        if (Core.ANDROID_AUTO_vehicle != null)
        {
            MessageTemplate template = new MessageTemplate.Builder(getString(R.string.loading))
                    .setTitle(getString(R.string.nombre_app))
                    .setIcon(
                            new CarIcon.Builder(
                                    IconCompat.createWithResource(
                                            getCarContext(),
                                            R.drawable.loading))
                                    .setTint(CarColor.BLUE)
                                    .build())
                    .build();

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    getScreenManager().push(new InsuranceCallingScreen(getCarContext(), Core.ANDROID_AUTO_incidenceId, Core.ANDROID_AUTO_vehicle));
                }
            }, 1000);

            return template;
        }

        //Chequeamos si hay incidencia abierta.
        User user = Core.getUser();
        Incidence activeIncidence = null;
        Vehicle vehicleActiveIncidence = null;
        ArrayList<Vehicle> list = Core.getVehicles();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Vehicle v = list.get(i);
                if (v.incidences != null)
                {
                    for (int j = 0; j < v.incidences.size(); j++)
                    {
                        Incidence incidence = v.incidences.get(j);
                        if (!incidence.isClosed() && !incidence.isCanceled())
                        {
                            //chequeamos que la incidencia esté reportada por el usuario. Ponemos la condicion de si es null porque al reportar devuelve la incidence sin reporter
                            if (incidence.reporter == null || (incidence.reporter != null && user.id != null && incidence.reporter == Integer.parseInt(user.id)))
                            {
                                activeIncidence = incidence;
                                vehicleActiveIncidence = v;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (activeIncidence != null)
        {
            final Incidence incidence = activeIncidence;
            final Vehicle vehicle = vehicleActiveIncidence;

            Action action1incidence = new Action.Builder()
                    .setTitle(getString(R.string.close_incidence))
                    //.setBackgroundColor(CarColor.createCustom(blue, blue))
                    .setBackgroundColor(CarColor.BLUE)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick()
                        {
                            Api.closeIncidence(new IRequestListener() {
                                @Override
                                public void onFinish(IResponse response)
                                {
                                    if (response.isSuccess())
                                    {
                                        Core.removeData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE);

                                        incidence.close();

                                        for (int j = 0; j < vehicle.incidences.size(); j++)
                                        {
                                            Incidence in = vehicle.incidences.get(j);
                                            if (in.id == incidence.id)
                                            {
                                                in.close();
                                                break;
                                            }
                                        }
                                        Core.saveVehicle(vehicle);

                                        EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                                        invalidate();
                                    }
                                }
                            }, incidence.id);
                        }
                    }).build();

            Action action2incidence = new Action.Builder()
                    .setTitle(getString(R.string.cancel_incidence))
                    //.setBackgroundColor(CarColor.createCustom(red, red))
                    .setBackgroundColor(CarColor.BLUE)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick()
                        {
                            //Cancelamos incidencia
                            Api.cancelIncidence(new IRequestListener() {
                                @Override
                                public void onFinish(IResponse response)
                                {
                                    if (response.isSuccess())
                                    {
                                        Core.removeData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE);

                                        incidence.cancel();

                                        for (int j = 0; j < vehicle.incidences.size(); j++)
                                        {
                                            Incidence in = vehicle.incidences.get(j);
                                            if (in.id == incidence.id)
                                            {
                                                in.cancel();
                                                break;
                                            }
                                        }
                                        Core.saveVehicle(vehicle);

                                        EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                                        Core.cleanAndroidAuto();
                                        invalidate();
                                    }
                                }
                            }, incidence.id);
                        }
                    }).build();

            MessageTemplate templateIncidence = new MessageTemplate.Builder(getString(R.string.your_incidence_reported))
                    .setTitle(getString(R.string.nombre_app))
                    /*.setIcon(
                            new CarIcon.Builder(
                                    IconCompat.createWithResource(
                                            getCarContext(),
                                            R.mipmap.ic_launcher))
                                    .setTint(CarColor.BLUE)
                                    .build())*/
                    .addAction(action1incidence)
                    .addAction(action2incidence)
                    .build();


            return templateIncidence;
        }


        Action action1 = new Action.Builder()
                .setTitle(getString(R.string.fault))
                //.setBackgroundColor(CarColor.createCustom(blue, blue))
                .setBackgroundColor(CarColor.BLUE)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        String beaconKey = Prefs.loadData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED, null);
                        Vehicle vehicle = Core.getVehicleFromBeacon(beaconKey);

                        if (vehicle != null)
                        {
                            Prefs.removeData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED);
                            Prefs.removeData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR);

                            int parent = 2; //Avería es 2
                            getScreenManager().push(new FaultListScreen(getCarContext(), parent, vehicle));
                        }
                        else
                        {
                            getScreenManager().push(new VehicleListScreen(getCarContext(), false));
                        }
                    }
                }).build();

        Action action2 = new Action.Builder()
                .setTitle(getString(R.string.accident))
                //.setBackgroundColor(CarColor.createCustom(red, red))
                .setBackgroundColor(CarColor.RED)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        String beaconKey = Prefs.loadData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED, null);
                        Vehicle vehicle = Core.getVehicleFromBeacon(beaconKey);

                        if (vehicle != null)
                        {
                            Prefs.removeData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED);
                            Prefs.removeData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR);

                            getScreenManager().push(new AccidentScreen(getCarContext(), vehicle));
                        }
                        else
                        {
                            getScreenManager().push(new VehicleListScreen(getCarContext(), true));
                        }
                    }
                }).build();

        MessageTemplate template = new MessageTemplate.Builder(getString(R.string.report_ask_what))
                .setTitle(getString(R.string.nombre_app))
                /*.setIcon(
                        new CarIcon.Builder(
                                IconCompat.createWithResource(
                                        getCarContext(),
                                        R.mipmap.ic_launcher))
                                .setTint(CarColor.BLUE)
                                .build())*/
                .addAction(action1)
                .addAction(action2)
                .build();


        return template;
    }
}