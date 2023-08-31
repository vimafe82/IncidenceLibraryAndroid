package es.incidence.core.fragment.vehicle;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.e510.commons.utils.DateUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;

import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.ListItemListener;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.utils.view.IField;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class VehicleInsuranceFragment extends ListFragment
{
    private static final String TAG = makeLogTag(VehicleInsuranceFragment.class);

    public static final int ROW_INSURANCE = 0;
    public static final int ROW_INSURANCE_NUMBER = 1;
    public static final int ROW_INSURANCE_TITULAR = 2;
    public static final int ROW_INSURANCE_CADUCITY = 3;

    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    public Vehicle vehicle;

    public static VehicleInsuranceFragment newInstance(Vehicle vehicle)
    {
        VehicleInsuranceFragment fragment = new VehicleInsuranceFragment();

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
            setNavigationTitle(getString(R.string.insurance_data));

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

            String name = (ve.insurance != null && ve.insurance.name != null) ? ve.insurance.name : "";
            ListItem l1 = new ListItem(getString(R.string.insurance), name);
            l1.object = ROW_INSURANCE;
            l1.editClicable = true;
            l1.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l1);

            String policyNumber = (ve.policy != null && ve.policy.policyNumber != null) ? ve.policy.policyNumber : "";
            ListItem l2 = new ListItem(getString(R.string.company_insurance_number), policyNumber);
            l2.object = ROW_INSURANCE_NUMBER;
            l2.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l2);

            String policyDNI = (ve.policy != null && ve.policy.dni != null) ? ve.policy.dni : "";
            ListItem l3 = new ListItem(getString(R.string.company_insurance_titular), policyDNI);
            l3.dropfield = true;
            if (ve.policy != null && ve.policy.identityType != null && ve.policy.identityType.id == 2) {
                l3.titleDrop = getString(R.string.nie);
            } else if (ve.policy != null && ve.policy.identityType != null && ve.policy.identityType.id == 3) {
                l3.titleDrop = getString(R.string.cif);
            } else {
                l3.titleDrop = getString(R.string.nif);
            }
            l3.menuDrop = R.menu.popup_menu_nif;
            l3.object = ROW_INSURANCE_TITULAR;
            l3.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l3);


            String birthday = (ve.policy != null && ve.policy.policyEnd != null) ? ve.policy.policyEnd : null;
            if (birthday != null)
            {
                Date date = DateUtils.parseDate(birthday, DateUtils.DATE);
                birthday = DateUtils.dateToString(date, DateUtils.DATE_ES);
            } else {
                birthday = "";
            }

            ListItem l4 = new ListItem(getString(R.string.company_insurance_caducity), birthday);
            l4.object = ROW_INSURANCE_CADUCITY;
            l4.type = IField.TYPE_DATE;
            l4.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            //l4.exclamation = true;
            //l4.exclamationMessage = getString(R.string.caducity_insurance_date_near);
            temp.add(l4);


            renewItems(temp);
        }
    }

    @Override
    public void reloadData() {
        loadData();
    }

    @Override
    public void onClickRow(Object object)
    {
    }

    public void onClickRightNavButton()
    {
        mListener.addFragmentAnimated(VehicleInsuranceEditFragment.newInstance(vehicle));
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