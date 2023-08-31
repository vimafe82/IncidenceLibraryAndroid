package es.incidence.core.fragment.vehicle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Driver;
import es.incidence.core.domain.User;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INotification;

public class VehicleDriversEditFragment extends VehicleDriversFragment
{
    public static final String KEY_DRIVER = "KEY_DRIVER";
    public Driver driver;

    private int typeDriver;
    private IButton btnContinue;

    public static VehicleDriversEditFragment newInstance(Driver driver, Vehicle vehicle)
    {
        VehicleDriversEditFragment fragment = new VehicleDriversEditFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DRIVER, driver);
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.EDITABLE;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            driver = getArguments().getParcelable(KEY_DRIVER);
        }
    }

    @Override
    public void loadData()
    {
        setNavigationTitle(getString(R.string.drivers));
        setNavigationButtonRight(R.drawable.icon_close);
        setNavigationButtonRightTintColor(Utils.getColor(getContext(), R.color.black600));
        setOnNavigationButtonRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRightNavButton();
            }
        });

        ArrayList<ListItem> temp = new ArrayList<>();

        ListItem l1 = new ListItem("", driver.name);
        l1.object = driver;
        l1.editable = false;
        l1.idBackgroundColor = R.color.grey200;
        l1.idTextColor = R.color.black400;
        temp.add(l1);

        typeDriver = driver.type;
        String value = driver.isTypePrimary() ? getString(R.string.driver_primary) : getString(R.string.driver_secondary);
        ListItem l2 = new ListItem("", value);
        l2.rightDrawable = Utils.getDrawable(getContext(), R.drawable.icon_arrow_down);
        l2.rightDrawableSize = Utils.dpToPx(20);
        l2.object = driver;
        l2.editClicable = true;
        temp.add(l2);

        renewItems(temp);


        layoutBottom.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_continue_dismiss, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
        TextView txtAddLater = view.findViewById(R.id.txtAddLater);
        txtAddLater.setTextColor(Utils.getColor(getContext(), R.color.error));
        txtAddLater.setText(getString(R.string.delete));
        txtAddLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDriverPopUp();
            }
        });
        FontUtils.setTypeValueText(txtAddLater, Constants.FONT_SEMIBOLD, getContext());

        btnContinue = view.findViewById(R.id.btnContinueColor);
        btnContinue.setPrimaryColors();
        btnContinue.setText(getString(R.string.save));
        FontUtils.setTypeValueText(btnContinue, Constants.FONT_SEMIBOLD, getContext());
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        layoutBottom.addView(view);
    }

    @Override
    public void onClickRightNavButton()
    {
        mListener.cleanAllBackStackEntries();
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            //Driver driver = (Driver) listItem.object;

            showTypeDriverPopUp(listItem);
        }
    }

    private void showDeleteDriverPopUp()
    {
        String title = null;
        String message = getString(R.string.delete_driver_message, driver.name);
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

                        User user = Core.getUser();
                        boolean isDriverUserTemp = false;
                        if (user.id != null && driver.id == Integer.parseInt(user.id))
                        {
                            isDriverUserTemp = true;
                        }
                        final boolean isDriverUser = isDriverUserTemp;

                        showHud();
                        Api.deleteVehicleDriver(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response) {
                                hideHud();
                                if (response.isSuccess())
                                {
                                    if (isDriverUser)
                                    {
                                        Core.deleteVehicle(vehicle);
                                        EventBus.getDefault().post(new Event(EventCode.VEHICLE_DELETED, vehicle));
                                        mListener.cleanAllBackStackEntries();
                                    }
                                    else
                                    {
                                        ArrayList<Driver> temp = new ArrayList<>();
                                        for (int i = 0; i < vehicle.drivers.size(); i++) {
                                            Driver d = vehicle.drivers.get(i);
                                            if (d.id != driver.id) {
                                                temp.add(d);
                                            }
                                        }
                                        vehicle.drivers = temp;
                                        Core.saveVehicle(vehicle);
                                        EventBus.getDefault().post(new Event(EventCode.VEHICLE_UPDATED, vehicle));

                                        closeThis();
                                    }
                                }
                                else
                                {
                                    onBadResponse(response);
                                }
                            }
                        }, vehicle.id, driver.id + "");
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }


    private void showTypeDriverPopUp(ListItem listItem)
    {
        String title = null;
        String message = getString(R.string.select_type_driver);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.driver_primary));
        options.add(getString(R.string.driver_secondary));
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
                        //primary
                        typeDriver = 1;
                        listItem.subtitle = getString(R.string.driver_primary);
                        reloadData();
                    }
                    else if (index == 1)
                    {
                        //secondary
                        typeDriver = 0;
                        listItem.subtitle = getString(R.string.driver_secondary);
                        reloadData();
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    private void save()
    {
        if (typeDriver != driver.type)
        {
            boolean isUserPrimary = Core.isUserPrimaryForVehicle(vehicle);

            if (isUserPrimary)
            {   //El usuario es el principal con lo que puede gestionar el cambio directamente
                showHud();
                Api.changeVehicleDriver(new IRequestListener() {
                    @Override
                    public void onFinish(IResponse response) {
                        hideHud();
                        if (response.isSuccess())
                        {
                            User user = Core.getUser();

                            for (int i = 0; i < vehicle.drivers.size(); i++) {
                                Driver d = vehicle.drivers.get(i);

                                if (user.id != null && d.id == Integer.parseInt(user.id))
                                {   //el usuario pasa a ser secundario.
                                    d.type = 0;
                                }
                                else if (d.id == driver.id) {
                                    d.type = typeDriver;
                                }
                            }
                            Core.saveVehicle(vehicle);
                            EventBus.getDefault().post(new Event(EventCode.VEHICLE_UPDATED, vehicle));


                            closeThis();
                        }
                        else
                        {
                            onBadResponse(response);
                        }
                    }
                }, vehicle.id, driver.id + "");
            }
            else
            {   //El usuario es el secundario y solicita ser principal.
                showHud();
                Api.requestAddVehicleDriver(new IRequestListener() {
                    @Override
                    public void onFinish(IResponse response) {
                        hideHud();
                        if (response.isSuccess())
                        {
                            EventBus.getDefault().post(new Event(EventCode.VEHICLE_DRIVER_UPDATED));
                            closeThis();
                        }
                        else
                        {
                            onBadResponse(response);
                        }
                    }
                }, vehicle.id, typeDriver+"");
            }

        }
    }
}