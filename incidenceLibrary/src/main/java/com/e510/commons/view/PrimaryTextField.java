package com.e510.commons.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import com.e510.incidencelibrary.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;
import com.e510.commons.view.searchableSpinner.SearchableItem;
import com.e510.commons.view.searchableSpinner.SearchableSpinner;

import java.util.ArrayList;
import java.util.Calendar;

public class PrimaryTextField extends RelativeLayout {

    public enum Type {
        REGULAR,
        EMAIL,
        PASSWORD,
        BUTTON,
        SELECT,
        DATE
    }

    private String identifier;
    private Type type;
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
    private TextView mandatoryTextView;

    public Boolean isMandatory = false;

    private ArrayList<SearchableItem> options;
    private SelectOptionListener selectOptionListener;
    public void setOnSelectOptionListener(SelectOptionListener listener) {
        selectOptionListener = listener;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public PrimaryTextField(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public PrimaryTextField(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public PrimaryTextField(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PrimaryField, defStyleAttr,0);

        try
        {
            topTitle = a.getString(R.styleable.PrimaryField_topTitle);
            hint = a.getString(R.styleable.PrimaryField_hint);
            type = Type.values()[a.getInt(R.styleable.PrimaryField_type,0)];
            radius = a.getInt(R.styleable.PrimaryField_radius, Integer.parseInt(AppConfiguration.getInstance().appearance.roundedFields));
            drawableLeft = a.getDrawable(R.styleable.PrimaryField_drawableLeft);
            drawableRight = a.getDrawable(R.styleable.PrimaryField_drawableRight);
        }
        finally
        {
            a.recycle();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.primary_textfield, this, true);
        layoutRound = view.findViewById(R.id.layoutRound);
        layoutRound.setClipToOutline(true);
        txtTitle = view.findViewById(R.id.txtTitle);
        editText = view.findViewById(R.id.txtInput);

        FontUtils.setTypeValueText(txtTitle, context);
        FontUtils.setTypeValueText(editText, context);
        imgLeft = view.findViewById(R.id.imgLeft);
        imgRight = view.findViewById(R.id.imgRight);
        mandatoryTextView = view.findViewById(R.id.mandatoryTextView);

        setup(context);
    }

    public void setSelectOptions(ArrayList<SearchableItem> selectOptions) {
        options = selectOptions;
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

    public void setup(final Context context)
    {
        GradientDrawable drawable = Utils.createGradientDrawable(context, isEnabled() ? getBackgroundColorId() : getBackgroundDisabledColorId(), (int) radius);
        txtTitle.setTextColor(Utils.getColor(getContext(), getTitleTextColor()));



        if (type != Type.BUTTON)
        {
            int border = 1;
            try{
                float bord = getBorderWidth();
                border = (int)(bord*3);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (border != -1 ) {
                if (border == 0) {
                    drawable.setStroke(3, Utils.getColor(getContext(), R.color.lightGray));
                } else {
                    drawable.setStroke(border, Utils.getColor(getContext(), R.color.colorPrimary));
                }
            }
        }
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

        editText.setMaxLines(1);
        editText.setSingleLine(true);
        editText.setTextColor(Utils.getColor(getContext(), getEditTextColor()));

        switch (type) {
            case BUTTON:
                editText.setGravity(Gravity.CENTER);
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setHintTextColor(Utils.getColor(context, R.color.colorPrimary));
                break;
            case EMAIL:
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case PASSWORD:
                Typeface cache = editText.getTypeface();
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setTypeface(cache);
                imgRight.setImageDrawable(context.getDrawable(R.drawable.ic_hide));
                DrawableCompat.setTint(imgRight.getDrawable(), Utils.getColor(context, R.color.colorPrimary));
                imgRight.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            Typeface cache = editText.getTypeface();
                            editText.setInputType(InputType.TYPE_CLASS_TEXT);
                            editText.setTypeface(cache);
                            imgRight.setImageDrawable(context.getDrawable(R.drawable.ic_show));
                            DrawableCompat.setTint(imgRight.getDrawable(), Utils.getColor(context, R.color.colorPrimary));
                        } else {
                            Typeface cache = editText.getTypeface();
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setTypeface(cache);
                            imgRight.setImageDrawable(context.getDrawable(R.drawable.ic_hide));
                            DrawableCompat.setTint(imgRight.getDrawable(), Utils.getColor(context, R.color.colorPrimary));
                        }
                    }
                });
                break;
            case SELECT:
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setOnClickListener(selectOnClickListener());

                imgRight.setVisibility(View.VISIBLE);
                imgRight.setImageDrawable(context.getDrawable(R.drawable.ic_next));
                imgRight.getLayoutParams().height = Utils.dpToPx(10);
                imgRight.getLayoutParams().width = Utils.dpToPx(10);
                DrawableCompat.setTint(imgRight.getDrawable(), Utils.getColor(context, R.color.gray));
                break;
            case DATE:
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setOnClickListener(datePickerOnClickListener());
                break;
        }

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    try {
                        editText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
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

    public void setMandatory(boolean isMandatory) {
        if (isMandatory) {
            mandatoryTextView.setVisibility(VISIBLE);
            this.isMandatory = isMandatory;
        }
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
        editText.setHint(hint);
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public void setDrawableLeft(Drawable drawableLeft) {
        this.drawableLeft = drawableLeft;
        imgLeft.setImageDrawable(drawableLeft);
    }

    public void setDrawableRight(Drawable drawableRight) {
        this.drawableRight = drawableRight;
        imgRight.setImageDrawable(drawableRight);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    private OnClickListener selectOnClickListener() {
        return v -> {
            SearchableSpinner spinner = new SearchableSpinner(getContext(), false);
            spinner.setTitle(topTitle);
            spinner.setPositiveButton(getContext().getString(R.string.button_close));
            spinner.setAdapter(spinner.createAdapter(getContext(), options));
            spinner.setOnSearchItemClickedListener(new SearchableSpinner.OnSearchItemClicked() {
                @Override
                public void onSearchItemClicked(SearchableItem item, int position) {
                    setText(item.name);
                    if (selectOptionListener != null)
                    {
                        selectOptionListener.onItemSelected(position);
                    }
                }
            });
            spinner.showDialog();
        };
    }

    private View.OnClickListener datePickerOnClickListener() {
        return v -> {
            Calendar currentCalendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(getContext(), onDateSetListener(v), currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_WEEK));
            dialog.show();
        };
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener(View view) {
        return (DatePicker datePicker, int year, int month, int day) -> {
            month = month + 1;
            String fm = "" + month;
            String fd = "" + day;
            if(month < 10){
                fm = "0" + month;
            }
            if (day < 10){
                fd = "0" + day;
            }
            editText.setText(fd + "/" + fm + "/" + year);
        };
    }

    public interface SelectOptionListener
    {
        void onItemSelected(int position);
    }
}
