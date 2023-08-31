package es.incidence.core.fragment.incidence.report;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.view.BottomSheetLayout;
import com.e510.commons.view.SwipeButton;
import com.mapbox.geojson.Point;

import org.greenrobot.eventbus.EventBus;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Asitur;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.common.MapFullFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;

public class ReportMapFragment extends MapFullFragment
{
    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    //params
    public Vehicle vehicle;

    public static final String KEY_INCIDENCE = "KEY_INCIDENCE";
    public Incidence incidence;

    private Handler handlerScan;

    private TextView txtTitle;
    private TextView txtMessage;
    private IButton btnContinue;
    private TextView txtAddLater;
    private RelativeLayout layoutCallGrua;
    private SwipeButton btnCancelGrua;

    public static ReportMapFragment newInstance(Vehicle vehicle, Incidence incidence)
    {
        ReportMapFragment fragment = new ReportMapFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        bundle.putParcelable(KEY_INCIDENCE, incidence);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            vehicle = getArguments().getParcelable(KEY_VEHICLE);
            incidence = getArguments().getParcelable(KEY_INCIDENCE);
        }
    }

    @Override
    public void onDestroy() {
        if (handlerScan != null)
        {
            handlerScan.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        searchField.setVisibility(View.GONE);


        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_bottomsheet_map_report, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        view.setClipToOutline(true);
        float radius = Utils.dpToPx(16);
        GradientDrawable drawable = Utils.createGradientDrawable(getContext(), android.R.color.white, 0);
        drawable.setCornerRadii(new float [] { radius, radius,
                radius, radius,
                0, 0,
                0, 0});
        view.setBackground(drawable);

        txtTitle = view.findViewById(R.id.txtTitle);
        txtMessage = view.findViewById(R.id.txtMessage);

        FontUtils.setTypeValueText(view.findViewById(R.id.txtTitle), Constants.FONT_SEMIBOLD, getContext());
        btnContinue = view.findViewById(R.id.btnContinue);
        FontUtils.setTypeValueText(btnContinue, Constants.FONT_SEMIBOLD, getContext());
        btnContinue.setPrimaryColors();
        txtAddLater = view.findViewById(R.id.txtAddLater);
        FontUtils.setTypeValueText(txtAddLater, Constants.FONT_SEMIBOLD, getContext());

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addFragmentAnimated(IncidenceValorationFragment.newInstance(incidence));
            }
        });

        txtAddLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.cleanAllBackStackEntries();
            }
        });


        layoutCallGrua = view.findViewById(R.id.btnCallGrua);
        GradientDrawable drawableGrua = Utils.createGradientDrawable(getContext(), R.color.incidence100, 64);
        layoutCallGrua.setBackground(drawableGrua);
        layoutCallGrua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callGrua();
            }
        });
        ImageView imgCallGrua = layoutCallGrua.findViewById(R.id.imgCallGrua);
        imgCallGrua.setColorFilter(Utils.getColor(getContext(), R.color.incidence500));

        btnCancelGrua = view.findViewById(R.id.btnContinueSwipe);
        GradientDrawable drawableCancelGrua = Utils.createGradientDrawable(getContext(), R.color.error, 64);
        btnCancelGrua.setBackground(drawableCancelGrua);
        btnCancelGrua.setText(getString(R.string.contact_grua_cancel));
        btnCancelGrua.setDisabledDrawable(Utils.getDrawable(getContext(), R.drawable.icon_swipe_arrow));
        btnCancelGrua.setSwipeButtonListener(new SwipeButton.SwipeButtonListener() {
            @Override
            public void onSwiped() {
                cancelGrua();
            }
        });

        bottomSheetLayout.setVisibility(View.VISIBLE);
        bottomSheetLayoutBlur.setAlpha(0);
        bottomSheetLayoutBlur.setVisibility(View.GONE);
        bottomSheetLayout.setOnProgressListener(new BottomSheetLayout.OnProgressListener() {
            @Override
            public void onProgress(float v) {
                float opacity = v;
                if (opacity > 0.9) {
                    opacity = 0.9f;
                }
                bottomSheetLayoutBlur.setAlpha(opacity);
                bottomSheetLayoutBlur.setVisibility(opacity > 0 ? View.VISIBLE : View.GONE);
            }
        });
        RelativeLayout layout = bottomSheetLayout.findViewById(R.id.layoutContent);
        layout.addView(view);
    }

    @Override
    public void loadData()
    {
        if (incidence != null)
        {
            if (incidence.latitude != null && incidence.longitude != null)
            {
                Point origin = Point.fromLngLat(incidence.longitude, incidence.latitude);
                drawPoint(origin);
            }

            loadGruaComing();
            loadAsitur();
        }
    }

    private void loadGruaComing()
    {
        txtTitle.setText(R.string.title_assist_coming);
        //txtMessage.setText(getString(R.string.subtitle_assist_coming, ""));
        //btnContinue.setVisibility(View.GONE);
        //txtAddLater.setVisibility(View.GONE);
        //layoutCallGrua.setVisibility(View.VISIBLE);
        //btnCancelGrua.setVisibility(View.VISIBLE);

        txtMessage.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
        txtAddLater.setVisibility(View.GONE);
        layoutCallGrua.setVisibility(View.GONE);
        btnCancelGrua.setVisibility(View.GONE);
    }

    private void loadGruaArrived()
    {
        txtTitle.setText(R.string.title_assist_finished);
        txtMessage.setText(R.string.subtitle_assist_finished);
        txtMessage.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.VISIBLE);
        txtAddLater.setVisibility(View.VISIBLE);
        layoutCallGrua.setVisibility(View.GONE);
        btnCancelGrua.setVisibility(View.GONE);
    }

    private void loadAsitur()
    {
        if (incidence.asitur != null)
        {
            showHud();
            Api.asiturIncidence(new IRequestListener() {
                @Override
                public void onFinish(IResponse response) {
                    hideHud();
                    if (response.isSuccess())
                    {
                        Asitur asitur = (Asitur) response.get("asitur", Asitur.class);

                        if (asitur != null)
                        {
                            if (asitur.latitude != null && asitur.longitude != null)
                            {
                                if (incidence.latitude != null && incidence.longitude != null)
                                {
                                    Point origin = Point.fromLngLat(asitur.longitude, asitur.latitude);
                                    Point destination = Point.fromLngLat(incidence.longitude, incidence.latitude);
                                    Drawable d1 = getResources().getDrawable(R.drawable.icon_grua);
                                    Drawable d2 = getResources().getDrawable(R.drawable.icon_user_location);

                                    drawRoute(origin, d1, destination, d2, imgCenterMap.getVisibility() != View.VISIBLE, false);
                                }
                                else
                                {
                                    Point origin = Point.fromLngLat(asitur.longitude, asitur.latitude);
                                    Drawable d1 = getResources().getDrawable(R.drawable.icon_grua);
                                    drawPoint(origin, d1);
                                }
                            }
                            else if (incidence.latitude != null && incidence.longitude != null)
                            {
                                Point origin = Point.fromLngLat(incidence.longitude, incidence.latitude);
                                Drawable d1 = getResources().getDrawable(R.drawable.icon_user_location);
                                drawPoint(origin, d1);
                            }

                            if (asitur.finish == 1)
                            {
                                loadGruaArrived();
                                bottomSheetLayout.expand();

                                EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));
                            }
                            else {
                                if (handlerScan != null)
                                {
                                    handlerScan.removeCallbacksAndMessages(null);
                                }
                                handlerScan = new Handler(Looper.getMainLooper());
                                handlerScan.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        loadAsitur();
                                    }
                                }, 10000);
                            }
                        }
                        else
                        {
                            if (handlerScan != null)
                            {
                                handlerScan.removeCallbacksAndMessages(null);
                            }
                            handlerScan = new Handler(Looper.getMainLooper());
                            handlerScan.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    loadAsitur();
                                }
                            }, 10000);
                        }
                    }
                    else
                    {
                        onBadResponse(response);
                    }
                }
            }, incidence.id+"");
        }
    }

    private void callGrua()
    {

    }

    private void cancelGrua()
    {
        bottomSheetLayout.collapse();
    }
}
