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
import es.incidence.core.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INotification;

public class VehicleDataEditFragment extends VehicleDataFragment
{
    //utilizamos una copia para no afectar en el objeto si no se guarda
    private Vehicle vehicleEditing;
    private boolean hasChanges = false;

    public static VehicleDataEditFragment newInstance(Vehicle vehicle)
    {
        VehicleDataEditFragment fragment = new VehicleDataEditFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicleEditing = new Vehicle(vehicle);
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.EDITABLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        layoutBottom.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_continue_dismiss, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
        TextView txtAddLater = view.findViewById(R.id.txtAddLater);
        txtAddLater.setText(getString(R.string.cancel));
        txtAddLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelClose();
            }
        });
        FontUtils.setTypeValueText(txtAddLater, Constants.FONT_SEMIBOLD, getContext());

        IButton btnContinue = view.findViewById(R.id.btnContinueColor);
        btnContinue.setPrimaryColors();
        btnContinue.setText(getString(R.string.save));
        FontUtils.setTypeValueText(btnContinue, Constants.FONT_SEMIBOLD, getContext());
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(false);
            }
        });

        layoutBottom.addView(view);
    }

    @Override
    public Vehicle getVehicle() {
        return vehicleEditing;
    }

    @Override
    public void loadData()
    {
        super.loadData();

        setNavigationButtonRight(R.drawable.icon_close);
        setNavigationButtonRightTintColor(Utils.getColor(getContext(), R.color.black600));
    }

    @Override
    public void reloadData() {

        String licensePlate = getItem(ROW_PLATE).subtitle;
        String yearPlate = getItem(ROW_YEAR_PLATE).subtitle;
        String brand = getItem(ROW_BRAND).subtitle;
        String model = getItem(ROW_MODEL).subtitle;

        vehicleEditing.licensePlate = licensePlate;
        vehicleEditing.registrationYear = yearPlate;
        vehicleEditing.brand = brand;
        vehicleEditing.model = model;

        super.reloadData();
    }

    @Override
    public void onClickRightNavButton()
    {
        if (hasChanges)
        {
            showSavePopUp(true);
        }
        else
        {
            mListener.cleanAllBackStackEntries();
        }
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            int row = (int) listItem.object;

            if (row == ROW_COLOR)
            {
                mListener.addFragmentAnimated(VehicleDataEditColorFragment.newInstance(vehicleEditing));
            }
        }
    }

    private void save(boolean cleanAll)
    {
        String licensePlate = getItem(ROW_PLATE).subtitle;
        String yearPlate = getItem(ROW_YEAR_PLATE).subtitle;
        String brand = getItem(ROW_BRAND).subtitle;
        String model = getItem(ROW_MODEL).subtitle;

        vehicleEditing.licensePlate = licensePlate;
        vehicleEditing.registrationYear = yearPlate;
        vehicleEditing.brand = brand;
        vehicleEditing.model = model;

        showHud();
        Api.updateVehicle(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess())
                {
                    hasChanges = false;

                    Vehicle v = (Vehicle) response.get("vehicle", Vehicle.class);
                    Core.saveVehicle(v);
                    EventBus.getDefault().post(new Event(EventCode.VEHICLE_UPDATED, v));

                    if (cleanAll) {
                        mListener.cleanAllBackStackEntries();
                    } else {
                        closeThis();
                    }
                }
                else
                {
                    onBadResponse(response);
                }
            }
        }, vehicleEditing);
    }

    @Override
    public void onChangeValues() {
        hasChanges = true;
    }

    @Override
    public boolean onBackPressed() {
        if (hasChanges)
        {
            showSavePopUp(false);

            return true;
        }

        return super.onBackPressed();
    }

    private void cancelClose()
    {
        if (hasChanges)
        {
            showSavePopUp(false);
        }
        else
        {
            closeThis();
        }
    }


    private void showSavePopUp(boolean cleanAll)
    {
        hideKeyboard();

        String title = getString(R.string.wish_continue);;
        String message = getString(R.string.no_saved_changes);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.save_and_close));
        options.add(getString(R.string.no_save));
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
                        //save_and_close
                        save(cleanAll);
                    }
                    else if (index == 1)
                    {
                        //no_save
                        hasChanges = false;
                        if (cleanAll) {
                            mListener.cleanAllBackStackEntries();
                        } else {
                            closeThis();
                        }
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }
}
