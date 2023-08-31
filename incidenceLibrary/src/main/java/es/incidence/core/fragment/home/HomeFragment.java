package es.incidence.core.fragment.home;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.e510.commons.activity.BaseActivity;
import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.fragment.FragmentAnimation;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Prefs;
import com.e510.commons.utils.Utils;
import com.e510.location.LocationManager;
import com.mapbox.geojson.Point;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.HomeNoticesAdapter;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Notification;
import es.incidence.core.domain.User;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.account.profile.ProfileEditFragment;
import es.incidence.core.fragment.add.AddBeaconFragment;
import es.incidence.core.fragment.add.AddVehicleFragment;
import es.incidence.core.fragment.common.MapBoxFragment;
import es.incidence.core.fragment.incidence.IncidenceDetailFragment;
import es.incidence.core.fragment.incidence.report.IncidenceReportFragment;
import es.incidence.core.fragment.incidence.report.IncidenceValorationFragment;
import es.incidence.core.fragment.incidence.report.ReportMapFragment;
import es.incidence.core.fragment.vehicle.VehicleDataFragment;
import es.incidence.core.fragment.vehicle.VehicleInsuranceFragment;
import es.incidence.core.fragment.vehicle.VehicleOptionsFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.manager.ImageManager;
import es.incidence.core.manager.MapBoxManager;
import es.incidence.core.manager.SpeechManager;
import es.incidence.core.manager.insuranceCall.InsuranceCallController;
import es.incidence.core.utils.FolderListView;
import es.incidence.core.utils.view.INotification;

public class HomeFragment extends MapBoxFragment
{
    private static final String TAG = makeLogTag(HomeFragment.class);
    private static final int REQUEST_CODE = 112;

    private RelativeLayout layoutRootHome;
    private TextView homeTitleSmall;
    private TextView homeTitleBig;
    private RelativeLayout layoutLocationDisabled;
    private RelativeLayout layoutVehicle;
    private ImageView arrowVehicle;
    private ImageView imgVehicle;
    private ImageView imgVehicleBeacon;
    private TextView txtVehicleMatricula;
    private TextView txtVehicleName;
    private FolderListView listViewNotices;
    private ImageView homeCircle;
    private ImageView imgIncidenceActive;
    private ImageView microButton;

    private HomeNoticesAdapter noticesAdapter;
    private ArrayList<Notification> notifications;
    private ArrayList<Vehicle> vehicles;
    private Vehicle vehicleActiveIncidence;
    private Incidence activeIncidence;
    private boolean userClickedToActivateLocation;

    private CardView alertVolumeErrorContainer;
    private TextView alertVolumeErrorTitle;
    private TextView alertVolumeErrorSubTitle;
    private ImageView alertVolumeErrorImgClose;

    public static HomeFragment newInstance()
    {
        HomeFragment fragment = new HomeFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootHome;
    }

    @Override
    public boolean needEventBus() {
        return true;
    }

    @Override
    public boolean isMoveDisabled() {
        return true;
    }

    @Override
    public void onBecomeFrontFromBackPressed() {
        super.onBecomeFrontFromBackPressed();
        EventBus.getDefault().post(new Event(EventCode.HOME_MENU_UNLOCK));
        setUpSpeechButton();
        setUpVolumeAlert();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupUI(view);
        overrideOnCreateView(view, savedInstanceState);

        return view;
    }


    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        View imgHamburgerClick = rootView.findViewById(R.id.imgHamburgerClick);
        imgHamburgerClick.setOnClickListener(getBaseActivity());

        layoutRootHome = rootView.findViewById(R.id.layoutRootHome);
        homeTitleSmall = rootView.findViewById(R.id.homeTitleSmall);
        homeTitleBig = rootView.findViewById(R.id.homeTitleBig);
        FontUtils.setTypeValueText(homeTitleBig, Constants.FONT_SEMIBOLD, getContext());

