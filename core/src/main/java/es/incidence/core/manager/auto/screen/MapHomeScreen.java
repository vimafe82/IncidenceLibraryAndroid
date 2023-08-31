package es.incidence.core.manager.auto.screen;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarLocation;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Place;
import androidx.car.app.model.PlaceListMapTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

import com.e510.commons.utils.Prefs;
import com.e510.location.LocationManager;

import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.manager.beacon.BeaconManager;

public class MapHomeScreen extends BaseScreen
{
    private Location location;

    public MapHomeScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        String titulo = getString(R.string.nombre_app);

        String beaconKey = Prefs.loadData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED, null);
        Vehicle vehicle = Core.getVehicleFromBeacon(beaconKey);
        if (vehicle != null && vehicle.insurance != null && vehicle.insurance.name != null)
        {
            titulo += " · " + vehicle.insurance.name;
        }

        ItemList.Builder builder = new ItemList.Builder();

        Row row1 = new Row.Builder().setTitle(getString(R.string.fault))
                .setBrowsable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick() {

                        Core.ANDROID_AUTO_home_option_clicked = 0;
                        Core.ANDROID_AUTO_home_option_clicked_vehicle = vehicle;
                        getScreenManager().popToRoot();
                        /*
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
                        }*/

                    }
                }).build();
        builder.addItem(row1);
        Row row2 = new Row.Builder().setTitle(getString(R.string.accident))
                .setBrowsable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        Core.ANDROID_AUTO_home_option_clicked = 1;
                        Core.ANDROID_AUTO_home_option_clicked_vehicle = vehicle;
                        getScreenManager().popToRoot();
                        /*
                        if (vehicle != null)
                        {
                            Prefs.removeData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR_CLICKED);
                            Prefs.removeData(getCarContext(), BeaconManager.NOTIFICATION_EXTRA_BEACON_CAR);

                            getScreenManager().push(new AccidentScreen(getCarContext(), vehicle));
                        }
                        else
                        {
                            getScreenManager().push(new VehicleListScreen(getCarContext(), true));
                        }*/

                    }
                }).build();
        builder.addItem(row2);

        ItemList list1 = builder.build();


        PlaceListMapTemplate.Builder builderTempplate = new PlaceListMapTemplate.Builder()
                .setItemList(list1)
                .setCurrentLocationEnabled(true)
                .setHeaderAction(Action.APP_ICON)
            .setTitle(titulo);

        if (location != null)
        {
            builderTempplate.setAnchor(new Place.Builder(CarLocation.create(location)).build());
        }
        else
        {
            LocationManager.getLocation(getCarContext(), new LocationManager.LocationListener() {
                @Override
                public void onLocationResult(Location location) {
                    MapHomeScreen.this.location = location;
                    invalidate();
                }
            });
        }

        PlaceListMapTemplate template = builderTempplate.build();


        return template;
    }
}
