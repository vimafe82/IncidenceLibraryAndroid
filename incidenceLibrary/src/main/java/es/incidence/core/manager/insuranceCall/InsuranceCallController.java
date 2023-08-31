package es.incidence.core.manager.insuranceCall;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;

import com.e510.commons.utils.DateUtils;
import com.e510.location.LocationManager;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.manager.MapBoxManager;
import es.incidence.core.utils.IUtils;
import es.incidence.core.utils.view.IBottomSheet;


public class InsuranceCallController
{
    public static void reportIncidence(Context context, InsuranceCallDelegate delegate, int incidenceId, Vehicle vehicle, IFragment baseFragment, boolean openFromNotification)
    {
        if (LocationManager.hasPermission(context))
        {
            //showHud();
            if (Core.manualAddressSearchResult != null)
            {
                Location location = new Location("");
                location.setLatitude(Core.manualAddressSearchResult.getCoordinate().latitude());
                location.setLongitude(Core.manualAddressSearchResult.getCoordinate().longitude());
                location.setAltitude(Core.manualAddressSearchResult.getCoordinate().altitude());
                location.setAccuracy(0);
                location.setSpeed(0);

                reportLocation(context, delegate, location, incidenceId, vehicle, baseFragment, openFromNotification);
            }
            else
            {
                LocationManager.getLocation(context, new LocationManager.LocationListener() {
                    @Override
                    public void onLocationResult(Location location) {

                        if (location != null)
                        {
                            reportLocation(context, delegate, location, incidenceId, vehicle, baseFragment, openFromNotification);
                        }
                        else
                        {
                            if (delegate != null)
                                delegate.onLocationErrorResult();
                        }
                    }
                });
            }
        }
        else
        {
            if (delegate != null)
                delegate.onLocationErrorResult();
        }
    }

