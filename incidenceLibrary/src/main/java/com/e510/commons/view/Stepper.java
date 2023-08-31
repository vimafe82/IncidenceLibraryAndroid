package com.e510.commons.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.incidencelibrary.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;

public class Stepper extends RelativeLayout {

    public interface StepperListener {
        void onStepQuantityChange(int newQuantity);
    }

    private StepperListener listener;
    public void setStepperListener(StepperListener listener) {
        this.listener = listener;
    }

    private float radius;
    private LinearLayout layoutRound;

    private int quantity;
    private EditText txtInput;
    private TextView txtMinus;
    private TextView txtMore;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getQuantity()
    {
        return quantity;
    }
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
        refreshState();
    }

    public Stepper(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public Stepper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public Stepper(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr)
    {
        /*
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PrimaryField, defStyleAttr,0);

        try
        {
            radius = a.getInt(R.styleable.PrimaryField_radius, Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields));
        }
        finally
        {
            a.recycle();
        }*/

        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.stepper, this, true);
        layoutRound = view.findViewById(R.id.layoutRound);
        txtInput = view.findViewById(R.id.txtInput);
        txtMinus = view.findViewById(R.id.txtMinus);
        txtMore = view.findViewById(R.id.txtMore);
        FontUtils.setTypeValueText(view, FontUtils.PRIMARY_MEDIUM, getContext());

        int colorPrimary = Color.parseColor("#"+ AppConfiguration.getInstance().appearance.colors.primary);
        txtMinus.setBackgroundColor(colorPrimary);
        txtMore.setBackgroundColor(colorPrimary);

        radius = Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields);
        if (radius > 0)
        {
            radius = radius * 30 / 50;
        }
        GradientDrawable background =  Utils.createGradientDrawable(context, R.color.white, (int)radius);
        layoutRound.setBackground(background);
        layoutRound.setClipToOutline(true);


        txtMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity--;
                if (quantity < 0)
                    quantity = 0;
                refreshState();

                if (listener != null) {
                    listener.onStepQuantityChange(-1);
                }
            }
        });

        txtMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                refreshState();

                if (listener != null) {
                    listener.onStepQuantityChange(1);
                }
            }
        });

        refreshState();
    }

    private void refreshState()
    {
        txtInput.setText(quantity+"");

        if (quantity == 0) {
            txtMinus.setAlpha(0.5f);
        }
        else {
            txtMinus.setAlpha(1f);
        }
    }
}
