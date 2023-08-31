package com.e510.commons.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.commons.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.commons.utils.config.AppConfiguration;

public class InfoView extends RelativeLayout {

    private RelativeLayout layoutRoot;
    private TextView lblTitle;
    private TextView lblSubtitle;
    private PrimaryButton button;

    public InfoView(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public InfoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public InfoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_info, this, true);

        layoutRoot = view.findViewById(R.id.layoutRoot);
        lblTitle = view.findViewById(R.id.lblTitle);
        lblTitle.setText(context.getString(R.string.default_empty_ws_response_title));
        lblSubtitle = view.findViewById(R.id.lblSubtitle);
        lblSubtitle.setText(context.getString(R.string.default_empty_ws_response_subtitle));
        button = view.findViewById(R.id.button);
        button.setText(context.getString(R.string.default_error_ws_response_button));
        button.setTextToUpperCase();

        hide();
    }

    public void init(View.OnClickListener listenerButton)
    {
        setStyles();
        setButtonClickListener(listenerButton);
    }

    private void setStyles()
    {
        FontUtils.setTypeValueText(lblSubtitle, getContext());
        FontUtils.setTypeValueText(lblTitle, FontUtils.PRIMARY_BOLD, getContext());
        FontUtils.setTypeValueText(button, FontUtils.PRIMARY_BOLD, getContext());

        int colorPrimary = Color.parseColor("#"+ AppConfiguration.getInstance().appearance.colors.primary);
        lblTitle.setTextColor(colorPrimary);
    }

    public void clearBackground()
    {
        layoutRoot.setBackgroundColor(Utils.getColor(getContext(), android.R.color.transparent));
    }

    public void setTitle(String title)
    {
        lblTitle.setText(title);
    }

    public void setSubtitle(String subtitle)
    {
        lblSubtitle.setText(subtitle);
    }

    public void setButtonTitle(String title)
    {
        button.setText(title);
    }

    public void setButtonClickListener(View.OnClickListener listener)
    {
        button.setOnClickListener(listener);
    }

    public void show()
    {
        show(true, true, true);
    }

    public void showTitle()
    {
        show(true, false, false);
    }

    public void showTexts()
    {
        show(true, true, false);
    }

    public void show(boolean showTitle, boolean showSubtitle, boolean showButton)
    {
        lblTitle.setVisibility(showTitle ? View.VISIBLE : View.GONE);
        lblSubtitle.setVisibility(showSubtitle ? View.VISIBLE : View.GONE);
        button.setVisibility(showButton ? View.VISIBLE : View.GONE);
        this.setVisibility(View.VISIBLE);
    }

    public void hide() {
        this.setVisibility(View.GONE);
    }
}
