package es.incidence.core.fragment.incidence.report;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.R;
import es.incidence.core.domain.Answer;
import es.incidence.core.domain.Incidence;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;

public class IncidenceValorationFragment extends IncidenceReportFragment
{
    private SeekBar seekBar;
    private ImageView imgVeryBad;
    private ImageView imgBad;
    private ImageView imgNeutral;
    private ImageView imgGood;
    private ImageView imgVeryGood;

    private static final String KEY_INCIDENCE = "KEY_INCIDENCE";
    private Incidence incidence;

    public static IncidenceValorationFragment newInstance(Incidence incidence)
    {
        IncidenceValorationFragment fragment = new IncidenceValorationFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INCIDENCE, incidence);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            incidence = getArguments().getParcelable(KEY_INCIDENCE);
        }
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        rootView.setBackgroundColor(Utils.getColor(getContext(), R.color.incidence100));
        navigation.setTitle(getString(R.string.service_valoration));
        btnRed.setVisibility(View.GONE);
        btnBlue.setText(R.string.continuar);
        btnCancel.setText(R.string.valorate_later);
        imgNavTitleSecondRight.setVisibility(View.GONE);
        holdSpeech = true;
    }

    @Override
    public void addContent()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_incidence_valoration, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        RelativeLayout layoutSlide = view.findViewById(R.id.layoutSlide);
        GradientDrawable back =  Utils.createGradientDrawable(getContext(), android.R.color.white, 4);
        layoutSlide.setBackground(back);

        seekBar = view.findViewById(R.id.seekBar);
        imgVeryBad = view.findViewById(R.id.imgValVeryBad);
        imgBad = view.findViewById(R.id.imgValBad);
        imgNeutral = view.findViewById(R.id.imgValNeutral);
        imgGood = view.findViewById(R.id.imgValGood);
        imgVeryGood = view.findViewById(R.id.imgValVeryGood);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                imgVeryBad.setVisibility(i == 0 ? View.VISIBLE : View.INVISIBLE);
                imgBad.setVisibility(i == 1 ? View.VISIBLE : View.INVISIBLE);
                imgNeutral.setVisibility(i == 2 ? View.VISIBLE : View.INVISIBLE);
                imgGood.setVisibility(i == 3 ? View.VISIBLE : View.INVISIBLE);
                imgVeryGood.setVisibility(i == 4 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        layoutContent.addView(view);
    }

    @Override
    public void onClickBlue()
    {
        int rate = seekBar.getProgress();

        showHud();
        Api.rateIncidence(new IRequestListener() {
            @Override
            public void onFinish(IResponse response)
            {
                hideHud();
                if (response.isSuccess())
                {
                    incidence.rate = rate;

                    ArrayList<Answer> answers = response.getList("answers", Answer.class);
                    if (answers != null && answers.size() > 0)
                    {
                        mListener.addFragmentAnimated(IncidenceValorationWhyBadFragment.newInstance(incidence, answers));
                    }
                    else
                    {
                        mListener.addFragmentAnimated(IncidenceValorationCommentFragment.newInstance(incidence, null, null));
                    }
                }
                else
                {
                    onBadResponse(response);
                }
            }
        }, incidence.id+"", rate+"", null, null, null);
    }
}
