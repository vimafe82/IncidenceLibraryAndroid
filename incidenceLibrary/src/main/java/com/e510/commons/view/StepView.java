package com.e510.commons.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.incidencelibrary.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;

import java.util.ArrayList;

public class StepView extends RelativeLayout {

    private LinearLayout layoutRoot;
    private ArrayList<View> lines = new ArrayList<>();

    private float radius;
    private int position;
    private int quantity;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getQuantity()
    {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;

        refreshState();
    }

    public int getPosition()
    {
        return position;
    }

    public boolean isFinished()
    {
        return position == quantity-1;
    }

    public StepView(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public StepView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public StepView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StepView, defStyleAttr,0);

        try
        {
            position = a.getInt(R.styleable.StepView_stepPosition, 0);
            quantity = a.getInt(R.styleable.StepView_stepQuantity, 0);
        }
        finally
        {
            a.recycle();
        }

        radius = Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields);
        if (radius > 0)
        {
            radius = radius * 30 / 50;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.step_view, this, true);
        layoutRoot = view.findViewById(R.id.layoutRoot);

        refreshState();
    }

    private void refreshState()
    {
        layoutRoot.removeAllViews();
        lines.clear();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < quantity; i++)
        {
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.step_position, this, false);
            TextView txtTitle = view.findViewById(R.id.txtTitle);
            View line = view.findViewById(R.id.line);
            lines.add(line);
            FontUtils.setTypeValueText(txtTitle, FontUtils.PRIMARY_REGULAR, getContext());

            txtTitle.setText(i+1 + "");

            int color = (i == position) ? R.color.colorPrimary : R.color.gray;

            GradientDrawable background =  Utils.createGradientDrawable(getContext(), color, (int)radius);
            line.setBackground(background);

            layoutRoot.addView(view);
        }
    }

    public void nextStep()
    {
        int next = position + 1;

        if (next < quantity) {

            View viewOld = lines.get(position);
            GradientDrawable background =  Utils.createGradientDrawable(getContext(), R.color.gray, (int)radius);
            viewOld.setBackground(background);

            View view = lines.get(next);
            GradientDrawable background2 =  Utils.createGradientDrawable(getContext(), R.color.colorPrimary, (int)radius);
            view.setBackground(background2);

            position = next;
        }
    }

    public void previousStep()
    {
        int previous = position - 1;

        if (previous >= 0) {

            int colorPrimary = Color.parseColor("#"+ AppConfiguration.getInstance().appearance.colors.primary);
            int colorGray = Utils.getColor(getContext(), R.color.gray);

            View viewOld = lines.get(position);
            GradientDrawable background =  Utils.createGradientDrawable(getContext(), R.color.gray, (int)radius);
            viewOld.setBackground(background);

            View view = lines.get(previous);
            GradientDrawable background2 =  Utils.createGradientDrawable(getContext(), R.color.colorPrimary, (int)radius);
            view.setBackground(background2);

            position = previous;
        }
    }
}
