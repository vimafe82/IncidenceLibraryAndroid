package com.e510.commons.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.graphics.drawable.DrawableCompat;

import com.e510.commons.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;

public class SearchView extends RelativeLayout {

    private String hint;
    private float radius;

    private RelativeLayout layoutRound;
    private VXEditText txtInput;
    private View lineView;
    private ImageView imgRight;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public SearchView(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public SearchView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr,0);

        try
        {
            hint = a.getString(R.styleable.SearchView_searchHint);
        }
        finally
        {
            a.recycle();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.search_view, this, true);
        layoutRound = view.findViewById(R.id.layoutRound);
        txtInput = view.findViewById(R.id.txtInput);
        FontUtils.setTypeValueText(txtInput, FontUtils.PRIMARY_BOLD, getContext());
        lineView = view.findViewById(R.id.lineView);
        imgRight = view.findViewById(R.id.imgRight);

        int colorPrimary = Color.parseColor("#"+ AppConfiguration.getInstance().appearance.colors.primary);
        lineView.setBackgroundColor(colorPrimary);
        DrawableCompat.setTint(imgRight.getDrawable(), colorPrimary);
        txtInput.setTextColor(colorPrimary);

        int radius = Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields);
        GradientDrawable background =  Utils.createGradientDrawable(context, R.color.white, radius);
        layoutRound.setBackground(background);
        layoutRound.setClipToOutline(true);

        if (hint != null) {
            txtInput.setHint(hint);
        }

        setClearButton();
    }

    public void setHint(String value) {
        txtInput.setHint(value);
    }

    private void setClearButton() {
        final Drawable icon_close = Utils.getDrawable(getContext(), R.drawable.ic_close);
        final Drawable icon_search = Utils.getDrawable(getContext(), R.drawable.ic_search);

        txtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    imgRight.setImageDrawable(icon_close);
                } else {
                    imgRight.setImageDrawable(icon_search);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtInput.setText("");
                txtInput.clearFocus();
            }
        });
    }

    public void setTextWatcher(TextWatcher watcher)
    {
        txtInput.addTextChangedListener(watcher);
    }

    public void focus()
    {
        txtInput.requestFocus();
    }

    public EditText getEditText()
    {
        return txtInput;
    }

    public String getString()
    {
        return txtInput.getText().toString();
    }

    public void setText(String str)
    {
        txtInput.setText(str);
    }
}
