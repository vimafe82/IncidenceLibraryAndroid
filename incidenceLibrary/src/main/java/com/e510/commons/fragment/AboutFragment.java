package com.e510.commons.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.e510.incidencelibrary.R;
import com.e510.commons.domain.Device;
import com.e510.commons.utils.DeviceUtils;
import com.e510.commons.utils.config.AppConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class AboutFragment extends BaseFragment {
    private static final String TAG = makeLogTag(AboutFragment.class);

    public ImageView imgLogo;
    public TextView txtName, txtVersion;

    public static AboutFragment newInstance(){
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.about_us_title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_about, container, false);
        setupUI(view);

        return  view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        imgLogo = rootView.findViewById(R.id.imgLogo);
        loadImageLogo();

        txtName = rootView.findViewById(R.id.company_name);
        int colorPrimary = Color.parseColor("#"+ AppConfiguration.getInstance().appearance.colors.primary);
        txtName.setTextColor(colorPrimary);
        txtVersion = rootView.findViewById(R.id.company_version);
    }

    @Override
    public void loadData()
    {
        Device device = DeviceUtils.getDevice(getContext());

        txtName.setText(AppConfiguration.getInstance().name);

        String jsonVersion = "";
        try {
            jsonVersion = " - " + AppConfiguration.getInstance().version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        txtVersion.setText("v " + device.getAppVersion() + " (" + device.getBuild() + jsonVersion + ")");
    }

    public void loadImageLogo () {
        //start a background thread for networking
        new Thread(new Runnable() {
            public void run(){
                try {
                    //download the drawable
                    String url = AppConfiguration.getInstance().appearance.icon;
                    final Drawable drawable = Drawable.createFromStream((InputStream) new URL(url).getContent(), "src");
                    //edit the view in the UI thread
                    imgLogo.post(new Runnable() {
                        public void run() {
                            imgLogo.setImageDrawable(drawable);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
