package com.e510.commons.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import com.e510.commons.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;

public class PrimarySwitch extends RelativeLayout {

    private String topTitle;
    private String hint;
    private float radius;
    private RelativeLayout layoutRound;
    private Drawable drawableLeft;
    private Drawable drawableRight;

    private TextView txtTitle;
    private VXEditText editText;
    private ImageView imgLeft;
    private ImageView imgRight;
    private Switch txtSwitch;

    private TextView mandatoryTextView;
    public Boolean isMandatory = false;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public PrimarySwitch(Context context) {
        super(context);
        init(context, null, 0);
    }

    public PrimarySwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public PrimarySwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PrimaryField, defStyleAttr, 0);

        try {
            topTitle = a.getString(R.styleable.PrimaryField_topTitle);
            radius = a.getInt(R.styleable.PrimaryField_radius, Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields));
            drawableLeft = a.getDrawable(R.styleable.PrimaryField_drawableLeft);
            drawableRight = a.getDrawable(R.styleable.PrimaryField_drawableRight);
        } finally {
            a.recycle();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.primary_switch, this, true);
        layoutRound = view.findViewById(R.id.layoutRound);
        layoutRound.setClipToOutline(true);
        txtTitle = view.findViewById(R.id.txtTitle);
        editText = view.findViewById(R.id.txtInput);
        txtSwitch = view.findViewById(R.id.txtSwitch);

        FontUtils.setTypeValueText(txtTitle, context);
        FontUtils.setTypeValueText(editText, context);
        imgLeft = view.findViewById(R.id.imgLeft);
        imgRight = view.findViewById(R.id.imgRight);
        mandatoryTextView = view.findViewById(R.id.mandatoryTextView);

        setup(context);
    }

    public int getBackgroundColorId() {
        return R.color.white;
    }

    public int getBackgroundDisabledColorId() {
        return R.color.extraLightGray;
    }

    public int getTitleTextColor() {
        return R.color.darkGray;
    }

    public int getEditTextColor() { return R.color.black; }

    public float getBorderWidth() { return Float.parseFloat(AppConfiguration.getInstance().appearance.borderFields); }

    public void setup(final Context context) {
        GradientDrawable drawable = Utils.createGradientDrawable(context, isEnabled() ? getBackgroundColorId() : getBackgroundDisabledColorId(), (int) radius);
        txtTitle.setTextColor(Utils.getColor(getContext(), getTitleTextColor()));

        layoutRound.setBackground(drawable);

        if (topTitle != null) {
            txtTitle.setText(topTitle);
        }

        if (hint != null) {
            editText.setHint(hint);
        }

        if (drawableLeft != null) {
            imgLeft.setVisibility(View.VISIBLE);
            imgLeft.setImageDrawable(drawableLeft);
        }

        if (drawableRight != null) {
            imgRight.setVisibility(View.VISIBLE);
            imgRight.setImageDrawable(drawableRight);
            DrawableCompat.setTint(imgRight.getDrawable(), Utils.getColor(context, R.color.colorPrimary));
        }

        //editText.setMaxLines(1);
        //editText.setSingleLine(true);
        editText.setTextColor(Utils.getColor(getContext(), getEditTextColor()));
        //No enabled para que no sea clicable
        editText.setEnabled(false);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        editText.setClickable(true);
        editText.setOnClickListener(l);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editText.setEnabled(enabled);
        if (!enabled) {
            GradientDrawable drawable = Utils.createGradientDrawable(getContext(), R.color.extraLightGray, (int) radius);
            layoutRound.setBackground(drawable);
        }
    }

    public void setMandatory(boolean isMandatory) {
        if (isMandatory) {
            mandatoryTextView.setVisibility(VISIBLE);
            this.isMandatory = isMandatory;
        }
    }

    public void disableEditable()
    {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }

    public void setFocus(boolean showKeyboard)
    {
        editText.requestFocus();
        if (showKeyboard)
        {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 200);
        }
    }

    public void selectAll()
    {
        editText.selectAll();
    }

    public void setInputType(int type)
    {
        editText.setInputType(type);
    }

    public void setTextSize(int size)
    {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
    }

    public void setGravity(int gravity)
    {
        editText.setGravity(gravity);
    }

    public void setHintTextColor(int idColor)
    {
        editText.setHintTextColor(Utils.getColor(getContext(), idColor));
    }

    public void addTextWatcher(TextWatcher textWatcher)
    {
        editText.addTextChangedListener(textWatcher);
    }

    public void removeTextWatcher(TextWatcher textWatcher)
    {
        editText.removeTextChangedListener(textWatcher);
    }

    public void setBackground(Drawable drawable)
    {
        layoutRound.setBackground(drawable);
    }

    public void toUpperCase() {
        if (hint != null)
            editText.setHint(hint.toUpperCase());
    }

    public void setText(String text)
    {
        editText.setText(text);
    }

    public String getText()
    {
        String res = "";

        if (editText.getText().toString() != null)
            res = editText.getText().toString();

        return res;
    }

    public void setTopTitle(String topTitle)
    {
        this.topTitle = topTitle;
        txtTitle.setText(topTitle);
    }

    public void setHint(String hint)
    {
        this.hint = hint;
    }

    public void setDrawableLeft(Drawable drawableLeft) {
        this.drawableLeft = drawableLeft;
        imgLeft.setImageDrawable(drawableLeft);
    }

    public void setDrawableRight(Drawable drawableRight) {
        this.drawableRight = drawableRight;
        imgRight.setImageDrawable(drawableRight);
    }

    public boolean isChecked()
    {
        return txtSwitch.isChecked();
    }

    public void setChecked(boolean checked)
    {
        txtSwitch.setChecked(checked);
    }
}