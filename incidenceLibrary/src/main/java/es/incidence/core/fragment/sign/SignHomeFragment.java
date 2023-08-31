package es.incidence.core.fragment.sign;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.LogUtil;
import com.e510.commons.utils.Utils;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.fragment.welcome.WelcomeFragment;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INavigation;

public class SignHomeFragment extends IFragment
{
    private static final String TAG = makeLogTag(SignHomeFragment.class);

    private INavigation navigation;

    private TextView txtStep1number;
    private ImageView txtStep1check;
    private TextView txtStep1;
    private TextView txtStep2number;
    private ImageView txtStep2check;
    private TextView txtStep2;
    private TextView txtStep3number;
    private ImageView txtStep3check;
    private TextView txtStep3;

    private IButton btnCreate;
    private TextView txtHaveAccount;
    private TextView txtSignIn;

    private static final String TAG_STEP = "TAG_STEP";
    private int nextStep;

    private static final String KEY_AUTO_SELECTED_VEHICLE = "KEY_AUTO_SELECTED_VEHICLE";
    private Vehicle autoSelectedVehicle;

    public static SignHomeFragment newInstance(int step, Vehicle vehicle)
    {
        SignHomeFragment fragment = new SignHomeFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(TAG_STEP, step);
        bundle.putParcelable(KEY_AUTO_SELECTED_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            nextStep = getArguments().getInt(TAG_STEP, 0);
            autoSelectedVehicle = getArguments().getParcelable(KEY_AUTO_SELECTED_VEHICLE);
        }
    }

    @Override
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootSignHome;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sign_home, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView)
    {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);
        navigation.init(this, getString(R.string.create_account), true);
        navigation.setBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mListener.showInitialFragment(WelcomeFragment.newInstance());
                getBaseActivity().setToolbarColor(Utils.getColor(getContext(), R.color.incidencePrincipal), Utils.getColor(getContext(), android.R.color.white), false);
            }
        });

        txtStep1number = rootView.findViewById(R.id.txtStep1number);
        txtStep1check = rootView.findViewById(R.id.txtStep1check);
        txtStep1 = rootView.findViewById(R.id.txtStep1);
        txtStep2number = rootView.findViewById(R.id.txtStep2number);
        txtStep2check = rootView.findViewById(R.id.txtStep2check);
        txtStep2 = rootView.findViewById(R.id.txtStep2);
        txtStep3number = rootView.findViewById(R.id.txtStep3number);
        txtStep3check = rootView.findViewById(R.id.txtStep3check);
        txtStep3 = rootView.findViewById(R.id.txtStep3);

        btnCreate = rootView.findViewById(R.id.btnCreate);
        btnCreate.setText(getString(R.string.create_one_account));
        btnCreate.setPrimaryColors();
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCreateAccount();
            }
        });

        txtHaveAccount = rootView.findViewById(R.id.txtHaveAccount);
        txtSignIn = rootView.findViewById(R.id.txtSignIn);
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showInitialFragment(R.id.layout_activity_main, SignInFragment.newInstance());
            }
        });

        FontUtils.setTypeValueText(btnCreate, Constants.FONT_SEMIBOLD, getContext());
        FontUtils.setTypeValueText(txtSignIn, Constants.FONT_SEMIBOLD, getContext());

        if (nextStep == 1)
        {
            navigation.setTitle(getString(R.string.vehicle_data));
            txtStep1number.setVisibility(View.INVISIBLE);
            txtStep1check.setVisibility(View.VISIBLE);
            txtStep1check.setColorFilter(Utils.getColor(getContext(), R.color.success));
            txtStep1.setTextColor(Utils.getColor(getContext(), R.color.success));
            btnCreate.setText(getString(R.string.vehicle_data));

            txtHaveAccount.setVisibility(View.INVISIBLE);
            txtSignIn.setVisibility(View.INVISIBLE);
        }
        else if (nextStep == 2)
        {
            navigation.setTitle(getString(R.string.create_account_step3));
            txtStep1number.setVisibility(View.INVISIBLE);
            txtStep1check.setVisibility(View.VISIBLE);
            txtStep1check.setColorFilter(Utils.getColor(getContext(), R.color.success));
            txtStep1.setTextColor(Utils.getColor(getContext(), R.color.success));
            txtStep2number.setVisibility(View.INVISIBLE);
            txtStep2check.setVisibility(View.VISIBLE);
            txtStep2check.setColorFilter(Utils.getColor(getContext(), R.color.success));
            txtStep2.setTextColor(Utils.getColor(getContext(), R.color.success));
            btnCreate.setText(getString(R.string.search_baliza));

            txtHaveAccount.setVisibility(View.INVISIBLE);
            txtSignIn.setVisibility(View.VISIBLE);
            txtSignIn.setText(R.string.omitir);
            txtSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Core.startApp(getBaseActivity());
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        try
        {
            menu.clear();
            super.onCreateOptionsMenu(menu, inflater);
        }
        catch (Exception e)
        {
            LogUtil.logE(TAG, e.getMessage());
        }
    }

    private void onClickCreateAccount()
    {
        if (nextStep == 0)
        {
            mListener.addFullFragmentAnimated(SignUpPersonFragment.newInstance());
        }
        else if (nextStep == 1)
        {
            mListener.addFullFragmentAnimated(SignUpVehicleFragment.newInstance());
        }
        else if (nextStep == 2)
        {
            mListener.addFullFragmentAnimated(SignUpBeaconFragment.newInstance(autoSelectedVehicle));
        }
    }
}
