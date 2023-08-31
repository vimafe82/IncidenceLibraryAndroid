package es.incidence.core.fragment.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.e510.incidencelibrary.R;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.utils.view.INavigation;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class AddFragment extends IFragment
{
    private static final String TAG = makeLogTag(AddFragment.class);

    private INavigation navigation;

    public static AddFragment newInstance()
    {
        AddFragment fragment = new AddFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
        }
    }

    @Override
    public int getTitleId() {
        return R.string.add;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootAdd;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);
        navigation.init(this, getString(getTitleId()), true);

        RelativeLayout layoutCar = rootView.findViewById(R.id.select_car);
        layoutCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToVehicle();
            }
        });
        RelativeLayout layoutBeacon = rootView.findViewById(R.id.select_beacon);
        layoutBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToBeacon();
            }
        });
    }

    @Override
    public void loadData()
    {
    }

    private void goToVehicle()
    {
        mListener.addFragmentAnimated(AddVehicleFragment.newInstance(false));
    }

    private void goToBeacon()
    {
        mListener.addFragmentAnimated(AddBeaconFragment.newInstance(0, 1, null,true));
    }
}
