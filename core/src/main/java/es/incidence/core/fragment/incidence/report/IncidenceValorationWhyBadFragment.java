package es.incidence.core.fragment.incidence.report;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.R;
import es.incidence.core.domain.Answer;
import es.incidence.core.domain.Incidence;

public class IncidenceValorationWhyBadFragment extends IncidenceReportFragment
{
    private TextView txtSpecify;
    private RelativeLayout layoutSlide;
    private EditText editText;

    private static final String KEY_INCIDENCE = "KEY_INCIDENCE";
    private Incidence incidence;

    private static final String KEY_ANSWERS = "KEY_ANSWERS";
    private ArrayList<Answer> answers;
    private ArrayList<Integer> responseAnswers = new ArrayList<>();

    public static IncidenceValorationWhyBadFragment newInstance(Incidence incidence, ArrayList<Answer> answers)
    {
        IncidenceValorationWhyBadFragment fragment = new IncidenceValorationWhyBadFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INCIDENCE, incidence);
        bundle.putParcelableArrayList(KEY_ANSWERS, answers);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            incidence = getArguments().getParcelable(KEY_INCIDENCE);
            answers = getArguments().getParcelableArrayList(KEY_ANSWERS);
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
        View view = inflater.inflate(R.layout.layout_incidence_valoration_why_bad, null);


        LinearLayout layoutAnswers = view.findViewById(R.id.layoutAnswers);

        if (answers != null)
        {
            for (int i = 0; i < answers.size(); i++)
            {
                Answer answer = answers.get(i);

                View row = inflater.inflate(R.layout.row_answer, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = Utils.dpToPx(16);
                row.setLayoutParams(params);
                TextView txtCheck1 = row.findViewById(R.id.txtCheck1);
                txtCheck1.setText(answer.name);
                ImageView check1 = row.findViewById(R.id.check1);
                if (i == answers.size() - 1)
                    check1.setTag("last");
                check1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        boolean checked = false;
                        if (responseAnswers.contains(answer.id))
                        {
                            responseAnswers.remove(answer.id);
                            checked = false;
                        }
                        else
                        {
                            responseAnswers.add(answer.id);
                            checked = true;
                        }

                        check1.setImageDrawable(Utils.getDrawable(getContext(), checked ? R.drawable.checkbox_on : R.drawable.checkbox_off));

                        if (view.getTag() != null)
                        {
                            layoutSlide.setVisibility(checked ? View.VISIBLE : View.GONE);
                            txtSpecify.setVisibility(checked ? View.VISIBLE : View.GONE);
                        }
                    }
                });

                layoutAnswers.addView(row);
            }
        }

        layoutSlide = view.findViewById(R.id.layoutSlide);
        GradientDrawable back =  Utils.createGradientDrawable(getContext(), android.R.color.white, 4);
        layoutSlide.setBackground(back);

        txtSpecify = view.findViewById(R.id.txtSpecify);
        editText = view.findViewById(R.id.edittext);

        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
        layoutContent.addView(view);
    }

    @Override
    public void onClickBlue()
    {
        String customAnswer = editText.getText().toString();
        mListener.addFragmentAnimated(IncidenceValorationCommentFragment.newInstance(incidence, responseAnswers, customAnswer));
    }

    @Override
    public void onClickCancel() {
        //super.onClickCancel();

        ArrayList<String> list = new ArrayList<>();
        list.add(IncidenceReportFragment.class.getName());
        list.add(IncidenceValorationWhyBadFragment.class.getName());
        mListener.cleanAllBackStackEntries(list);

        closeThis();
    }
}
