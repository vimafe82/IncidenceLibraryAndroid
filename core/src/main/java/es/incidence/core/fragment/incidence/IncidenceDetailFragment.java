package es.incidence.core.fragment.incidence;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.view.CircleImageView;
import com.mapbox.geojson.Point;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Notification;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.fragment.common.MapBoxFragment;
import es.incidence.core.fragment.incidence.report.IncidenceValorationFragment;
import es.incidence.core.fragment.incidence.report.ReportMapFragment;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INavigation;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class IncidenceDetailFragment extends MapBoxFragment
{
    private static final String TAG = makeLogTag(IncidenceDetailFragment.class);

    private INavigation navigation;

    private TextView txtDate;
    private TextView txtAddress;
    private TextView txtStatus;
    private LinearLayout layoutMessages;
    private IButton btnContinue;
    private RelativeLayout layoutFace;
    private ImageView imgFace;
    private TextView txtFace;

    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    public Vehicle vehicle;

    public static final String KEY_INCIDENCE = "KEY_INCIDENCE";
    public Incidence incidence;

    public static IncidenceDetailFragment newInstance(Vehicle vehicle, Incidence incidence)
    {
        IncidenceDetailFragment fragment = new IncidenceDetailFragment();

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
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootIncidenceDetail;
    }

    @Override
    public boolean isMoveDisabled() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_incidence_detail, container, false);
        setupUI(view);
        overrideOnCreateView(view, savedInstanceState);

        return view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);

        String city = incidence.city != null ? " " + getString(R.string.incidence_in) + " " + incidence.city : "";
        String title = incidence.getTitle() + city;
        navigation.init(this, title, true);

        txtAddress = rootView.findViewById(R.id.txtAddress);
        txtDate = rootView.findViewById(R.id.txtDate);
        txtStatus = rootView.findViewById(R.id.txtStatus);
        FontUtils.setTypeValueText(txtAddress, Constants.FONT_SEMIBOLD, getContext());
        FontUtils.setTypeValueText(txtDate, Constants.FONT_REGULAR, getContext());
        FontUtils.setTypeValueText(txtStatus, Constants.FONT_SEMIBOLD, getContext());

        layoutMessages = rootView.findViewById(R.id.layoutMessages);
        int radius = 8;
        GradientDrawable back =  Utils.createGradientDrawable(getContext(), android.R.color.white, radius);
        layoutMessages.setBackground(back);

        btnContinue = rootView.findViewById(R.id.btnContinue);
        btnContinue.setPrimaryColors();
        btnContinue.setText(R.string.go_to_map);
        FontUtils.setTypeValueText(btnContinue, Constants.FONT_SEMIBOLD, getContext());
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickContinue();
            }
        });

        layoutFace = rootView.findViewById(R.id.layoutFace);
        GradientDrawable back2 =  Utils.createGradientDrawable(getContext(), android.R.color.white, radius);
        layoutFace.setBackground(back2);
        imgFace = rootView.findViewById(R.id.imgFace);
        txtFace = rootView.findViewById(R.id.txtFace);
    }

    @Override
    public void loadData()
    {
        if (vehicle != null && incidence != null)
        {
            txtAddress.setText(incidence.street);
            txtDate.setText(vehicle.licensePlate + " - " +incidence.dateCreated);
            if (incidence.isClosed())
            {
                txtStatus.setText(getString(R.string.incidence_status_closed));

                int radius = 100;
                GradientDrawable back =  Utils.createGradientDrawable(getContext(), R.color.success, radius);
                txtStatus.setBackground(back);
            }
            else if (incidence.isCanceled())
            {
                txtStatus.setText(getString(R.string.incidence_status_canceled));

                int radius = 100;
                GradientDrawable back =  Utils.createGradientDrawable(getContext(), R.color.error, radius);
                txtStatus.setBackground(back);
            }
            else
            {
                txtStatus.setText(getString(R.string.incidence_status_active));

                int radius = 100;
                GradientDrawable back =  Utils.createGradientDrawable(getContext(), R.color.incidence500, radius);
                txtStatus.setBackground(back);
            }


            addMensajes();

            if (incidence.isCanceled())
            {
                btnContinue.setVisibility(View.GONE);
            }
            else if (incidence.rate != null)
            {
                showValorationView(incidence.rate);
            }
            else if (incidence.rate == null && incidence.isClosed())
            {
                btnContinue.setText(R.string.valorate_assitence);
            }

            if (incidence.latitude != null && incidence.longitude != null)
            {
                Point origin = Point.fromLngLat(incidence.longitude, incidence.latitude);
                //Point destination = Point.fromLngLat(2.3936500, 41.5096100);
                //drawRoute(origin, destination);
                drawPoint(origin);
            }
        }
    }

    private void addMensajes()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        layoutMessages.removeAllViews();

        ArrayList<String> mensajes = new ArrayList<>();
        ArrayList<String> mensajesSubtitulo = new ArrayList<>();


        if (incidence != null && incidence.notifications != null)
        {
            for (int i = 0; i < incidence.notifications.size(); i++)
            {
                Notification notification = incidence.notifications.get(i);

                mensajes.add(notification.title);
                mensajesSubtitulo.add(notification.dateCreated != null ? notification.dateCreated : "-");
            }
        }

        for (int i = 0; i < mensajes.size(); i++)
        {
            String m = mensajes.get(i);
            String s = mensajesSubtitulo.get(i);

            View view = inflater.inflate(R.layout.row_incidence_message, null);
            FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
            View progressUp = view.findViewById(R.id.progressUp);
            CircleImageView progressCircle = view.findViewById(R.id.progressCircle);
            View progressDown = view.findViewById(R.id.progressDown);
            TextView txtTitle = view.findViewById(R.id.txtTitle);
            TextView txtSubtitle = view.findViewById(R.id.txtSubtitle);

            txtTitle.setText(m);
            txtSubtitle.setText(s);

            if (i == 0)
            {
                progressUp.setVisibility(View.INVISIBLE);
            }
            else
            {
                progressUp.setVisibility(View.VISIBLE);
            }

            if (i == mensajes.size() - 1)
            {
                progressDown.setVisibility(View.INVISIBLE);
            }
            else
            {
                progressDown.setVisibility(View.VISIBLE);
            }

            int color = Utils.getColor(getContext(), R.color.grey300);
            if (s != null && !s.equals("-"))
            {
                color = Utils.getColor(getContext(), R.color.incidence500);
            }
            if (incidence.isClosed())
            {
                color = Utils.getColor(getContext(), R.color.success);
            }

            progressUp.setBackgroundColor(color);
            progressCircle.setCircleBackgroundColor(color);
            progressDown.setBackgroundColor(color);

            layoutMessages.addView(view);
        }
    }

    private void showValorationView(int valoration)
    {
        btnContinue.setVisibility(View.GONE);
        layoutFace.setVisibility(View.VISIBLE);

        int drawable = R.drawable.icon_face_very_bad;
        int message = R.string.incidence_valorated_very_bad;
        if (valoration == 1)
        {
            drawable = R.drawable.icon_face_bad;
            message = R.string.incidence_valorated_bad;
        }
        else if (valoration == 2)
        {
            drawable = R.drawable.icon_face_neutral;
            message = R.string.incidence_valorated_neutral;
        }
        else if (valoration == 3)
        {
            drawable = R.drawable.icon_face_good;
            message = R.string.incidence_valorated_good;
        }
        else if (valoration == 4)
        {
            drawable = R.drawable.icon_face_very_good;
            message = R.string.incidence_valorated_very_good;
        }

        imgFace.setImageDrawable(Utils.getDrawable(getContext(), drawable));
        txtFace.setText(message);
    }

    private void onClickContinue()
    {
        if (incidence.rate == null && incidence.isClosed())
        {
            goToValorate();
        }
        else
        {
            goToMap();
        }
    }

    private void goToValorate()
    {
        mListener.addFragmentAnimated(IncidenceValorationFragment.newInstance(incidence));
    }

    private void goToMap()
    {
        mListener.addFragmentAnimated(ReportMapFragment.newInstance(vehicle, incidence));
    }
}
