package es.incidence.core.fragment.vehicle;

import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Beacon;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.add.AddBeaconFragment;
import es.incidence.core.fragment.beacon.BeaconDataEditFragment;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.INotification;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class VehicleBeaconFragment extends ListFragment
{
    private static final String TAG = makeLogTag(VehicleBeaconFragment.class);

    public static final int ROW_NAME = 0;
    public static final int ROW_MODEL = 1;

    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    public Vehicle vehicle;

    private View addBeaconView;

    public static VehicleBeaconFragment newInstance(Vehicle vehicle)
    {
        VehicleBeaconFragment fragment = new VehicleBeaconFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.vehicles;
    }

    @Override
    public boolean needEventBus() {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            vehicle = getArguments().getParcelable(KEY_VEHICLE);
        }
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.TITLE_SUBTITLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        layoutBottom.removeAllViews();

        boolean isUserPrimary = Core.isUserPrimaryForVehicle(vehicle);
        if (isUserPrimary)
        {
            TextView txtDesvincular = new TextView(getContext());
            FontUtils.setTypeValueText(txtDesvincular, Constants.FONT_SEMIBOLD, getContext());
            txtDesvincular.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                txtDesvincular.setLineHeight(Utils.dpToPx(24));
            }
            txtDesvincular.setTextColor(Utils.getColor(getContext(), R.color.error100));
            txtDesvincular.setText(R.string.unlink);
            txtDesvincular.setGravity(Gravity.CENTER);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = Utils.dpToPx(40);
            txtDesvincular.setLayoutParams(params);

            layoutBottom.addView(txtDesvincular);
            layoutBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Desvincular
                    showUnlinkPopUp();
                }
            });

            LayoutInflater inflater = LayoutInflater.from(getContext());
            addBeaconView = inflater.inflate(R.layout.row_list, null);
            FontUtils.setTypeValueText(addBeaconView, Constants.FONT_REGULAR, getContext());
            addBeaconView.findViewById(R.id.imgLeft).setVisibility(View.GONE);
            TextView txtTitle = addBeaconView.findViewById(R.id.txtTitle);
            txtTitle.setText(getString(R.string.add_new_device));
            txtTitle.setTextColor(Utils.getColor(getContext(), R.color.incidence500));
            ImageView imgRight = addBeaconView.findViewById(R.id.imgRight);
            imgRight.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.icon_plus));

            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) imgRight.getLayoutParams();
            params2.width = Utils.dpToPx(14);
            params2.height = Utils.dpToPx(14);
            params2.rightMargin = Utils.dpToPx(21);
            imgRight.setLayoutParams(params2);

            addBeaconView.setVisibility(View.GONE);
            addBeaconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.addFragmentAnimated(AddBeaconFragment.newInstance(0, 1, vehicle,true));
                }
            });
            layoutContent.addView(addBeaconView);
        }
    }


    @Override
    public void loadData()
    {
        if (vehicle != null)
        {
            setNavigationTitle(getString(R.string.beacon));

            ArrayList<ListItem> temp = new ArrayList<>();

            if (vehicle.beacon != null)
            {
                boolean isUserPrimary = Core.isUserPrimaryForVehicle(vehicle);
                if (isUserPrimary)
                {
                    layoutBottom.setVisibility(View.VISIBLE);
                    addBeaconView.setVisibility(View.GONE);

                    setNavigationButtonRight(R.drawable.icon_edit);
                    setOnNavigationButtonRightClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onClickRightNavButton();
                        }
                    });
                }



                String name = "";
                if (vehicle.beacon.name != null)
                    name = vehicle.beacon.name;

                ListItem l1 = new ListItem(getString(R.string.name), name);
                l1.object = ROW_NAME;
                temp.add(l1);

                ListItem l2 = new ListItem(getString(R.string.model), (vehicle.beacon.beaconType != null) ? vehicle.beacon.beaconType.name : "");
                l2.object = ROW_MODEL;
                temp.add(l2);
            }
            else
            {
                if (layoutBottom != null && addBeaconView != null)
                {
                    layoutBottom.setVisibility(View.GONE);
                    addBeaconView.setVisibility(View.VISIBLE);
                }
            }

            renewItems(temp);
        }
    }

    @Override
    public void onClickRow(Object object)
    {
    }

    public void onClickRightNavButton()
    {
        if (vehicle.beacon.vehicle == null)
        {
            vehicle.beacon.vehicle = new Vehicle();
            vehicle.beacon.vehicle.id = vehicle.id;
        }

        mListener.addFragmentAnimated(BeaconDataEditFragment.newInstance(vehicle.beacon));
    }

    private void showUnlinkPopUp()
    {
        hideKeyboard();

        String title = null;
        String message = getString(R.string.unlink_beacon_message, vehicle.getName());
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.cancel));
        options.add(getString(R.string.unlink));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //cancela
                    }
                    else if (index == 1)
                    {
                        //elimina
                        showHud();
                        Api.deleteVehicleBeacon(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response) {
                                hideHud();
                                if (response.isSuccess())
                                {
                                    vehicle.beacon = null;
                                    Core.saveVehicle(vehicle);
                                    EventBus.getDefault().post(new Event(EventCode.VEHICLE_UPDATED, vehicle));
                                    closeThis();
                                }
                                else
                                {
                                    onBadResponse(response);
                                }
                            }
                        }, vehicle);
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.BEACON_UPDATED)
        {
            vehicle.beacon = (Beacon) event.object;
            loadData();
        }
        else if (event.code == EventCode.VEHICLE_UPDATED)
        {
            loadData();
        }
    }
}