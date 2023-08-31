package es.incidence.core.fragment.sign;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.e510.commons.activity.BaseActivity;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;
import com.e510.commons.view.FloatLabeled.FloatEditText;
import com.e510.location.LocationManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.activity.IActivity;
import es.incidence.core.activity.MainActivity;
import es.incidence.core.adapter.BeaconVehicleListAdapter;
import es.incidence.core.domain.Beacon;
import es.incidence.core.domain.BeaconType;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.entity.sign.SignStep;
import es.incidence.core.entity.sign.SignStepType;
import es.incidence.core.fragment.add.AddVehicleFragment;
import es.incidence.core.fragment.incidence.report.IncidenceReportFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.manager.beacon.BeaconListener;
import es.incidence.core.manager.beacon.BeaconManager;
import es.incidence.core.utils.Tooltip;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.IField;
import es.incidence.core.utils.view.INavigation;
import es.incidence.core.utils.view.INotification;

public class SignUpBeaconFragment extends SignUpFragment {
    private static final String TAG = makeLogTag(SignUpBeaconFragment.class);


    private RelativeLayout layoutHeader;
    private ImageView imgBackground;
    private ImageView imgBeacon;
    private ScrollView layoutInfo;
    private RelativeLayout layoutIn;
    private ImageView imgIn;
    private ImageView imgInSuccess;
    private TextView txtInfoTitle;
    private TextView txtInfoSubtitle;
    private TextView txtOmitir;
    private IButton btnContinue;
    private RelativeLayout layoutLoading;

    private RelativeLayout layoutSuccess;

    private static final String VIEW_SEARCH = "VIEW_SEARCH";
    private static final String VIEW_ACTIVATE_BLUETOOTH = "VIEW_ACTIVATE_BLUETOOTH";
    private static final String VIEW_NO_BEACONS_DETECTED = "VIEW_NO_BEACONS_DETECTED";
    private static final String VIEW_BEACONS_DETECTED = "VIEW_BEACONS_DETECTED";
    private static final String VIEW_BEACON_ADDED = "VIEW_BEACON_ADDED";
    private String currentView;
    public boolean isScanning;

    private Handler handlerScan;
    private View beaconsView;
    private BeaconVehicleListAdapter beaconsListAdapter;
    private ArrayList<Object> vehicles = new ArrayList<>();
    private Beacon selectedBeacon;

    public static final String KEY_AUTO_SELECTED_VEHICLE = "KEY_AUTO_SELECTED_VEHICLE";
    private static final String TAG_FROM_ADD_BEACON = "TAG_FROM_ADD_BEACON";

    public Vehicle autoSelectedVehicle;

    private boolean openedSettingsToActivateLocation;


    //QR
    public boolean isIoT;
    private CodeScanner mCodeScanner;
    private RelativeLayout layoutScanQR;

    private boolean showIoT = true;


