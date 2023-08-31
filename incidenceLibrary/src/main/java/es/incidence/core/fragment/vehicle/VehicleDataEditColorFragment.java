package es.incidence.core.fragment.vehicle;

import android.os.Bundle;
import android.view.View;

import com.e510.commons.fragment.BaseFragment;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.ColorType;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.domain.VehicleType;
import es.incidence.core.view.VehicleColorView;

public class VehicleDataEditColorFragment extends VehicleDataFragment
{
    private VehicleColorView vehicleColorView;

    public static VehicleDataEditColorFragment newInstance(Vehicle vehicle)
    {
        VehicleDataEditColorFragment fragment = new VehicleDataEditColorFragment();

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

        vehicleColorView = new VehicleColorView(getContext());

        VehicleType vehicleType = Core.getVehicleType(vehicle.vehicleType.id);
        if (vehicleType != null) {
            vehicleColorView.setVehicleType(vehicleType);

            ColorType colorType = Core.getColorType(vehicleType, vehicle.color.id);
            if (colorType != null) {
                vehicleColorView.setVehicleColor(colorType);
            }
        }

        vehicleColorView.enableContinueButton();
        vehicleColorView.setAcceptTitle(getString(R.string.accept));
        vehicleColorView.setDismissTitle(getString(R.string.cancel));
        vehicleColorView.onDismissClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeThis();
            }
        });
        vehicleColorView.onContinueClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorType color = vehicleColorView.getVehicleColor();
                vehicle.color = color;


                BaseFragment baseFragment = mListener.getPenultimFragment();
                if (baseFragment != null) {
                    baseFragment.reloadData();
                }
                closeThis();
            }
        });

        layoutContent.addView(vehicleColorView);
    }

    @Override
    public void loadData()
    {
        setNavigationTitle(getString(R.string.color));
    }
}