    private static void reportLocation(Context context, InsuranceCallDelegate delegate, Location location, int incidenceId, Vehicle vehicle, IFragment baseFragment, boolean openFromNotification)
    {
        MapBoxManager.searchAddress(location, new SearchCallback() {
            @Override
            public void onResults(@NotNull List<? extends SearchResult> list, @NotNull ResponseInfo responseInfo)
            {
                String street = "";
                String city = "";
                String country = "";

                if (!list.isEmpty()) {
                    SearchResult searchResult = list.get(0);
                    SearchAddress searchAddress = searchResult.getAddress();
                    if (searchAddress != null)
                    {
                        if (searchAddress.getLocality() != null)
                        {
                            city = searchAddress.getLocality();
                        }
                        if (searchAddress.getCountry() != null)
                        {
                            country = searchAddress.getCountry();
                        }

                        String addr2 = searchAddress.getStreet();
                        if (addr2 == null)
                            addr2 = "";
                        if (searchAddress.getHouseNumber() != null) {
                            addr2 += ", " + searchAddress.getHouseNumber();
                        }

                        street = addr2;
                    }
                }

                Api.reportIncidence(new IRequestListener() {
                    @Override
                    public void onFinish(IResponse response) {
                        if (baseFragment != null)
                            baseFragment.hideHud();

                        if (response.isSuccess())
                        {
                            String ahora = DateUtils.getCurrentDate().getTimeInMillis() + "";
                            Core.saveData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE, ahora);

                            Incidence incidence = (Incidence) response.get("incidence", Incidence.class);
                            vehicle.incidences.add(incidence);
                            Core.saveVehicle(vehicle);

                            EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                            onSuccessReport(context, delegate, incidence, vehicle, baseFragment);
                        }
                        else
                        {
                            if (delegate != null)
                                delegate.onBadResponseReport(response);
                        }
                    }
                }, vehicle.licensePlate, incidenceId+"", street, city, country, location, openFromNotification);
            }

            @Override
            public void onError(@NotNull Exception e) {
                if (delegate != null)
                    delegate.onLocationErrorResult();
            }
        });
    }



    private static void onSuccessReport(Context context, InsuranceCallDelegate delegate, Incidence incidence, Vehicle vehicle, IFragment baseFragment)
    {
        Log.e("ERROR", "onSuccessReport");
        if (baseFragment != null)
        {
            Log.e("ERROR", "onSuccessReport1");
            if (incidence != null && incidence.openApp != null)
            {
                Log.e("ERROR", "onSuccessReport2");
                EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                if (delegate != null)
                    delegate.onSuccessReportToCall(incidence);

                Core.startNewApp(context, incidence.openApp.androidPackage, incidence.openApp.androidDeeplink, incidence.openApp.androidGooglePlayURL);
            }
            else if (incidence != null && incidence.asitur != null)
            {
                Log.e("ERROR", "onSuccessReport3");
                EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                if (delegate != null)
                    delegate.onSuccessReport(incidence);
            }
            else if (vehicle != null && vehicle.insurance != null)
            {
                Log.e("ERROR", "onSuccessReport4");
                //llamamos a la aseguradora
                locateInsuranceCallPhone(context, vehicle, new LocationCallInsuranceListener() {
                    @Override
                    public void onGetPhone(String phone)
                    {
                        Log.e("ERROR", "onSuccessReport5");
                        if (phone != null && phone.length() > 0)
                        {
                            Log.e("ERROR", "onSuccessReport6");
                            //Core.callPhone(phone);
                            String[] items = {context.getString(R.string.call_to, phone), context.getString(R.string.cancel)};
                            int[] icons = {R.drawable.ic_call_phone, R.drawable.transparent};

                            IBottomSheet.Builder builder = new IBottomSheet.Builder(context);
                            builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {

                                        EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                                        if (delegate != null)
                                            delegate.onSuccessReportToCall(incidence);

                                        Core.callPhone(phone, true);
                                    }
                                    else if (which == 1)
                                    {
                                        /* Ya no cancelamos para igualar a iOS.
                                        //Cancelamos incidencia
                                        if (baseFragment != null)
                                            baseFragment.showHud();
                                        Api.closeIncidence(new IRequestListener() {
                                            @Override
                                            public void onFinish(IResponse response)
                                            {
                                                if (baseFragment != null)
                                                    baseFragment.hideHud();

                                                if (response.isSuccess())
                                                {
                                                    incidence.close();

                                                    EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                                                    if (delegate != null)
                                                        delegate.onSuccessReport(incidence);
                                                }
                                                else
                                                {
                                                    if (baseFragment != null)
                                                        baseFragment.onBadResponse(response);
                                                }
                                            }
                                        }, incidence.id);
                                       */

                                        EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                                        if (delegate != null)
                                            delegate.onSuccessReport(incidence);
                                    }
                                }
                            });
                            IBottomSheet bottomSheet = builder.create();
                            bottomSheet.setCancelable(false);
                            bottomSheet.setCanceledOnTouchOutside(false);

                            //no funcionan los set cancelables a false cuando pulsan fuera y por eso cremos Ibottomsheet

                            bottomSheet.show();
                        }
                        else
                        {
                            Log.e("ERROR", "onSuccessReport8");
                            EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                            if (delegate != null)
                                delegate.onSuccessReport(incidence);
                        }
                    }
                });
            }
            else
            {
                if (delegate != null)
                    delegate.onSuccessReport(incidence);
            }
        }
        else if (delegate != null)
            delegate.onSuccessReport(incidence);
    }


    public interface LocationCallInsuranceListener
    {
        void onGetPhone(String phone);
    }
    public static void locateInsuranceCallPhone(Context context, Vehicle vehicle, LocationCallInsuranceListener listener)
    {
        Log.e("ERROR", "locateInsuranceCallPhone");
        if (Core.manualAddressSearchResult != null)
        {
            Log.e("ERROR", "locateInsuranceCallPhone1");
            Location location = new Location("");
            location.setLatitude(Core.manualAddressSearchResult.getCoordinate().latitude());
            location.setLongitude(Core.manualAddressSearchResult.getCoordinate().longitude());

            String phone = "";
            boolean inSpain = true;
            if (location != null)
            {
                inSpain = IUtils.isLocationInSpain(context, location);
            }

            if (inSpain)
            {
                if (vehicle.insurance.phone != null)
                    phone = vehicle.insurance.phone;
            }
            else
            {
                if (vehicle.insurance.internationaPhone != null)
                    phone = vehicle.insurance.internationaPhone;
            }

            if (listener != null)
                listener.onGetPhone(phone);
        }
        else
        {
            Log.e("ERROR", "locateInsuranceCallPhone2");
            LocationManager.getLocation(context, new LocationManager.LocationListener() {
                @Override
                public void onLocationResult(Location location)
                {
                    Log.e("ERROR", "locateInsuranceCallPhone3");
                    String phone = "";
                    boolean inSpain = true;
                    if (location != null)
                    {
                        inSpain = IUtils.isLocationInSpain(context, location);
                    }

                    if (inSpain)
                    {
                        if (vehicle.insurance.phone != null)
                            phone = vehicle.insurance.phone;
                    }
                    else
                    {
                        if (vehicle.insurance.internationaPhone != null)
                            phone = vehicle.insurance.internationaPhone;
                    }
                    Log.e("ERROR", "locateInsuranceCallPhone4");
                    if (listener != null)
                        listener.onGetPhone(phone);
                }
            });
        }
    }
}