        layoutVehicle = rootView.findViewById(R.id.layoutVehicle);
        layoutVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickLayoutVehicles();
            }
        });
        rootView.findViewById(R.id.viewHack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickLayoutVehicles();
            }
        });

        arrowVehicle = layoutVehicle.findViewById(R.id.imgRight);
        imgVehicle = layoutVehicle.findViewById(R.id.imgVehicle);
        imgVehicleBeacon = layoutVehicle.findViewById(R.id.imgCheck);
        txtVehicleMatricula = layoutVehicle.findViewById(R.id.txtMatricula);
        txtVehicleName = layoutVehicle.findViewById(R.id.txtName);
        FontUtils.setTypeValueText(txtVehicleMatricula, Constants.FONT_SEMIBOLD, getContext());
        FontUtils.setTypeValueText(txtVehicleName, Constants.FONT_SEMIBOLD, getContext());

        //---
        alertVolumeErrorContainer = layoutRootHome.findViewById(R.id.alertVolumeErrorContainer);
        alertVolumeErrorTitle = layoutRootHome.findViewById(R.id.alertVolumeErrorTitle);
        alertVolumeErrorSubTitle = layoutRootHome.findViewById(R.id.alertVolumeErrorSubTitle);
        alertVolumeErrorImgClose = layoutRootHome.findViewById(R.id.alertVolumeErrorImgClose);
        FontUtils.setTypeValueText(alertVolumeErrorTitle, Constants.FONT_SEMIBOLD, getContext());
        FontUtils.setTypeValueText(alertVolumeErrorSubTitle, Constants.FONT_REGULAR, getContext());
        alertVolumeErrorImgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    microButtonClick();
                }
            }
        );
        //---

        layoutLocationDisabled = rootView.findViewById(R.id.layoutLocationDisabled);
        layoutLocationDisabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLocationPopUp();
            }
        });
        listViewNotices = rootView.findViewById(R.id.listView);
        notifications = new ArrayList<>();
        noticesAdapter = new HomeNoticesAdapter(this, notifications);
        listViewNotices.setAdapter(noticesAdapter);

        imgIncidenceActive = rootView.findViewById(R.id.imgIncidenceActive);
        microButton = rootView.findViewById(R.id.microButton);
        String lang = Core.getLanguage();
        //if (!lang.equals("es") && !lang.equals("ca") && !lang.equals("eu") && !lang.equals("gl"))
        if (lang.equals("en"))
        {
            imgIncidenceActive.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.incidence_active_en));
        }
        imgIncidenceActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goToActiveIncidence();
                showCloseIncidenceActive();
            }
        });
        homeCircle = rootView.findViewById(R.id.homeCircle);

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
        homeCircle.setBackgroundDrawable(animation);
        animation.start();

        homeCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newReport();
            }
        });

        rootView.findViewById(R.id.layoutMapClicable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addFragmentAnimated(HomeFullMapFragment.newInstance(vehicles));
            }
        });

        setUpSpeechButton();
        setUpVolumeAlert();
        microButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                microButtonClick();
            }
        });
    }

    private void microButtonClick() {
        SpeechManager.isEnabled = !SpeechManager.isEnabled;
        if (SpeechManager.isEnabled) {
            if (SpeechManager.hasRecordAudioPermission(getContext())) {
                SpeechManager.isEnabled = true;
                setUpSpeechButton();
                setUpVolumeAlert();
            } else {
                SpeechManager.requestPermission(Constants.PERMISSION_RECORD_AUDIO_REQUEST_CODE, getBaseActivity());
            }
        } else {
            SpeechManager.isEnabled = false;
            setUpSpeechButton();
            setUpVolumeAlert();
        }
    }

    private void setUpSpeechButton() {
        if (SpeechManager.isEnabled) {
            microButton.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.ic_home_mircro_on));
        } else {
            microButton.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.ic_home_mircro_off));
        }
    }

    private void setUpVolumeAlert() {
        if (SpeechManager.isEnabled) {
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int minVolumeNeeded = maxVolume / 4;

            boolean ringerModeError = false;
            /*if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT || audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                ringerModeError = true;
            }*/

            if (volume < minVolumeNeeded || ringerModeError) {
                alertVolumeErrorContainer.setVisibility(View.VISIBLE);
            } else {
                alertVolumeErrorContainer.setVisibility(View.GONE);
            }
        } else {
            alertVolumeErrorContainer.setVisibility(View.GONE);
        }
    }

    private void onClickLayoutVehicles()
    {
        if (vehicles != null && vehicles.size() == 1)
        {
            Vehicle vehicle = vehicles.get(0);
            mListener.addFragmentAnimated(VehicleDataFragment.newInstance(vehicle));
        }
        else if (activeIncidence == null)
        {
            showVehiclesActionSheet();
        }
    }

    private void goToActiveIncidence()
    {
        mListener.addFragmentAnimated(IncidenceDetailFragment.newInstance(vehicleActiveIncidence, activeIncidence));
    }

    private void newReport()
    {
        //displaySpeechRecognizer();

        if (Api.hasConnection(getContext()))
        {
            Vehicle vehicle = vehicles != null && vehicles.size() == 1 ? vehicles.get(0) : null;
            //Vehicle vehicleTmp = vehicles != null && vehicles.size() > 0 ? vehicles.get(0) : null;
            Vehicle vehicleTmp = getFavoriteVehicle();
            mListener.addFragmentAnimated(IncidenceReportFragment.newInstance(vehicle, vehicleTmp, false));
        }
        else
        {
            showNoConnectionToReportPopUp();
        }
    }

    private void showNoConnectionToReportPopUp()
    {
        hideKeyboard();

        String title = getString(R.string.alert_no_internet);;
        String message = getString(R.string.alert_no_internet_message);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.retry));
        options.add(getString(R.string.call_my_insurance));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //retry
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                newReport();
                            }
                        }, 500);

                    }
                    else if (index == 1)
                    {
                        //call_my_insurance
                        Vehicle vehicle = getFavoriteVehicle();
                        InsuranceCallController.locateInsuranceCallPhone(getContext(), vehicle, new InsuranceCallController.LocationCallInsuranceListener() {
                            @Override
                            public void onGetPhone(String phone)
                            {
                                if (phone != null && phone.length() > 0)
                                {
                                    Core.callPhone(phone);
                                }
                            }
                        });
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    @Override
    public void loadData()
    {
        boolean called = activateCall();
        if (!called) {
            activateLocation();
        }

        loadNotificationsAndVehicles(true);
    }

    private void checkOpenFromNotification()
    {
        String openAppFromNotificationBeacon = Prefs.loadData(getContext(), Constants.NOTIFICATION_PUSH_ACTION_OPEN_INCIDENCE);
        if (openAppFromNotificationBeacon != null && imgIncidenceActive.getVisibility() == View.GONE)
        {
            //No usamos vehicles porque todavía no se habrá actualizado el WS
            Vehicle vehicle = Core.getVehicleFromBeacon(openAppFromNotificationBeacon);

            if (vehicle != null && vehicle.insurance != null)
            {
                mListener.cleanAllBackStackEntries();
                mListener.addFragment(R.id.fragment_container, IncidenceReportFragment.newInstance(vehicle, true), false, true, FragmentAnimation.PUSH);
            }
        }
    }

    private void loadNotificationsAndVehicles(boolean checkOpen)
    {
        layoutVehicle.setVisibility(View.GONE);
        homeCircle.setVisibility(View.GONE);
        imgIncidenceActive.setVisibility(View.GONE);
        microButton.setVisibility(View.GONE);
        INotification.shared(getContext()).removeFrom(layoutRootHome);

        showHud();
        Api.getNotifications(new IRequestListener() {
            @Override
            public void onFinish(IResponse response)
            {
                if (response.isSuccess())
                {
                    ArrayList<Notification> temp = response.getList("notifications", Notification.class);
                    addNotifications(temp);

                    Api.getVehicles(new IRequestListener() {
                        @Override
                        public void onFinish(IResponse response)
                        {
                            hideHud();
                            if (response.isSuccess())
                            {
                                vehicles = response.getList("vehicles", Vehicle.class);
                                reloadDataVehicles();

                                if (checkOpen)
                                {
                                    checkOpenFromNotification();
                                }
                            }
                            else
                            {
                                onBadResponse(response);
                            }
                        }
                    });
                }
                else
                {
                    hideHud();
                    onBadResponse(response);
                }
            }
        });
    }

    private void loadNotifications()
    {
        showHud();
        Api.getNotifications(new IRequestListener() {
            @Override
            public void onFinish(IResponse response)
            {
                hideHud();
                if (response.isSuccess())
                {
                    ArrayList<Notification> temp = response.getList("notifications", Notification.class);
                    addNotifications(temp);
                }
                else
                {
                    onBadResponse(response);
                }
            }
        });
    }

    private Vehicle getFavoriteVehicle()
    {
        String idVehicleDefault = Core.loadData(Constants.KEY_USER_DEFAULT_VEHICLE_ID);
        Vehicle vehicle = getVehicle(idVehicleDefault);

        if (vehicle == null && vehicles != null && vehicles.size() > 0) {
            vehicle = vehicles.get(0);
        }

        return vehicle;
    }

    private Vehicle getVehicle(String idVehicle)
    {
        Vehicle vehicle = null;

        if (vehicles != null && vehicles.size() > 0 && idVehicle != null) {
            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle v = vehicles.get(i);
                if (idVehicle != null && idVehicle.equals(v.id)) {
                    vehicle = v;
                    break;
                }
            }
        }

        return vehicle;
    }

    private void reloadDataVehicles()
    {
        if (vehicles != null && vehicles.size() > 0)
        {
            layoutVehicle.setVisibility(View.VISIBLE);
            //homeCircle.setVisibility(View.VISIBLE);
            imgIncidenceActive.setVisibility(View.GONE);
            Vehicle vehicle = getFavoriteVehicle();
            activeIncidence = null;
            vehicleActiveIncidence = null;

            boolean anyVehicleWithInsurance = false;
            User user = Core.getUser();

            for (int i = 0; i < vehicles.size(); i++)
            {
                Vehicle v = vehicles.get(i);

                if (v.insurance != null)
                {
                    anyVehicleWithInsurance = true;
                }

                if (v.incidences != null)
                {
                    for (int j = 0; j < v.incidences.size(); j++)
                    {
                        Incidence incidence = v.incidences.get(j);
                        if (!incidence.isClosed() && !incidence.isCanceled())
                        {
                            //chequeamos que la incidencia esté reportada por el usuario
                            if (incidence.reporter != null && user.id != null && incidence.reporter == Integer.parseInt(user.id))
                            {
                                activeIncidence = incidence;
                                vehicleActiveIncidence = v;
                                break;
                            }
                        }
                    }
                }
                if (activeIncidence != null)
                {
                    break;
                }
            }

            if (anyVehicleWithInsurance)
            {
                homeCircle.setVisibility(View.VISIBLE);
                microButton.setVisibility(View.VISIBLE);
            }
            else
            {
                homeCircle.setVisibility(View.GONE);
                microButton.setVisibility(View.GONE);
            }

            if (activeIncidence != null)
            {
                imgIncidenceActive.setVisibility(View.VISIBLE);
                homeCircle.setVisibility(View.GONE);
                microButton.setVisibility(View.GONE);
                //ya no se muestra automaticamente sino al clicar en incidencia activa
                //showCloseIncidenceActive();
                reloadDataBottomVehicle(vehicleActiveIncidence);
            }
            else
            {
                reloadDataBottomVehicle(vehicle);
            }

            if (vehicles.size() == 1)
            {
                arrowVehicle.setRotation(-90f);
            }
            else
            {
                arrowVehicle.setRotation(0f);
            }
        }
        else
        {
            layoutVehicle.setVisibility(View.GONE);
            homeCircle.setVisibility(View.GONE);
            imgIncidenceActive.setVisibility(View.GONE);
            microButton.setVisibility(View.GONE);
            activeIncidence = null;
            vehicleActiveIncidence = null;
        }
    }

    private void reloadDataBottomVehicle(Vehicle vehicle)
    {
        if (vehicle.image != null)
        {
            ImageManager.loadImage(getContext(), vehicle.image, imgVehicle);
        }
        else
        {
            imgVehicle.setImageDrawable(null);
        }
        txtVehicleMatricula.setText(vehicle.licensePlate);
        txtVehicleName.setText(vehicle.getName());

        if (vehicle.beacon != null)
        {
            imgVehicleBeacon.setVisibility(View.VISIBLE);
        }
        else
        {
            imgVehicleBeacon.setVisibility(View.GONE);
        }
    }

    private void addNotifications(ArrayList<Notification> temp)
    {
        notifications.clear();
        notifications.addAll(temp);
        noticesAdapter.notifyDataSetChanged();
    }

    private void showCloseIncidenceActive()
    {
        if (activeIncidence != null)
        {
            if (activeIncidence.asitur != null)
            {
                BaseFragment baseFragment = mListener.getActiveFragment();
                if (baseFragment == null || baseFragment instanceof HomeFragment)
                {
                    mListener.addFragmentAnimated(ReportMapFragment.newInstance(vehicleActiveIncidence, activeIncidence));
                }
            }
            else
            {
                String title = getString(R.string.your_incidence_reported);
                String message = getString(R.string.your_incidence_reported_desc);
                String titleButton = getString(R.string.close_incidence);
                String titleButtonCancel = getString(R.string.cancel_incidence);

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        showHud();
                        Api.closeIncidence(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response)
                            {
                                hideHud();
                                if (response.isSuccess())
                                {
                                    Core.removeData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE);

                                    activeIncidence.close();

                                    //El listener para que se espere a la animación del hide
                                    INotification.shared(getContext()).hide(imgIncidenceActive, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showRateIncidenceActive();
                                        }
                                    });
                                }
                                else
                                {
                                    onBadResponse(response);
                                }
                            }
                        }, activeIncidence.id);
                    }
                };

                View.OnClickListener listenerCancel = new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        showHud();
                        Api.cancelIncidence(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response)
                            {
                                if (response.isSuccess())
                                {
                                    Core.removeData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE);

                                    INotification.shared(getContext()).hide();
                                    imgIncidenceActive.setVisibility(View.GONE);
                                    microButton.setVisibility(View.GONE);
                                    homeCircle.setVisibility(View.VISIBLE);
                                    microButton.setVisibility(View.VISIBLE);
                                    loadNotificationsAndVehicles(false);
                                }
                                else
                                {
                                    hideHud();
                                    onBadResponse(response);
                                }
                            }
                        }, activeIncidence.id);
                    }
                };

                RelativeLayout layoutToShow = layoutRootHome;//getBaseActivity().findViewById(R.id.mainBaseLayout);
                INotification.shared(getContext()).showNotification(layoutToShow, title, message, titleButton, titleButtonCancel, listener, listenerCancel, true);
            }
        }
    }

    private void showRateIncidenceActive()
    {
        String title = getString(R.string.incidence_wish_good);
        String message = getString(R.string.incidence_wish_good_desc);
        String titleButton = getString(R.string.make_valoration);
        String titleCancelButton = getString(R.string.later);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                INotification.shared(getContext()).hide();
                imgIncidenceActive.setVisibility(View.GONE);
                homeCircle.setVisibility(View.VISIBLE);
                microButton.setVisibility(View.VISIBLE);
                loadNotificationsAndVehicles(false);

                mListener.addFragmentAnimated(IncidenceValorationFragment.newInstance(activeIncidence));
            }
        };

        View.OnClickListener listenerCancel = new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                INotification.shared(getContext()).hide();
                imgIncidenceActive.setVisibility(View.GONE);
                homeCircle.setVisibility(View.VISIBLE);
                microButton.setVisibility(View.VISIBLE);
                loadNotificationsAndVehicles(false);
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showNotification(layoutToShow, title, message, titleButton, titleCancelButton, listener, listenerCancel, false);
    }

    private void showLocationPopUp()
    {
        String title = getString(R.string.activate_location_title);
        String message = getString(R.string.activate_location_message);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.activate_location));
        options.add(getString(R.string.add_manually));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //Activar ubicación
                        userClickedToActivateLocation = true;
                        activateLocation();
                    }
                    else if (index == 1)
                    {
                        //Introducir manualmente
                        mListener.addFragmentAnimated(HomeFullMapFragment.newInstance(vehicles));
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, listener);
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof Notification)
        {
            Notification notification = (Notification) object;

            if (notification.status != Constants.NOTIFICATION_STATUS_READ)
            {
                Api.updateNotificationStatus(notification, Constants.NOTIFICATION_STATUS_READ, new IRequestListener() {
                    @Override
                    public void onFinish(IResponse response)
                    {
                        if (response.isSuccess())
                        {
                            notification.status = Constants.NOTIFICATION_STATUS_READ;
                            noticesAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            onBadResponse(response);
                        }
                    }
                });
            }

            if (notification.action != null)
            {
                /*
                if (notification.action.equals(Constants.NOTIFICATION_ACTION_ADD_VEHICLE) || notification.action.equals(Constants.NOTIFICATION_ACTION_ADD_BEACON))
                {
                    mListener.addFragmentAnimated(AddFragment.newInstance());
                }*/
                if (notification.action.equals(Constants.NOTIFICATION_ACTION_OPEN_USER))
                {
                    mListener.addFragmentAnimated(ProfileEditFragment.newInstance());
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_ADD_VEHICLE))
                {
                    mListener.addFragmentAnimated(AddVehicleFragment.newInstance(false));
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_ADD_BEACON))
                {
                    Vehicle vehicle = null;
                    if (notification.vehicleId != null) {
                        vehicle = getVehicle(notification.vehicleId);
                    }
                    mListener.addFragmentAnimated(AddBeaconFragment.newInstance(0, 1, vehicle,true));
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_OPEN_POLICY_ID))
                {
                    Vehicle vehicle = getVehicle(notification.vehicleId);
                    if (vehicle != null)
                    {
                        mListener.addFragmentAnimated(VehicleInsuranceFragment.newInstance(vehicle));
                    }
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_OPEN_VEHICLE_ID))
                {
                    Vehicle vehicle = getVehicle(notification.vehicleId);
                    if (vehicle != null)
                    {
                        mListener.addFragmentAnimated(VehicleDataFragment.newInstance(vehicle));
                    }
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_OPEN_RATE_INCIDENCE))
                {
                    Vehicle vehicle = getVehicle(notification.vehicleId);
                    if (vehicle != null && vehicle.incidences != null && notification.incidenceId != null)
                    {
                        Incidence incidence = null;
                        for (Incidence in : vehicle.incidences)
                        {
                            if (in.id == Integer.parseInt(notification.incidenceId))
                            {
                                incidence = in;
                                break;
                            }
                        }

                        if (incidence == null)
                        {
                            incidence = new Incidence();
                            incidence.id = Integer.parseInt(notification.incidenceId);
                        }

                        mListener.addFragmentAnimated(IncidenceValorationFragment.newInstance(incidence));
                    }
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_OPEN_VEHICLE_DRIVERS))
                {
                    Vehicle vehicle = getVehicle(notification.vehicleId);
                    if (vehicle != null)
                    {
                        if (notification.userId != null && notification.driverType != null && notification.driverName != null) //Se ha de aprobar/denegar a usuario como conductor principal o secundario
                        {
                            String title = notification.driverType.equals("1") ? getString(R.string.validate_driver_primary) : getString(R.string.validate_driver_secondary);
                            String message = notification.driverType.equals("1") ? getString(R.string.validate_driver_primary_desc, notification.driverName, vehicle.getName()) : getString(R.string.validate_driver_secondary_desc, notification.driverName, vehicle.getName());

                            ArrayList<String> options = new ArrayList<>();
                            options.add(getString(R.string.validate));
                            options.add(getString(R.string.no_validate));
                            ArrayList<Integer> optionsColors = new ArrayList<>();
                            optionsColors.add(Utils.getColor(getContext(), R.color.black600));
                            optionsColors.add(Utils.getColor(getContext(), R.color.error));

                            View.OnClickListener listener = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (view.getTag() != null)
                                    {
                                        String status = "0";

                                        int index = (int)view.getTag();
                                        if (index == 0)
                                        {
                                            //validate
                                            status = "1";
                                        }
                                        else if (index == 1)
                                        {
                                            //no validate
                                            status = "0";
                                        }

                                        showHud();
                                        Api.validateVehicleDriver(new IRequestListener() {
                                            @Override
                                            public void onFinish(IResponse response) {
                                                hideHud();
                                                if (response.isSuccess())
                                                {
                                                    loadNotificationsAndVehicles(false);
                                                }
                                                else
                                                {
                                                    onBadResponse(response);
                                                }
                                            }
                                        }, vehicle.id, notification.userId, status);
                                    }
                                }
                            };

                            RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
                            INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);



                        }
                        else
                        {
                            mListener.addFragmentAnimated(VehicleOptionsFragment.newInstance(vehicle));
                        }
                    }
                }
                else if (notification.action.equals(Constants.NOTIFICATION_ACTION_RESEND_DRIVER_REQUEST))
                {
                    if (notification.vehicleId != null && notification.driverType != null)
                    {
                        showHud();
                        Api.requestAddVehicleDriver(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response) {
                                hideHud();
                                if (response.isSuccess())
                                {
                                    loadNotificationsAndVehicles(false);
                                }
                                else
                                {
                                    onBadResponse(response);
                                }
                            }
                        }, notification.vehicleId, notification.driverType);
                    }
                }
            }
        }
        else if (object instanceof HomeNoticesAdapter.DeleteNotice)
        {
            HomeNoticesAdapter.DeleteNotice del = (HomeNoticesAdapter.DeleteNotice) object;
            Notification notification = del.notification;

            showHud();
            Api.updateNotificationStatus(notification, Constants.NOTIFICATION_STATUS_DELETE, new IRequestListener() {
                @Override
                public void onFinish(IResponse response)
                {
                    hideHud();
                    if (response.isSuccess())
                    {
                        ArrayList<Notification> temp = new ArrayList<>();
                        for (int i = 0; i < notifications.size(); i++)
                        {
                            Notification not = notifications.get(i);
                            if (!not.equals(notification))
                            {
                                temp.add(not);
                            }
                        }

                        notifications.clear();
                        notifications.addAll(temp);
                        noticesAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        onBadResponse(response);
                    }
                }
            });
        }
    }

    private void activateLocation()
    {
        if (LocationManager.hasPermission(getContext()))
        {
            //A partir de Api 30 no se puede solicitar directamente el Always, sino no sale la alerta
            if (Build.VERSION.SDK_INT >= 30)
            {
                //si da Permitir sólo cuando esté en uso: OK, no hacemos nada más (nos vale cuando está en uso para poder reportar una incidencia)
                // a no ser que: si tiene baliza vinculada y no tiene los de ubicación siempre, pedirlos aquí.

                if (Core.hasAnyVehicleWithBeacon())
                {
                    if (!LocationManager.hasBackgroundPermission(getContext()))
                    {/*
                        String title = getString(R.string.nombre_app);
                        String message = getString(R.string.activate_location_message_beacon_always);
                        String okButton = getString(R.string.activate_location_message_beacon_always_go);
                        DialogInterface.OnClickListener listenerOk = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                LocationManager.requestPermissionWithBackground(Constants.PERMISSION_LOCATION_BACKGROUND_REQUEST_CODE, getBaseActivity());
                            }
                        };
                        DialogInterface.OnClickListener listenerCancel = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        };
                        String cancelButton = getString(R.string.cancel);
                        showAlertConfirm(title, message, listenerOk, okButton, listenerCancel, cancelButton);
                        */
                        LocationManager.requestPermissionWithBackground(Constants.PERMISSION_LOCATION_BACKGROUND_REQUEST_CODE, getBaseActivity());
                    }
                }
            }

            layoutLocationDisabled.setVisibility(View.GONE);
            LocationManager.getLocation(getContext(), new LocationManager.LocationListener() {
                @Override
                public void onLocationResult(Location location) {

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
            });
        }
        else
        {
            layoutLocationDisabled.setVisibility(View.VISIBLE);

            //A partir de Api 30 no se puede solicitar directamente el Always, sino no sale la alerta
            if (Build.VERSION.SDK_INT >= 30) {
                LocationManager.requestPermission(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity());
            }
            else {
                LocationManager.requestPermissionWithBackground(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity());
            }
        }
    }

    private boolean activateCall()
    {
        boolean called = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getBaseActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                called = true;
                ActivityCompat.requestPermissions(getBaseActivity(), new String[]{Manifest.permission.CALL_PHONE}, BaseActivity.PERMISSION_CALL_PHONE);
            }
        }

        return called;
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


            homeTitleSmall.setText(addr1);
            homeTitleBig.setText(addr2);
        }
    }

    private void printManualAddress()
    {
        layoutLocationDisabled.setVisibility(View.GONE);
        if (Core.manualAddressSearchResult != null)
        {
            printAddress(Core.manualAddressSearchResult);
            drawPoint(Core.manualAddressSearchResult.getCoordinate());
        }
    }

    private void showVehiclesActionSheet()
    {
        String idVehicleDefault = Core.loadData(Constants.KEY_USER_DEFAULT_VEHICLE_ID);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        ArrayList<View> views = new ArrayList<>();

        if (vehicles != null)
        {
            for (int i = 0; i < vehicles.size(); i++)
            {
                Vehicle vehicle = vehicles.get(i);

                View view = inflater.inflate(R.layout.row_vehicle, null);

                ImageView imgVehicle = view.findViewById(R.id.imgVehicle);
                ImageView imgRight = view.findViewById(R.id.imgRight);
                ImageView imgBeacon = view.findViewById(R.id.imgCheck);
                imgRight.setVisibility(View.GONE);
                TextView txtVehicleMatricula = view.findViewById(R.id.txtMatricula);
                TextView txtVehicleName = view.findViewById(R.id.txtName);
                FontUtils.setTypeValueText(txtVehicleMatricula, Constants.FONT_SEMIBOLD, getContext());
                FontUtils.setTypeValueText(txtVehicleName, Constants.FONT_SEMIBOLD, getContext());

                txtVehicleMatricula.setText(vehicle.licensePlate);
                txtVehicleName.setText(vehicle.getName());
                if (vehicle.image != null)
                {
                    ImageManager.loadImage(getContext(), vehicle.image, imgVehicle);
                }
                else {
                    imgVehicle.setImageDrawable(null);
                }

                if (idVehicleDefault != null && idVehicleDefault.equals(vehicle.id))
                {
                    imgRight.setVisibility(View.VISIBLE);
                    imgRight.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.icon_check_circle));
                }

                if (vehicle.beacon != null)
                {
                    imgBeacon.setVisibility(View.VISIBLE);
                }
                else
                {
                    imgBeacon.setVisibility(View.GONE);
                }

                views.add(view);
            }
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    Vehicle vehicle = vehicles.get(index);
                    Core.saveData(Constants.KEY_USER_DEFAULT_VEHICLE_ID, vehicle.id);

                    txtVehicleMatricula.setText(vehicle.licensePlate);
                    txtVehicleName.setText(vehicle.getName());

                    if (vehicle.image != null)
                    {
                        ImageManager.loadImage(getContext(), vehicle.image, imgVehicle);
                    }
                    else {
                        imgVehicle.setImageDrawable(null);
                    }

                    if (vehicle.beacon != null)
                    {
                        imgVehicleBeacon.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        imgVehicleBeacon.setVisibility(View.GONE);
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsViewNotification(layoutToShow, views, listener);
    }

    @Override
    public void onBaseRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        if (requestCode == Constants.PERMISSION_RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SpeechManager.isEnabled = true;
                setUpSpeechButton();
                setUpVolumeAlert();
            } else {
                SpeechManager.isEnabled = false;
                setUpSpeechButton();
                setUpVolumeAlert();
                showAlert(R.string.alert_need_audio_permission_description);
            }
        } else if (requestCode == Constants.PERMISSION_LOCATION_BACKGROUND_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
            }
            else
            {
                showAlert(R.string.alert_no_location_to_beacon);
            }
        }
        else if (requestCode == BaseActivity.PERMISSION_LOCATION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                activateLocation();
            }
            else if (Build.VERSION.SDK_INT >= 23 && permissions.length > 0 && !shouldShowRequestPermissionRationale(permissions[0]))
            {
                // User selected the Never Ask Again Option Change settings in app settings manually
                if (userClickedToActivateLocation)
                {
                    userClickedToActivateLocation = false;
                    LocationManager.showPermissionRequiredDialog(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity(), null);
                }
            }
            else {
                showAlert(R.string.alert_no_location_to_report);
            }

        }
        else if (requestCode == BaseActivity.PERMISSION_CALL_PHONE)
        {
            //nos da igual si aceptó o no el permiso de llamada, es para ordenar peticiones
            activateLocation();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.APP_DID_BECOME_ACTIVE)
        {
            loadData();
        }
        else if (event.code == EventCode.USER_LOCATION_UPDATED)
        {
            printManualAddress();
        }
        else if (event.code == EventCode.VEHICLE_ADDED)
        {
            /*
            if (vehicles == null)
                vehicles = new ArrayList<>();

            Vehicle temp = (Vehicle) event.object;
            vehicles.add(temp);
            reloadDataVehicles();
            loadNotifications();
            */

            loadNotificationsAndVehicles(false);
        }
        else if (event.code == EventCode.VEHICLE_UPDATED && vehicles != null)
        {
            ArrayList<Vehicle> list = new ArrayList<>();

            Vehicle temp = (Vehicle) event.object;
            for (int i = 0; i < vehicles.size(); i++)
            {
                Vehicle vehicle = vehicles.get(i);
                if (temp.id.equals(vehicle.id))
                {
                    list.add(temp);
                }
                else
                {
                    list.add(vehicle);
                }
            }

            vehicles = list;
            reloadDataVehicles();
            loadNotifications();
        }
        else if (event.code == EventCode.VEHICLE_DELETED && vehicles != null)
        {
            ArrayList<Vehicle> list = new ArrayList<>();

            Vehicle temp = (Vehicle) event.object;
            for (int i = 0; i < vehicles.size(); i++)
            {
                Vehicle vehicle = vehicles.get(i);
                if (!temp.id.equals(vehicle.id))
                {
                    list.add(vehicle);
                }
            }

            vehicles = list;
            reloadDataVehicles();
            loadNotifications();
        }
        else if (event.code == EventCode.BEACON_ADDED || event.code == EventCode.BEACON_DELETED)
        {
            loadNotifications();
        }
        else if (event.code == EventCode.INCIDENCE_REPORTED)
        {
            loadNotificationsAndVehicles(false);
        }
        else if (event.code == EventCode.USER_UPDATED)
        {
            loadNotifications();
        }
        else if (event.code == EventCode.VEHICLE_DRIVER_UPDATED)
        {
            loadNotificationsAndVehicles(false);
        }
        else if (event.code == EventCode.VOLUMEN_CHANGED)
        {
            setUpVolumeAlert();
        }
    }
}
