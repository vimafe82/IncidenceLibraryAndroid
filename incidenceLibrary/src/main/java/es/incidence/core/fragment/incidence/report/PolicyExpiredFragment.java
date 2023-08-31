package es.incidence.core.fragment.incidence.report;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;

public class PolicyExpiredFragment extends IncidenceReportFragment
{
    @Override
    public boolean needEventBus() {
        return true;
    }

    public static PolicyExpiredFragment newInstance()
    {
        PolicyExpiredFragment fragment = new PolicyExpiredFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        btnRed.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnBlue.setText(R.string.policy_update);
        imgNavTitleSecondRight.setVisibility(View.GONE);
        holdSpeech = true;
    }

    @Override
    public void addContent()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_incidence_policy_expired, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setTypeValueText(txtTitle, Constants.FONT_SEMIBOLD, getContext());

        RelativeLayout layoutTips = view.findViewById(R.id.layoutTips);
        FontUtils.setTypeValueText(layoutTips, Constants.FONT_SEMIBOLD, getContext());
        GradientDrawable back =  Utils.createGradientDrawable(getContext(), R.color.incidence300, (int) 8);
        back.setAlpha(20);
        layoutTips.setBackground(back);

        layoutContent.addView(view);
    }

    @Override
    public void onClickBlue() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.INCICENDE_TIME_CHANGED)
        {
            setUpTimeAlert();
        } else {
            super.onMessageEvent(event);
        }
    }
}