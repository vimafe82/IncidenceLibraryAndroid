package es.incidence.core.fragment.vehicle;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.entity.ListItem;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INotification;

public class SelectVehicleDriverTypeFragment extends ListFragment
{
    private static final String TAG = makeLogTag(SelectVehicleDriverTypeFragment.class);

    private static final int ROW_PRIMARY = 0;
    private static final int ROW_SECONDARY = 1;

    private static final String KEY_VEHICLE_ID = "KEY_VEHICLE_ID";
    private String vehicleId;

    public static SelectVehicleDriverTypeFragment newInstance(String vehicleId)
    {
        SelectVehicleDriverTypeFragment fragment = new SelectVehicleDriverTypeFragment();

        Bundle bundle = new Bundle();
        bundle.putString(KEY_VEHICLE_ID, vehicleId);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.create_account_step2;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            vehicleId = getArguments().getString(KEY_VEHICLE_ID);
        }
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.TITLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        TextView textView = new TextView(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = Utils.dpToPx(24);
        params.rightMargin = Utils.dpToPx(24);
        textView.setLayoutParams(params);
        textView.setTextColor(Utils.getColor(getContext(), R.color.black600));
        FontUtils.setTypeValueText(textView, Constants.FONT_REGULAR, getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setText(getString(R.string.ask_type_driver_you_are));
        layoutTopListView.addView(textView);
        layoutTopListView.setVisibility(View.VISIBLE);

        layoutBottom.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_continue_dismiss, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
        TextView txtAddLater = view.findViewById(R.id.txtAddLater);
        txtAddLater.setText(getString(R.string.cancel));
        txtAddLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeThis();
            }
        });
        FontUtils.setTypeValueText(txtAddLater, Constants.FONT_SEMIBOLD, getContext());

        IButton btnContinue = view.findViewById(R.id.btnContinueColor);
        btnContinue.setVisibility(View.GONE);

        layoutBottom.addView(view);
    }

    @Override
    public void loadData()
    {
        setNavigationTitle(getString(getTitleId()));

        ArrayList<ListItem> temp = new ArrayList<>();

        ListItem l1 = new ListItem(getString(R.string.driver_primary), ROW_PRIMARY);
        temp.add(l1);

        ListItem l2 = new ListItem(getString(R.string.driver_secondary), ROW_SECONDARY);
        temp.add(l2);

        renewItems(temp);
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            int row = (int) listItem.object;

            if (row == ROW_PRIMARY)
            {
                showPrimaryDriverPopUp();
            }
            else if (row == ROW_SECONDARY)
            {
                showSecondaryDriverPopUp();
            }
        }
    }

    private void showPrimaryDriverPopUp()
    {
        String title = getString(R.string.ask_you_are_driver_primary);
        String message = getString(R.string.ask_you_are_driver_primary_desc);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.i_am_driver_primary));
        options.add(getString(R.string.cancel));
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
                        //yes
                        requestDriver("1");
                    }
                    else if (index == 1)
                    {
                        //no
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    private void showSecondaryDriverPopUp()
    {
        String title = getString(R.string.ask_you_are_driver_secondary);
        String message = getString(R.string.ask_you_are_driver_secondary_desc);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.accept));
        options.add(getString(R.string.cancel));
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
                        //yes
                        requestDriver("0");
                    }
                    else if (index == 1)
                    {
                        //no
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }


    private void requestDriver(String vehicleDriverType)
    {
        showHud();
        Api.requestAddVehicleDriver(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess())
                {
                    //Finaliza el registro porque ese vehiculo ya existe.
                    //Go home
                    Core.startApp(getBaseActivity());
                }
                else
                {
                    onBadResponse(response);
                }
            }
        }, vehicleId, vehicleDriverType);
    }
}
