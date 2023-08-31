package es.incidence.core.fragment.vehicle;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.ScreenUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.swipe.OnSwipeTouchListener;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.R;
import es.incidence.core.adapter.InsuranceListAdapter;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Insurance;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.IField;

public class VehicleInsuranceListFragment extends VehicleInsuranceFragment {

    private IField txtSearchInsurance;
    private ArrayList<Insurance> allInsurances;
    private ArrayList<Object> insurances = new ArrayList<>();
    private InsuranceListAdapter insuranceListAdapter;
    private ListView listViewInsurances;
    private ScrollView scrollInsuranceNotFound;
    private TextView txtInsuranceNotFound;
    private IField txtNewInsurance;
    private IButton btnNewInsurance;

    public static VehicleInsuranceListFragment newInstance(Vehicle vehicle)
    {
        VehicleInsuranceListFragment fragment = new VehicleInsuranceListFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.EDITABLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        layoutContent.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_insurance_list, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        txtSearchInsurance = view.findViewById(R.id.field);
        txtSearchInsurance.setWithGradient(false);
        txtSearchInsurance.setWithValidation(false);
        txtSearchInsurance.setImageLeft(Utils.getDrawable(getContext(), R.drawable.icon_search));
        txtSearchInsurance.setHint(getString(R.string.search_insurance));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String text = txtSearchInsurance.getText();
                reloadInsurances(text);
            }
        };
        txtSearchInsurance.setTextWatcher(textWatcher);


        listViewInsurances = view.findViewById(R.id.listView);
        insuranceListAdapter = new InsuranceListAdapter(this, insurances);
        listViewInsurances.setAdapter(insuranceListAdapter);

        scrollInsuranceNotFound = view.findViewById(R.id.scrollInsuranceNotFound);
        view.findViewById(R.id.layoutInsuListRoot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();}
        });
        scrollInsuranceNotFound.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            public void onSwipeRight() {
            }
            public void onSwipeLeft() {
            }
            @Override
            public void onClick() {
                super.onClick();
                hideKeyboard();
            }
        });
        txtInsuranceNotFound = view.findViewById(R.id.txtNotFound);
        txtNewInsurance = view.findViewById(R.id.field_new);
        txtNewInsurance.setHint(getString(R.string.new_insurance));
        txtNewInsurance.setTitle(getString(R.string.new_insurance));
        btnNewInsurance = view.findViewById(R.id.btnAddInsurance);
        btnNewInsurance.setPrimaryColors();
        btnNewInsurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewInsurance();
            }
        });
        FontUtils.setTypeValueText(btnNewInsurance, Constants.FONT_SEMIBOLD, getContext());

        layoutContent.addView(view);
    }

    private void reloadInsurances(String searchString)
    {
        ArrayList<Object> tempFeatured = new ArrayList<>();
        if (allInsurances != null)
        {
            for (int i = 0; i < allInsurances.size(); i++)
            {
                Insurance insurance = allInsurances.get(i);

                if (searchString != null && searchString.length() > 0)
                {
                    if (!insurance.name.toLowerCase().contains(searchString.toLowerCase()))
                    {
                        continue;
                    }
                }

                if (insurance.relation == 1) {
                    tempFeatured.add(insurance);
                }
            }
        }

        ArrayList<Object> temp = new ArrayList<>();
        if (allInsurances != null)
        {
            if (tempFeatured.size() > 0)
            {
                temp.add(getString(R.string.featured_insurance));
                temp.addAll(tempFeatured);
            }


            ArrayList<Object> temp2 = new ArrayList<>();

            for (int i = 0; i < allInsurances.size(); i++)
            {
                Insurance insurance = allInsurances.get(i);

                if (searchString != null && searchString.length() > 0)
                {
                    if (!insurance.name.toLowerCase().contains(searchString.toLowerCase()))
                    {
                        continue;
                    }
                }

                temp2.add(insurance);
            }

            if (temp2.size() > 0)
            {
                temp.add(getString(R.string.list_insurance));
                temp.addAll(temp2);
            }
        }

        insurances.clear();
        insurances.addAll(temp);
        insuranceListAdapter.notifyDataSetChanged();

        if (insurances.size() == 0 && searchString != null && searchString.length() > 0)
        {
            scrollInsuranceNotFound.setVisibility(View.VISIBLE);
            txtInsuranceNotFound.setVisibility(View.VISIBLE);
            txtNewInsurance.setVisibility(View.VISIBLE);
            txtNewInsurance.setText(searchString);
            txtNewInsurance.showOK();
            btnNewInsurance.setVisibility(View.VISIBLE);
            listViewInsurances.setVisibility(View.GONE);
        }
        else
        {
            scrollInsuranceNotFound.setVisibility(View.GONE);
            txtInsuranceNotFound.setVisibility(View.GONE);
            txtNewInsurance.setVisibility(View.GONE);
            btnNewInsurance.setVisibility(View.GONE);
            listViewInsurances.setVisibility(View.VISIBLE);
        }
    }
    private void loadInsurances()
    {
        showHud();
        Api.getInsurances(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess())
                {
                    allInsurances = response.getList("insurances", Insurance.class);

                    reloadInsurances(txtSearchInsurance.getText());
                }
                else
                {
                    onBadResponse(response);
                }
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();
        hideNavigationButtonRight();

        loadInsurances();
    }

    @Override
    public void onClickRow(Object object) {
        if (object instanceof Insurance)
        {
            Insurance insurance = (Insurance) object;
            vehicle.insurance = insurance;

            BaseFragment baseFragment = mListener.getPenultimFragment();
            if (baseFragment != null) {
                baseFragment.reloadData();
            }
            closeThis();
        }
    }

    private void addNewInsurance()
    {
        String name = txtNewInsurance.getText();
        if (name != null && name.length() > 0)
        {
            showHud();
            Api.addInsurance(new IRequestListener() {
                @Override
                public void onFinish(IResponse response) {
                    hideHud();
                    if (response.isSuccess())
                    {
                        Insurance insurance = (Insurance) response.get("insurance", Insurance.class);

                        vehicle.insurance = insurance;

                        BaseFragment baseFragment = mListener.getPenultimFragment();
                        if (baseFragment != null) {
                            baseFragment.reloadData();
                        }
                        closeThis();
                    }
                    else
                    {
                        onBadResponse(response);
                    }
                }
            }, vehicle.policy.id, name);
        }
    }
}
