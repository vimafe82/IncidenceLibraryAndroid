package es.incidence.core.fragment.incidence.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;

public class IncidenceThanksFragment extends IncidenceReportFragment
{
    public static IncidenceThanksFragment newInstance()
    {
        IncidenceThanksFragment fragment = new IncidenceThanksFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        rootView.setBackgroundColor(Utils.getColor(getContext(), R.color.incidence500));

        navigation.setVisibility(View.GONE);
        btnRed.setVisibility(View.GONE);
        btnBlue.setWhiteColors();
        btnBlue.setText(R.string.return_to_init);
        btnCancel.setVisibility(View.GONE);
        imgNavTitleSecondRight.setVisibility(View.GONE);
        holdSpeech = true;
    }

    @Override
    public void addContent()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_incidence_valoration_thanks, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
        FontUtils.setTypeValueText(view.findViewById(R.id.txtTitle), Constants.FONT_SEMIBOLD, getContext());
        layoutContent.addView(view);
    }

    @Override
    public void onClickBlue()
    {
        mListener.cleanAllBackStackEntries();
    }
}
