package es.incidence.core.fragment.vehicle;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.ListItemListener;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.common.ListFragment;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class VehicleDataFragment extends ListFragment
{
    private static final String TAG = makeLogTag(VehicleDataFragment.class);

    public static final int ROW_PLATE = 0;
    public static final int ROW_YEAR_PLATE = 1;
    public static final int ROW_BRAND = 2;
    public static final int ROW_MODEL = 3;
    public static final int ROW_COLOR = 4;

    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    public Vehicle vehicle;

    public static VehicleDataFragment newInstance(Vehicle vehicle)
    {
        VehicleDataFragment fragment = new VehicleDataFragment();

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
    public void reloadData() {
        loadData();
    }

    public Vehicle getVehicle()
    {
        return vehicle;
    }

    @Override
    public void loadData()
    {
        Vehicle ve = getVehicle();

        if (ve != null)
        {
            setNavigationTitle(getString(R.string.vehicle_data));

            boolean isUserPrimary = Core.isUserPrimaryForVehicle(ve);
            if (isUserPrimary)
            {
                setNavigationButtonRight(R.drawable.icon_edit);
                setOnNavigationButtonRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickRightNavButton();
                    }
                });
            }

            ArrayList<ListItem> temp = new ArrayList<>();

            ListItem l1 = new ListItem(getString(R.string.matricula), ve.licensePlate);
            l1.object = ROW_PLATE;
            l1.editable = false;
            temp.add(l1);

            ListItem l2 = new ListItem(getString(R.string.matricula_year), ve.registrationYear);
            l2.object = ROW_YEAR_PLATE;
            l2.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l2);

            ListItem l3 = new ListItem(getString(R.string.brand), ve.brand);
            l3.object = ROW_BRAND;
            l3.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l3);

            ListItem l4 = new ListItem(getString(R.string.model), ve.model);
            l4.object = ROW_MODEL;
            l4.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l4);

            String colorName = (ve.color != null) ? ve.color.name : "";
            ListItem l5 = new ListItem(getString(R.string.color), colorName);
            l5.object = ROW_COLOR;
            l5.editClicable = true;
            l5.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l5);

            renewItems(temp);
        }
    }

    @Override
    public void onClickRow(Object object)
    {
    }

    public void onClickRightNavButton()
    {
        mListener.addFragmentAnimated(VehicleDataEditFragment.newInstance(vehicle));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.VEHICLE_UPDATED)
        {
            Vehicle temp = (Vehicle) event.object;
            if (temp.id.equals(vehicle.id))
            {
                vehicle = temp;
                reloadData();
            }
        }
    }

    public void onChangeValues()
    {
    }
}