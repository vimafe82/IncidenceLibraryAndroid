package es.incidence.core.fragment.incidence.report;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;

public class IncidenceValorationCommentFragment extends IncidenceReportFragment
{
    private EditText editText;

    private static final String KEY_INCIDENCE = "KEY_INCIDENCE";
    private Incidence incidence;

    private static final String KEY_ANSWERS = "KEY_ANSWERS";
    private ArrayList<Integer> answers;

    private static final String KEY_CUSTOM_ANSWER = "KEY_CUSTOM_ANSWER";
    private String customAnswer;

    public static IncidenceValorationCommentFragment newInstance(Incidence incidence, ArrayList<Integer> answers, String customAnswer)
    {
        IncidenceValorationCommentFragment fragment = new IncidenceValorationCommentFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INCIDENCE, incidence);
        bundle.putIntegerArrayList(KEY_ANSWERS, answers);
        bundle.putString(KEY_CUSTOM_ANSWER, customAnswer);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            incidence = getArguments().getParcelable(KEY_INCIDENCE);
            answers = getArguments().getIntegerArrayList(KEY_ANSWERS);
            customAnswer = getArguments().getString(KEY_CUSTOM_ANSWER);
        }
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        rootView.setBackgroundColor(Utils.getColor(getContext(), R.color.incidence100));
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });
        navigation.setTitle(getString(R.string.service_valoration));
        btnRed.setVisibility(View.GONE);
        btnBlue.setText(R.string.send_valoration);
        btnCancel.setVisibility(View.GONE);
        imgNavTitleSecondRight.setVisibility(View.GONE);
        holdSpeech = true;
    }

    @Override
    public void addContent()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_incidence_valoration_comment, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());

        RelativeLayout layoutSlide = view.findViewById(R.id.layoutSlide);
        GradientDrawable back =  Utils.createGradientDrawable(getContext(), android.R.color.white, 4);
        layoutSlide.setBackground(back);

        editText = view.findViewById(R.id.edittext);

        layoutContent.addView(view);
    }

    @Override
    public void onClickBlue()
    {
        String rateComment = editText.getText().toString();

        showHud();
        Api.rateIncidence(new IRequestListener() {
            @Override
            public void onFinish(IResponse response)
            {
                hideHud();
                if (response.isSuccess())
                {
                    EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));
                    mListener.addFragmentAnimated(IncidenceThanksFragment.newInstance());
                }
                else
                {
                    onBadResponse(response);
                }
            }
        }, incidence.id+"", incidence.rate+"", rateComment, answers, customAnswer);
    }
}
