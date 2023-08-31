package es.incidence.core.fragment.beacon;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import es.incidence.core.Constants;
import es.incidence.core.R;
import es.incidence.core.adapter.IncidenceDGTListAdapter;
import es.incidence.core.domain.Beacon;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INavigation;
import es.incidence.core.utils.view.IStepper;

public class BeaconDetailInfoFragment extends IFragment
{
    private static final String TAG = makeLogTag(BeaconDetailInfoFragment.class);

    public static final String KEY_BEACON = "KEY_BEACON";

    private INavigation navigation;
    public IStepper stepper;
    public IButton btnBlue;

    public TextView txtHeaderBeacon;
    public ImageView imageBeacon;

    private Beacon beacon;

    private int position = 0;

    public static BeaconDetailInfoFragment newInstance(Beacon beacon)
    {
        BeaconDetailInfoFragment fragment = new BeaconDetailInfoFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_BEACON, beacon);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.beacon;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            beacon = getArguments().getParcelable(KEY_BEACON);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_beacon_detail_info, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);
        navigation.setBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onBackPressed()) {
                    closeThis();
                }
            }
        });

        stepper = rootView.findViewById(R.id.istepper);
        float[] steps = new float[]{0.5f, 0f, 0f};
        stepper.init(this, steps);

        String title = beacon.name;
        navigation.init(this, title, true);

        btnBlue = rootView.findViewById(R.id.btnBlue);
        btnBlue.setText(getString(R.string.continuar));
        btnBlue.setPrimaryColors();
        FontUtils.setTypeValueText(btnBlue, Constants.FONT_SEMIBOLD, getContext());
        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBlue();
            }
        });

        txtHeaderBeacon = rootView.findViewById(R.id.txtHeaderBeacon);
        imageBeacon = rootView.findViewById(R.id.imageBeacon);
    }

    private void onClickBlue() {

        if (position == 0) {
            txtHeaderBeacon.setText(getString(R.string.device_desc_info2));
            imageBeacon.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.device_info_2));
            position = 1;
        } else if (position == 1) {
            txtHeaderBeacon.setText(getString(R.string.device_desc_info3));
            imageBeacon.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.device_info_3));
            position = 2;

            btnBlue.setText(getString(R.string.device_desc_info_finish));
        } else if (position == 2) {
            closeThis();
        }
    }
}