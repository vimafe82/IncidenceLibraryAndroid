package es.incidence.core.fragment.vehicle;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Driver;
import es.incidence.core.domain.User;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.common.ListFragment;

public class VehicleDriversFragment extends ListFragment
{
    private static final String TAG = makeLogTag(VehicleDriversFragment.class);

    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    public Vehicle vehicle;

    public static VehicleDriversFragment newInstance(Vehicle vehicle)
    {
        VehicleDriversFragment fragment = new VehicleDriversFragment();

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
    public void loadData()
    {
        if (vehicle != null)
        {
            setNavigationTitle(getString(R.string.drivers));

            if (vehicle.drivers != null)
            {
                /*
                setNavigationButtonRight(R.drawable.icon_edit);
                setOnNavigationButtonRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickRightNavButton();
                    }
                });
                */

                User user = Core.getUser();
                boolean isUserPrimary = Core.isUserPrimaryForVehicle(vehicle);
                ArrayList<ListItem> temp = new ArrayList<>();

                for (int i = 0; i < vehicle.drivers.size(); i++)
                {
                    Driver driver = vehicle.drivers.get(i);

                    String title = driver.isTypePrimary() ? getString(R.string.driver_primary) : getString(R.string.driver_secondary);

                    ListItem l1 = new ListItem(title, driver.name);
                    l1.object = driver;

                    boolean isDriverUser = false;
                    if (user.id != null && driver.id == Integer.parseInt(user.id))
                    {
                        isDriverUser = true;
                    }

                    if (driver.isTypePrimary())
                    {
                        l1.clicable = false;
                    }
                    else
                    {
                        if (isUserPrimary || isDriverUser)
                        {
                            l1.rightDrawable = Utils.getDrawable(getContext(), R.drawable.icon_edit);
                            l1.rightDrawableSize = Utils.dpToPx(16);
                            l1.clicable = true;
                        }
                        else
                        {
                            l1.clicable = false;
                        }
                    }

                    //l1.rightDrawable = Utils.getDrawable(getContext(), R.drawable.icon_delete_x);
                    //l1.rightDrawableSize = Utils.dpToPx(24);

                    temp.add(l1);
                }

                renewItems(temp);
            }

        }
    }

    @Override
    public void onClickRow(Object object)
    {
        ListItem listItem = (ListItem) object;
        Driver driver = (Driver) listItem.object;

        mListener.addFragmentAnimated(VehicleDriversEditFragment.newInstance(driver, vehicle));
    }

    public void onClickRightNavButton()
    {
        //mListener.addFragmentAnimated(VehicleDriversEditFragment.newInstance(vehicle));
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
                if (vehicle.drivers != null)
                {
                    //ordenamos el conductor principal primero.
                    ArrayList<Driver> sorted = new ArrayList<>();
                    for (int i = 0; i < vehicle.drivers.size(); i++)
                    {
                        Driver driver = vehicle.drivers.get(i);
                        if (driver.isTypePrimary())
                        {
                            sorted.add(0, driver);
                        }
                        else
                        {
                            sorted.add(driver);
                        }
                    }
                }
                loadData();
            }
        }
    }
}