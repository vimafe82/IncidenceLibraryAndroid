package es.incidence.core.fragment.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.e510.commons.fragment.WebViewFragment;

import es.incidence.core.R;
import es.incidence.core.utils.view.INavigation;

public class WebFragment extends WebViewFragment {

    private INavigation navigation;

    public static final String KEY_TITLE = "KEY_TITLE";
    private String title;

    public static WebFragment newInstance(String url, String title)
    {
        WebFragment fragment = new WebFragment();
        fragment.url = url;
        fragment.type = 1;
        fragment.title = title;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_URL, url);
        bundle.putString(KEY_TITLE, title);
        bundle.putInt(KEY_TYPE, 1);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            title = getArguments().getString(KEY_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);
        navigation.init(this, title, true);
    }
}
