package es.incidence.core.fragment.vehicle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.DateUtils;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.IdentityType;
import es.incidence.core.domain.Policy;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INotification;

public class VehicleInsuranceEditFragment extends VehicleInsuranceFragment
{
    //utilizamos una copia para no afectar en el objeto si no se guarda
    private Vehicle vehicleEditing;
    private boolean hasChanges = false;

    public static VehicleInsuranceEditFragment newInstance(Vehicle vehicle)
    {
        VehicleInsuranceEditFragment fragment = new VehicleInsuranceEditFragment();

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
    public void loadData() {
        super.loadData();

        setNavigationButtonRight(R.drawable.icon_close);
        setNavigationButtonRightTintColor(Utils.getColor(getContext(), R.color.black600));
    }

    @Override
    public Vehicle getVehicle() {
        return vehicleEditing;
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            int row = (int) listItem.object;

            if (row == ROW_INSURANCE)
            {
                mListener.addFragmentAnimated(VehicleInsuranceListFragment.newInstance(vehicleEditing));
            }
        }
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

    private void save(boolean cleanAll)
    {
        String policyNumber = getItem(ROW_INSURANCE_NUMBER).subtitle;
        ListItem listItemDNI = getItem(ROW_INSURANCE_TITULAR);
        String dni = listItemDNI.subtitle;
        String caducity = getItem(ROW_INSURANCE_CADUCITY).subtitle;
        if (caducity != null && !caducity.equals("-") && caducity.length() > 0)
        {
            Date date = DateUtils.parseDate(caducity, DateUtils.DATE_ES);
            caducity = DateUtils.dateToString(date, DateUtils.DATE);
        }
        else
        {
            caducity = null;
        }

        Policy policy = new Policy();
        policy.policyNumber = policyNumber;
        //policy.identityType = vehicleEditing.policy.identityType;
        IdentityType identityType = new IdentityType();
        identityType.name = listItemDNI.titleDrop;
        if (identityType.name != null && identityType.name.equals("DNI")) {
            identityType.id = 1;
        } else if (identityType.name != null && identityType.name.equals("NIE")) {
            identityType.id = 2;
        } else if (identityType.name != null && identityType.name.equals("CIF")) {
            identityType.id = 3;
        } else {
            identityType.id = 1;
        }
        policy.identityType = identityType;
        policy.dni = dni;
        policy.policyEnd = caducity;

        showHud();
        Api.addVehiclePolicy(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess())
                {
                    hasChanges = false;

                    vehicle = vehicleEditing;
                    vehicle.policy = (Policy) response.get("policy", Policy.class);
                    Core.saveVehicle(vehicle);
                    EventBus.getDefault().post(new Event(EventCode.VEHICLE_UPDATED, vehicle));

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
        }, vehicleEditing, policy);
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