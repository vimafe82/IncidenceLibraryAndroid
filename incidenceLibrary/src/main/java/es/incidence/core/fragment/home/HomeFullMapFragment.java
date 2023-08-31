package es.incidence.core.fragment.home;

import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;
import com.e510.location.LocationManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.common.MapFullFragment;
import es.incidence.core.fragment.incidence.report.IncidenceReportFragment;
import es.incidence.core.manager.MapBoxManager;
import es.incidence.core.utils.view.IButton;

public class HomeFullMapFragment extends MapFullFragment
{
    private static final String KEY_VEHICLES = "KEY_VEHICLES";
    private ArrayList<Vehicle> vehicles;

    private IButton btnConfirm;
    private ImageView imageView;
    private SearchResult manualSearchResult;

    public static HomeFullMapFragment newInstance(ArrayList<Vehicle> vehicles)
    {
        HomeFullMapFragment fragment = new HomeFullMapFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KEY_VEHICLES, vehicles);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            vehicles = getArguments().getParcelableArrayList(KEY_VEHICLES);
        }
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        searchField.setVisibility(View.VISIBLE);


        int size = Utils.dpToPx(180);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);


        if (vehicles != null && vehicles.size() > 0)
        {
            boolean anyVehicleWithInsurance = false;
            boolean activeIncidence = false;
            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle v = vehicles.get(i);

                if (v.insurance != null) {
                    anyVehicleWithInsurance = true;
                }

                if (v.incidences != null)
                {
                    for (int j = 0; j < v.incidences.size(); j++)
                    {
                        Incidence incidence = v.incidences.get(j);
                        if (!incidence.isClosed() && !incidence.isCanceled())
                        {
                            activeIncidence = true;
                        }
                    }
                }
            }
            if (anyVehicleWithInsurance && !activeIncidence)
            {
                imageView = new ImageView(getContext());
                //imageView.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.home_circle1));
                imageView.setLayoutParams(params);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newReport();
                    }
                });

                ((RelativeLayout) rootView).addView(imageView);

                int duration = 500;
                int durationFade = 500;
                AnimationDrawable animation = new AnimationDrawable();
                animation.addFrame(getResources().getDrawable(R.drawable.home_circle1), duration);
                animation.addFrame(getResources().getDrawable(R.drawable.home_circle2), duration);
                animation.addFrame(getResources().getDrawable(R.drawable.home_circle3), duration);
                //animation.addFrame(getResources().getDrawable(R.drawable.home_circle4), duration);
                //animation.addFrame(getResources().getDrawable(R.drawable.home_circle5), duration);
                //animation.addFrame(getResources().getDrawable(R.drawable.home_circle4), duration);
                //animation.addFrame(getResources().getDrawable(R.drawable.home_circle3), duration);
                animation.addFrame(getResources().getDrawable(R.drawable.home_circle2), duration);
                animation.setExitFadeDuration(durationFade);
                imageView.setBackgroundDrawable(animation);
                animation.start();
            }
        }


        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(64));
        params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params2.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params2.rightMargin = Utils.dpToPx(24);
        params2.leftMargin = Utils.dpToPx(24);
        params2.bottomMargin = Utils.dpToPx(48);

        btnConfirm = new IButton(getContext());
        btnConfirm.setLayoutParams(params2);
        btnConfirm.setPrimaryColors();
        btnConfirm.setText(R.string.confirm_address);
        btnConfirm.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmManualAddress();
            }
        });
        ((RelativeLayout) rootView).addView(btnConfirm);


        enableClickPoints(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {



                Location location = new Location("");
                location.setLatitude(point.getLatitude());
                location.setLongitude(point.getLongitude());

                MapBoxManager.searchAddress(location, new SearchCallback() {
                    @Override
                    public void onResults(@NotNull List<? extends SearchResult> list, @NotNull ResponseInfo responseInfo) {
                        if (list.isEmpty()) {
                            //Log.i("SearchApiExample", "No reverse geocoding results");
                        } else {
                            SearchResult searchResult = list.get(0);
                            onSearched(searchResult);

                            Point origin = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                            drawRoute(origin, null, false);

                            printAddress(searchResult);
                        }
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                    }
                });

                return false;
            }
        });
    }

    private void newReport()
    {
        Vehicle vehicle = vehicles != null && vehicles.size() == 1 ? vehicles.get(0) : null;
        mListener.addFragmentAnimated(IncidenceReportFragment.newInstance(vehicle, false));
    }

    @Override
    public void loadData() {
        LocationManager.getLocation(getContext(), new LocationManager.LocationListener() {
            @Override
            public void onLocationResult(Location location) {
                if (location != null)
                {
                    Point origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                    drawPoint(origin);

                    MapBoxManager.searchAddress(location, new SearchCallback() {
                        @Override
                        public void onResults(@NotNull List<? extends SearchResult> list, @NotNull ResponseInfo responseInfo) {
                            if (list.isEmpty()) {
                                //Log.i("SearchApiExample", "No reverse geocoding results");
                            } else {
                                SearchResult searchResult = list.get(0);
                                printAddress(searchResult);
                            }
                        }

                        @Override
                        public void onError(@NotNull Exception e) {
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onSearched(SearchResult searchResult)
    {
        if (imageView != null)
        {
            imageView.setVisibility(View.GONE);
        }
        btnConfirm.setVisibility(View.VISIBLE);
        manualSearchResult = searchResult;
    }

    public void confirmManualAddress()
    {
        Core.manualAddressSearchResult = manualSearchResult;
        EventBus.getDefault().post(new Event(EventCode.USER_LOCATION_UPDATED));
        closeThis();
    }

    private void printAddress(SearchResult searchResult)
    {
        SearchAddress searchAddress = searchResult.getAddress();
        if (searchAddress != null)
        {
            String addr1 = searchAddress.getPostcode();
            if (addr1 == null)
                addr1 = "";
            if (searchAddress.getLocality() != null) {
                addr1 += ", " + searchAddress.getLocality();
            }
            else if (searchAddress.getPlace() != null) {
                addr1 += ", " + searchAddress.getPlace();
            }
            else if (searchAddress.getRegion() != null) {
                addr1 += ", " + searchAddress.getRegion();
            }


            String addr2 = searchAddress.getStreet();
            if (addr2 == null)
                addr2 = "";
            if (searchAddress.getHouseNumber() != null) {
                addr2 += ", " + searchAddress.getHouseNumber();
            }

            enableSearch = false;
            searchField.setText(addr2 + " " + addr1);
            enableSearch = true;
        }
    }
}