    public static SignUpBeaconFragment newInstance(Vehicle vehicle) {
        SignUpBeaconFragment fragment = new SignUpBeaconFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_AUTO_SELECTED_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public boolean needEventBus() {
        return true;
    }

    @Override
    public boolean onBackPressed() {

        if (isIoT) {
            isIoT = false;
            mCodeScanner = null;
            if (layoutScanQR != null) {
                layoutRootSignUp.removeView(layoutScanQR);
                layoutScanQR = null;
            }

            return true;
        } else if (isScanning) {
            return true;
        } else {
            boolean res = super.onBackPressed();

            if (res == false && isRegistration()) {
                Core.startApp(getBaseActivity());
                res = true;
            }

            return res;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            autoSelectedVehicle = getArguments().getParcelable(KEY_AUTO_SELECTED_VEHICLE);
        }
        if (autoSelectedVehicle == null) {
            ArrayList<Vehicle> vehicles = Core.getVehicles();
            if (vehicles != null && vehicles.size() == 1) {
                autoSelectedVehicle = vehicles.get(0);
            }
        }

        String showIoTStr = Core.loadData(Constants.KEY_CONFIG_SHOW_IOT);
        if (showIoTStr != null && "0".equals(showIoTStr)) {
            showIoT = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanBeacons();
    }

    @Override
    public int getStepBlock() {
        if (autoSelectedVehicle != null) {
            return 1;
        }

        return 2;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        if (autoSelectedVehicle == null) {
            beaconsView = getStep2View();
        }

        navigation.setTitle(getString(R.string.create_account_step3));


        LayoutInflater inflater = LayoutInflater.from(getContext());
        layoutSuccess = (RelativeLayout) inflater.inflate(R.layout.layout_beacon_add, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutSuccess.setLayoutParams(params);
        layoutSuccess.setVisibility(View.GONE);
        RelativeLayout layoutHeader2 = layoutSuccess.findViewById(R.id.layoutHeader);
        layoutLoading = layoutSuccess.findViewById(R.id.layoutLoading);
        TextView txtOmitir2 = layoutSuccess.findViewById(R.id.txtOmitir);
        txtOmitir2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printNextBlock();
            }
        });
        FontUtils.setTypeValueText(txtOmitir2, Constants.FONT_SEMIBOLD, getContext());

        IButton btnContinue2 = layoutSuccess.findViewById(R.id.btnContinueBeacon);
        btnContinue2.setPrimaryColors();
        FontUtils.setTypeValueText(btnContinue2, Constants.FONT_SEMIBOLD, getContext());
        btnContinue2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickContinue();
            }
        });

        ImageView imgBackground2 = layoutSuccess.findViewById(R.id.imgBackground);
        ImageView imgBeacon2 = layoutSuccess.findViewById(R.id.imgBeacon);
        ScrollView layoutInfo2 = layoutSuccess.findViewById(R.id.layoutInfo);
        RelativeLayout layoutIn2 = layoutSuccess.findViewById(R.id.layoutIn);
        imgInSuccess = layoutSuccess.findViewById(R.id.imgIn);
        TextView txtInfoTitle2 = layoutSuccess.findViewById(R.id.txtInfoTitle);
        FontUtils.setTypeValueText(txtInfoTitle2, Constants.FONT_SEMIBOLD, getContext());
        TextView txtInfoSubtitle2 = layoutSuccess.findViewById(R.id.txtInfoSubtitle);

        layoutHeader2.setVisibility(View.GONE);
        imgBackground2.setVisibility(View.GONE);
        imgBeacon2.setVisibility(View.GONE);
        layoutInfo2.setVisibility(View.VISIBLE);
        layoutIn2.setBackground(Utils.getDrawable(getContext(), R.drawable.ellipse));
        imgInSuccess.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.beacon_icon_iot));
        txtInfoTitle2.setVisibility(View.VISIBLE);
        txtInfoTitle2.setText(R.string.beacon_sync_success);
        txtInfoSubtitle2.setText(R.string.beacon_sync_success_desc);
        btnContinue2.setVisibility(View.VISIBLE);
        btnContinue2.setText(getString(R.string.finish));
        txtOmitir2.setVisibility(View.GONE);

        layoutRootSignUp.addView(layoutSuccess);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCodeScanner != null)
            mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        if (mCodeScanner != null)
            mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public void printStep(boolean isBack) {
        super.printStep(isBack);

        if (currentStep.id == ID_BEACON_SELECT_TYPE) {
            SignStep step = getStep(ID_BEACON_POWER);
            if (step != null) {
                steps.remove(step);
            }
        }
        if (currentStep.id == ID_BEACON_SELECT_VEHICLE) {
            loadVehicles();
        }
    }

    @Override
    public void loadData() {
        steps = new ArrayList<>();

        SignStep s0 = new SignStep(ID_BEACON_SELECT_TYPE);
        s0.type = SignStepType.CUSTOM;
        s0.navigationTitle = getString(R.string.create_account_step3);
        s0.title = getString(R.string.turn_on_beacon_flash);
        s0.customFullView = getStep0View();
        steps.add(s0);

        if (autoSelectedVehicle == null) {
            SignStep s2 = new SignStep(ID_BEACON_SELECT_VEHICLE);
            s2.type = SignStepType.CUSTOM;
            s2.navigationTitle = getString(R.string.create_account_step3);
            s2.title = getString(R.string.select_vehicle_to_link);
            s2.customFullView = beaconsView;
            steps.add(s2);
        }

        printStep(false, false);
        currentView = VIEW_SEARCH;
    }

    //private void makeLoadData()
    private synchronized void makeLoadData() {
        if (isIoT) {
            if (isIoT) {
                if (layoutScanQR == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    layoutScanQR = (RelativeLayout) inflater.inflate(R.layout.layout_scan_qr, null);
                    layoutScanQR.setVisibility(View.INVISIBLE);

                    INavigation layoutScanQRNavigation = layoutScanQR.findViewById(R.id.inavigation);
                    layoutScanQRNavigation.init(this, getString(R.string.create_account_step3), true);
                    layoutScanQRNavigation.setTitleColor(Utils.getColor(getContext(), android.R.color.white));
                    layoutScanQRNavigation.setBackColor(Utils.getColor(getContext(), android.R.color.white));

                    TextView txtHeaderQr = layoutScanQR.findViewById(R.id.txtHeaderQr);
                    FontUtils.setTypeValueText(txtHeaderQr, Constants.FONT_REGULAR, getContext());

                    layoutRootSignUp.addView(layoutScanQR);
                } else {
                    layoutScanQR.setVisibility(View.INVISIBLE);
                }

                if (mCodeScanner == null) {
                    CodeScannerView scannerView = layoutScanQR.findViewById(R.id.scanner_view);
                    mCodeScanner = new CodeScanner(getActivity(), scannerView);
                    mCodeScanner.setDecodeCallback(new DecodeCallback() {
                        @Override
                        public void onDecoded(@NonNull final Result result) {
                            if (result != null && result.getText() != null) {
                                LogUtil.logE(TAG, result.getText());

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layoutRootSignUp.removeView(layoutScanQR);
                                        layoutScanQR = null;
                                        mCodeScanner = null;

                                        selectedBeacon = new Beacon();
                                        selectedBeacon.iot = result.getText();

                                        validateBeacon();
                                    }
                                });
                            }
                        }
                    });

                    ArrayList<BarcodeFormat> frm = new ArrayList<>();
                    frm.add(BarcodeFormat.DATA_MATRIX);
                    frm.add(BarcodeFormat.QR_CODE);
                    mCodeScanner.setFormats(frm);
                    mCodeScanner.setAutoFocusEnabled(true);
                    mCodeScanner.setZoom(10);
                }

                //layoutRootSignUp.addView(layoutScanQR);
                mCodeScanner.startPreview();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layoutScanQR.setVisibility(View.VISIBLE);
                    }
                }, 500);
            }
        } else if (!mListener.checkPermission(getContext(), Manifest.permission.BLUETOOTH)) {
            DialogInterface.OnClickListener listenerCancel = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showCanceledView();
                }
            };
            DialogInterface.OnClickListener listenerSettings = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intentOpenBluetoothSettings = new Intent();
                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intentOpenBluetoothSettings);
                }
            };
            showAlertConfirm(getString(R.string.alert_need_bluetooth_title), getString(R.string.alert_need_bluetooth_description), listenerSettings, getString(R.string.settings), listenerCancel, getString(R.string.cancel));
        } else {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                showCanceledView();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled :)
                showCanceledView();
            } else {
                // Bluetooth is enabled
                checkLocationScan();
            }
        }
    }

    private void validateBeacon() {
        showHud();
        Api.validateBeacon(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess()) {
                    if (autoSelectedVehicle != null) {
                        addVehicleBeacon(autoSelectedVehicle, selectedBeacon);
                    } else {
                        printNextStep();
                    }
                } else {
                    onBadResponse(response);
                }
            }
        }, selectedBeacon);
    }

    private void checkLocationScan() {
        if (LocationManager.hasPermission(getBaseActivity())) {
            //if (!LocationManager.hasBackgroundPermission(getContext()))
            boolean initDetection = Core.hasAnyVehicleWithBeacon();
            if (initDetection && !LocationManager.hasBackgroundPermission(getContext())) {
                LocationManager.requestPermissionWithBackground(Constants.PERMISSION_LOCATION_BACKGROUND_REQUEST_CODE, getBaseActivity());
            } else {
                //showHud();
                layoutLoading.setVisibility(View.VISIBLE);
                scanBeacons();
            }
        } else {
            showLocationPopUp();
        }
    }

    private void showLocationPopUp() {
        String title = getString(R.string.activate_location_title);
        String message = getString(R.string.activate_location_message_beacon);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.activate_location));
        options.add(getString(R.string.cancel));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null) {
                    int index = (int) view.getTag();
                    if (index == 0) {
                        //Activar ubicación
                        activateLocation();
                    } else if (index == 1) {
                        //Cancela
                        closeThis();
                    }
                }
            }
        };

        RelativeLayout layoutToShow = layoutRootSignUp;//getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, null, listener, false);
    }

    private void activateLocation() {
        //A partir de Api 30 no se puede solicitar directamente el Always, sino no sale la alerta
        if (Build.VERSION.SDK_INT >= 30) {
            LocationManager.requestPermission(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity());
        } else {
            LocationManager.requestPermissionWithBackground(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity());
        }
    }

    public static boolean hasCameraPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private View getStep0View() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_beacon_select_type, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        View viewBluetoothContent = view.findViewById(R.id.line21);
        View viewQRContent = view.findViewById(R.id.line1);

        ImageView imgBluetooth = view.findViewById(R.id.imgRight2);
        ImageView imgQR = view.findViewById(R.id.imgRight);

        //ImageView imgBluetoothContent = view.findViewById(R.id.imgRight21);
        ImageView imgQRContent1 = view.findViewById(R.id.imgRight1);
        ImageView imgQRContent12 = view.findViewById(R.id.imgRight12);
        TextView lTxtOmitir3 = view.findViewById(R.id.txtOmitir3);

        if (fromAddBeacon()) {
            lTxtOmitir3.setVisibility(View.GONE);
        } else {
            lTxtOmitir3.setVisibility(View.VISIBLE);
        }


        lTxtOmitir3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printNextStep();
            }
        });

        /*
        imgQRContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTooltip(imgQRContent);
            }
        });
        */

        imgQRContent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTooltipDevice(imgQRContent1);
            }
        });

        imgQRContent12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTooltipText(imgQRContent12);
            }
        });

        RelativeLayout layoutBluetoothContent = view.findViewById(R.id.layoutRow21);
        layoutBluetoothContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isIoT = false;

                SignStep s1 = new SignStep(ID_BEACON_POWER);
                s1.type = SignStepType.CUSTOM;
                s1.navigationTitle = getString(R.string.create_account_step3);
                s1.title = getString(R.string.turn_on_beacon_flash);
                s1.customFullView = getStep1View();
                steps.add(1, s1);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!mListener.checkPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) || !mListener.checkPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT)) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, IActivity.PERMISSION_BLUETOOTH_CONNECT);
                        return;
                    } else {
                        printNextStep();
                        makeLoadData();
                    }
                } else {
                    printNextStep();
                    makeLoadData();
                }
            }
        });

        LinearLayout layoutQRContent = view.findViewById(R.id.layoutRow1);
        RelativeLayout layoutQR1 = view.findViewById(R.id.layoutRow11);
        RelativeLayout layoutQR2 = view.findViewById(R.id.layoutRow12);

        layoutQR1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasCameraPermission(getActivity()) || !mListener.checkPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) || !mListener.checkPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT)) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, BaseActivity.PERMISSION_CAMERA);
                } else {
                    isIoT = true;
                    makeLoadData();
                }
            }
        });

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.layout_popup);
        View popupImei = inflater.inflate(R.layout.layout_popup_imei, null);


        RelativeLayout popupContainer = popupImei.findViewById(R.id.popupContainer);
        View popupContainerBackground = popupContainer.findViewById(R.id.popupContainerBackground);
        RelativeLayout popupContainerView = popupContainer.findViewById(R.id.popupContainerView);
        ImageView imgClosePopupImei = popupContainer.findViewById(R.id.imgClosePopupImei);
        TextView txtTitle = popupContainer.findViewById(R.id.txtTitle);
        TextView txtSubTitle = popupContainer.findViewById(R.id.txtSubTitle);
        IField fieldImei = popupContainer.findViewById(R.id.fieldImei);
        RelativeLayout layoutRootIField = fieldImei.findViewById(R.id.layoutRoot);
        FloatEditText textInputLayout = fieldImei.findViewById(R.id.textInputLayout);
        IButton btnBlue = popupContainer.findViewById(R.id.btnBlue);

        textInputLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    GradientDrawable drawable = (GradientDrawable) layoutRootIField.getBackground();
                    drawable.setStroke(Utils.dpToPx(1), Utils.getColor(getContext(), R.color.black300));
                } else {
                    GradientDrawable drawable = (GradientDrawable) layoutRootIField.getBackground();
                    drawable.setStroke(Utils.dpToPx(1), Utils.getColor(getContext(), R.color.incidence400));
                }
            }
        });
        GradientDrawable drawable = (GradientDrawable) layoutRootIField.getBackground();
        drawable.setStroke(Utils.dpToPx(1), Utils.getColor(getContext(), R.color.black300));

        layoutQR2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutToShow.addView(popupImei);
            }
        });

        popupContainerBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutToShow.removeView(popupImei);
            }
        });

        popupContainerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        FontUtils.setTypeValueText(txtTitle, Constants.FONT_SEMIBOLD, getContext());
        FontUtils.setTypeValueText(txtSubTitle, Constants.FONT_REGULAR, getContext());
        imgClosePopupImei.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {
                                                     layoutToShow.removeView(popupImei);
                                                 }
                                             }
        );
        btnBlue.setVisibility(View.VISIBLE);
        btnBlue.setText(getString(R.string.accept));
        btnBlue.setPrimaryColors();
        FontUtils.setTypeValueText(btnBlue, Constants.FONT_SEMIBOLD, getContext());
        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedBeacon = new Beacon();
                selectedBeacon.iot = fieldImei.getText();
                //mListener.addFragmentAnimated(IncidenceReportFragment.newInstance(beacon.vehicle, beacon.vehicle, false));
                layoutToShow.removeView(popupImei);
                validateBeacon();
            }
        });

        fieldImei.setHint(getString(R.string.select_beacon_imei));
        fieldImei.setTitle(getString(R.string.select_beacon_imei));
        fieldImei.setType(IField.TYPE_NUMBER);

        RelativeLayout layoutQR = view.findViewById(R.id.layoutRow);

        if (!showIoT) {
            layoutQR.setVisibility(View.GONE);
        }
        layoutQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutQRContent.getVisibility() == View.VISIBLE) {
                    layoutQRContent.setVisibility(View.GONE);
                    viewQRContent.setVisibility(View.GONE);
                    imgQR.setImageResource(R.drawable.icon_arrow_down);
                } else {
                    layoutQRContent.setVisibility(View.VISIBLE);
                    viewQRContent.setVisibility(View.VISIBLE);
                    imgQR.setImageResource(R.drawable.icon_arrow_top);
                }
            }
        });

        RelativeLayout layoutBluetooth = view.findViewById(R.id.layoutRow2);
        layoutBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (layoutBluetoothContent.getVisibility() == View.VISIBLE) {
                    layoutBluetoothContent.setVisibility(View.GONE);
                    viewBluetoothContent.setVisibility(View.GONE);
                    imgBluetooth.setImageResource(R.drawable.icon_arrow_down);
                } else {
                    layoutBluetoothContent.setVisibility(View.VISIBLE);
                    viewBluetoothContent.setVisibility(View.VISIBLE);
                    imgBluetooth.setImageResource(R.drawable.icon_arrow_top);
                }
            }
        });

        return view;
    }

    public void showTooltipDevice(View rowView) {
        Tooltip.showTooltipDevice(getContext(), rowView);
    }

    public void showTooltipText(View rowView) {
        Tooltip.showTooltipText(getContext(), rowView, getString(R.string.select_beacon_imei_info));
    }

    private View getStep1View() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_beacon_add, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutHeader = view.findViewById(R.id.layoutHeader);
        txtOmitir = view.findViewById(R.id.txtOmitir);
        txtOmitir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //printNextBlock();

                if (handlerScan != null) {
                    handlerScan.removeCallbacksAndMessages(null);
                }

                if (currentView.equals(VIEW_SEARCH)) {
                    //hideHud();
                    layoutLoading.setVisibility(View.GONE);
                    showNoBeaconsDetectedView();
                    stopScanBeacons();
                } else {
                    printNextBlock();
                }
            }
        });
        FontUtils.setTypeValueText(txtOmitir, Constants.FONT_SEMIBOLD, getContext());

        btnContinue = view.findViewById(R.id.btnContinueBeacon);
        btnContinue.setPrimaryColors();
        FontUtils.setTypeValueText(btnContinue, Constants.FONT_SEMIBOLD, getContext());
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickContinue();
            }
        });

        imgBackground = view.findViewById(R.id.imgBackground);
        imgBeacon = view.findViewById(R.id.imgBeacon);
        layoutInfo = view.findViewById(R.id.layoutInfo);
        layoutIn = view.findViewById(R.id.layoutIn);
        imgIn = view.findViewById(R.id.imgIn);
        txtInfoTitle = view.findViewById(R.id.txtInfoTitle);
        FontUtils.setTypeValueText(txtInfoTitle, Constants.FONT_SEMIBOLD, getContext());
        txtInfoSubtitle = view.findViewById(R.id.txtInfoSubtitle);

        //txtOmitir.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);

        return view;
    }

    private View getStep2View() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_beacon_link_device, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        ListView listView = view.findViewById(R.id.listView);
        beaconsListAdapter = new BeaconVehicleListAdapter(this, vehicles);
        listView.setAdapter(beaconsListAdapter);

        return view;
    }

    private void scanBeacons() {
        isScanning = true;

        if (handlerScan != null) {
            handlerScan.removeCallbacksAndMessages(null);
        }

        handlerScan = new Handler(Looper.getMainLooper());
        handlerScan.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentView.equals(VIEW_SEARCH)) {
                    //hideHud();
                    layoutLoading.setVisibility(View.GONE);
                    showNoBeaconsDetectedView();
                    stopScanBeacons();
                }
            }
        }, 20000);

        BeaconManager.getInstance().bind(new BeaconListener() {
            @Override
            public void didEnterRegion(String region) {
            }

            @Override
            public void onBeaconsDetected(ArrayList<Beacon> beacons) {
                stopScanBeacons();
                showBeaconsDetectedView();
                //hideHud();
                layoutLoading.setVisibility(View.GONE);

                selectedBeacon = beacons.get(0);
                imgBeacon.setVisibility(View.VISIBLE);
                imgBeacon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validateBeacon();
                    }
                });
            }
        });
    }

    private void stopScanBeacons() {
        if (handlerScan != null) {
            handlerScan.removeCallbacksAndMessages(null);
        }
        BeaconManager.getInstance().unbind();

        isScanning = false;
    }

    private void loadVehicles() {
        showHud();
        Api.getVehicles(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                if (response.isSuccess()) {
                    ArrayList<Vehicle> list = response.getList("vehicles", Vehicle.class);
                    ArrayList<Object> temp = new ArrayList<>();

                    for (int i = 0; i < list.size(); i++) {
                        Vehicle vehicle = list.get(i);
                        temp.add(vehicle);
                    }
                    temp.add("add");

                    vehicles.clear();
                    vehicles.addAll(temp);
                    beaconsListAdapter.notifyDataSetChanged();

                    hideHud();

                    if (list.size() == 1) {
                        //sólo hay 1 vehículo. automatizamos la selección.
                        Vehicle vehicle = list.get(0);
                        addVehicleBeacon(vehicle);
                    }
                } else {
                    hideHud();
                    onBadResponse(response);
                }
            }
        });
    }

    private void onClickContinue() {
        if (currentView.equals(VIEW_ACTIVATE_BLUETOOTH)) {
            //Intentar de nuevo
            showSearchingView();
            makeLoadData();
        } else if (currentView.equals(VIEW_SEARCH)) {
            //Intentar de nuevo
            showSearchingView();
            makeLoadData();
        } else if (currentView.equals(VIEW_NO_BEACONS_DETECTED) || currentView.equals(VIEW_BEACONS_DETECTED)) {
            //Intentar de nuevo
            showSearchingView();
            makeLoadData();
        } else if (currentView.equals(VIEW_BEACON_ADDED)) {
            Core.startApp(getBaseActivity());
        }
    }

    private void showSearchingView() {
        layoutHeader.setVisibility(View.VISIBLE);
        imgBackground.setVisibility(View.VISIBLE);
        txtInfoTitle.setVisibility(View.VISIBLE);
        layoutInfo.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
        //txtOmitir.setVisibility(View.GONE);

        currentView = VIEW_SEARCH;
    }

    private void showCanceledView() {
        layoutHeader.setVisibility(View.GONE);
        imgBackground.setVisibility(View.GONE);
        imgBeacon.setVisibility(View.GONE);
        layoutInfo.setVisibility(View.VISIBLE);
        layoutIn.setBackground(Utils.getDrawable(getContext(), R.drawable.circle_help));
        imgIn.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.bluetooth));
        txtInfoTitle.setVisibility(View.GONE);
        txtInfoSubtitle.setText(Html.fromHtml(getString(R.string.beacon_error_need_bluettoh)));
        btnContinue.setVisibility(View.VISIBLE);
        btnContinue.setText(getString(R.string.retry));
        txtOmitir.setVisibility(View.VISIBLE);
        txtOmitir.setText(getString(R.string.no_activate_now));
        layoutLoading.setVisibility(View.GONE);

        currentView = VIEW_ACTIVATE_BLUETOOTH;
    }

    private void showBeaconsDetectedView() {
        TextView textView = layoutHeader.findViewById(R.id.txtHeaderVehicle);
        textView.setText(R.string.turn_on_beacon_flash_detected);

        btnContinue.setVisibility(View.VISIBLE);
        btnContinue.setText(getString(R.string.search_again));
        txtOmitir.setVisibility(View.VISIBLE);
        txtOmitir.setText(getString(R.string.omitir));

        currentView = VIEW_BEACONS_DETECTED;
    }

    private void showNoBeaconsDetectedView() {
        layoutHeader.setVisibility(View.GONE);
        imgBackground.setVisibility(View.GONE);
        imgBeacon.setVisibility(View.GONE);
        layoutInfo.setVisibility(View.VISIBLE);
        layoutIn.setBackground(Utils.getDrawable(getContext(), R.drawable.circle_help));
        imgIn.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.info));
        txtInfoTitle.setVisibility(View.GONE);
        txtInfoSubtitle.setText(R.string.beacons_not_detected);
        btnContinue.setVisibility(View.VISIBLE);
        btnContinue.setText(getString(R.string.retry));
        txtOmitir.setVisibility(View.VISIBLE);
        txtOmitir.setText(getString(R.string.omitir));

        currentView = VIEW_NO_BEACONS_DETECTED;
    }

    private void showBeaconAddedView() {
        //positionStep = 0;
        //printStep(false);
        isScanning = true;
        navigation.setEnabled(false);
        stepper.setVisibility(View.GONE);

        layoutSuccess.setVisibility(View.VISIBLE);/*
        if (isIoT)
        {
            imgInSuccess.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.beacon_icon_iot));
        }
        else
        {
            imgInSuccess.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.beacon_icon_smart));
        }*/


        currentView = VIEW_BEACON_ADDED;
    }

    @Override
    public void onClickRow(Object object) {
        if (object instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) object;
            addVehicleBeacon(vehicle);
        } else if (object instanceof String) {
            mListener.addFragmentAnimated(AddVehicleFragment.newInstance(true));
        }
    }

    private void addVehicleBeacon(Vehicle vehicle) {
        addVehicleBeacon(vehicle, null);
    }

    private void addVehicleBeacon(Vehicle vehicle, Beacon beacon) {
        if (vehicle.beacon != null) {
            showReplaceBeaconPopup(vehicle, beacon);
        } else {
            setBeaconToVehicle(vehicle, beacon);
        }
    }

    @Override
    public void onBaseRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == Constants.PERMISSION_LOCATION_BACKGROUND_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //showHud();
                layoutLoading.setVisibility(View.VISIBLE);
                scanBeacons();
            } else {
                showAlert(R.string.app_name, R.string.alert_no_location_to_beacon, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //showHud();
                        layoutLoading.setVisibility(View.VISIBLE);
                        scanBeacons();
                    }
                });
            }
        } else if (requestCode == BaseActivity.PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationScan();
            } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {
                // User selected the Never Ask Again Option Change settings in app settings manually

                DialogInterface.OnClickListener askListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                openedSettingsToActivateLocation = true;
                                LocationManager.openSettingsPermissions(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity());
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                closeThis();
                                break;
                        }
                    }
                };
                LocationManager.showPermissionRequiredDialog(BaseActivity.PERMISSION_LOCATION_REQUEST_CODE, getBaseActivity(), askListener);
            } else {
                closeThis();
            }
        } else if (requestCode == BaseActivity.PERMISSION_CAMERA) {
            isIoT = true;
            makeLoadData();
        }
    }

    private void showReplaceBeaconPopup(Vehicle vehicle, Beacon beacon) {
        hideKeyboard();

        String title = null;
        String message = getString(R.string.ask_link_beacon_to_vehicle_linked, vehicle.getName());
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.cancel));
        options.add(getString(R.string.replace_beacon));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null) {
                    int index = (int) view.getTag();
                    if (index == 0) {
                        //cancela
                    } else if (index == 1) {
                        //Replace beacon
                        setBeaconToVehicle(vehicle, beacon);
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    private void setBeaconToVehicle(Vehicle vehicle, Beacon beacon) {
        showHud();
        Api.addBeacon(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess()) {
                    if (beacon == null) {
                        vehicle.beacon = (Beacon) response.get("vehicleBeacon", Beacon.class);
                    } else {
                        Beacon beaconResp = (Beacon) response.get("vehicleBeacon", Beacon.class);
                        beacon.id = beaconResp.id;
                        BeaconType beaconType = new BeaconType();
                        if (beacon.iot != null) {
                            beaconType.id = 2;
                        } else {
                            beaconType.id = 1;
                        }
                        beacon.beaconType = beaconType;
                        vehicle.beacon = beacon;
                    }

                    Core.saveVehicle(vehicle);
                    EventBus.getDefault().post(new Event(EventCode.VEHICLE_UPDATED, vehicle));
                    EventBus.getDefault().post(new Event(EventCode.BEACON_ADDED, vehicle.beacon));

                    showBeaconAddedView();
                } else {
                    onBadResponse(response);
                }
            }
        }, selectedBeacon, vehicle);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event) {
        if (event.code == EventCode.VEHICLE_ADDED) {
            loadVehicles();
        } else if (event.code == EventCode.APP_DID_BECOME_ACTIVE) {
            if (openedSettingsToActivateLocation) {
                openedSettingsToActivateLocation = false;
                checkLocationScan();
            }
        }
    }
}
