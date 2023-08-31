package es.incidence.core.fragment.vehicle;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.fragment.incidence.IncidenceListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.INotification;

public class VehicleOptionsFragment extends ListFragment
{
    private static final String TAG = makeLogTag(VehicleOptionsFragment.class);

    private static final int ROW_VEHICLE_DATA = 0;
    private static final int ROW_INSURANCE_DATA = 1;
    private static final int ROW_BEACON = 2;
    private static final int ROW_INCIDENCES = 3;
    private static final int ROW_DRIVERS = 4;
    private static final int ROW_DELETE_VEHICLE = 5;


    private static final String KEY_VEHICLE = "KEY_VEHICLE";
    private Vehicle vehicle;

    public static VehicleOptionsFragment newInstance(Vehicle vehicle)
    {
        VehicleOptionsFragment fragment = new VehicleOptionsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public boolean needEventBus() {
        return true;
    }

    @Override
    public int getTitleId() {
        return R.string.vehicles;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            vehicle = getArguments().getParcelable(KEY_VEHICLE);
        }
    }

    @Override
    public void reloadData() {
        setNavigationTitle(vehicle.getName());
        setNavigationTitleRight(vehicle.licensePlate);
    }

    @Override
    public void loadData()
    {
        if (vehicle != null)
        {
            reloadData();

            ArrayList<ListItem> temp = new ArrayList<>();

            int colorTint = Utils.getColor(getContext(), R.color.black600);
            int drawableSize = Utils.dpToPx(20);

            Drawable drawable = Utils.getDrawable(getContext(), "icon_vehicles");
            drawable.setTint(colorTint);
            ListItem l1 = new ListItem(getString(R.string.vehicle_data), drawable, ROW_VEHICLE_DATA);
            l1.leftDrawableSize = drawableSize;
            temp.add(l1);

            drawable = Utils.getDrawable(getContext(), "icon_document");
            drawable.setTint(colorTint);
            ListItem l2 = new ListItem(getString(R.string.insurance_data), drawable, ROW_INSURANCE_DATA);
            l2.leftDrawableSize = drawableSize;
            l2.exclamation = vehicle.hasPolicyIncompleted();
            temp.add(l2);

            drawable = Utils.getDrawable(getContext(), "icon_devices");
            drawable.setTint(colorTint);
            ListItem l3 = new ListItem(getString(R.string.beacon), drawable, ROW_BEACON);
            l3.leftDrawableSize = drawableSize;
            temp.add(l3);

            drawable = Utils.getDrawable(getContext(), "icon_incidences");
            drawable.setTint(colorTint);
            String count = vehicle.incidences != null ? " (" + vehicle.incidences.size() + ")" : "";
            ListItem l4 = new ListItem(getString(R.string.incidences) + count, drawable, ROW_INCIDENCES);
            l4.leftDrawableSize = drawableSize;
            temp.add(l4);

            drawable = Utils.getDrawable(getContext(), "icon_user");
            drawable.setTint(colorTint);
            count = vehicle.drivers != null ? " (" + vehicle.drivers.size() + ")" : "";
            ListItem l5 = new ListItem(getString(R.string.drivers) + count, drawable, ROW_DRIVERS);
            l5.leftDrawableSize = drawableSize;
            temp.add(l5);

            renewItems(temp);

            //bottom
            ListItem lBottom = new ListItem(getString(R.string.delete_vechicle), ROW_DELETE_VEHICLE);
            lBottom.titleColor = Utils.getColor(getContext(), R.color.error);
            lBottom.arrowColor = Utils.getColor(getContext(), R.color.error);
            addRowBottom(lBottom);
        }
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            int row = (int) listItem.object;

            if (row == ROW_VEHICLE_DATA)
            {
                mListener.addFragmentAnimated(VehicleDataFragment.newInstance(vehicle));
            }
            else if (row == ROW_INSURANCE_DATA)
            {
                mListener.addFragmentAnimated(VehicleInsuranceFragment.newInstance(vehicle));
            }
            else if (row == ROW_BEACON)
            {
                mListener.addFragmentAnimated(VehicleBeaconFragment.newInstance(vehicle));
            }
            else if (row == ROW_INCIDENCES)
            {
                mListener.addFragmentAnimated(IncidenceListFragment.newInstance(vehicle));
            }
            else if (row == ROW_DRIVERS)
            {
                mListener.addFragmentAnimated(VehicleDriversFragment.newInstance(vehicle));
            }
            else if (row == ROW_DELETE_VEHICLE)
            {
                showDeleteVehiclePopUp();
            }
        }
    }

    private void showDeleteVehiclePopUp()
    {
        String title = null;
        boolean isUserPrimary = Core.isUserPrimaryForVehicle(vehicle);
        String message = (isUserPrimary && vehicle.drivers != null && vehicle.drivers.size() > 1) ? getString(R.string.delete_vechicle_message_primary) : getString(R.string.delete_vechicle_message);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.cancel));
        options.add(getString(R.string.delete));
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

                        boolean isUserPrimary = Core.isUserPrimaryForVehicle(vehicle);
                        if (isUserPrimary)
                        {
                            showHud();
                            Api.deleteVehicle(new IRequestListener() {
                                @Override
                                public void onFinish(IResponse response) {
                                    hideHud();
                                    if (response.isSuccess())
                                    {
                                        Core.deleteVehicle(vehicle);
                                        EventBus.getDefault().post(new Event(EventCode.VEHICLE_DELETED, vehicle));
                                        closeThis();
                                    }
                                    else
                                    {
                                        onBadResponse(response);
                                    }
                                }
                            }, vehicle);
                        }
                        else //Se elimina como conductor.
                        {
                            showHud();
                            Api.deleteVehicleDriver(new IRequestListener() {
                                @Override
                                public void onFinish(IResponse response) {
                                    hideHud();
                                    if (response.isSuccess())
                                    {
                                        Core.deleteVehicle(vehicle);
                                        EventBus.getDefault().post(new Event(EventCode.VEHICLE_DELETED, vehicle));
                                        closeThis();
                                    }
                                    else
                                    {
                                        onBadResponse(response);
                                    }
                                }
                            }, vehicle.id, Core.getUser().id + "");
                        }
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
}
