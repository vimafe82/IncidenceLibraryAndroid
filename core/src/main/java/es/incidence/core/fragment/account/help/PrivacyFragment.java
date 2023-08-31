package es.incidence.core.fragment.account.help;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import es.incidence.core.R;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.utils.view.INavigation;
import es.incidence.core.utils.view.TermsView;

public class PrivacyFragment extends IFragment
{
    private static final String TAG = makeLogTag(PrivacyFragment.class);

    private INavigation navigation;
    private TermsView termsView;

    public static PrivacyFragment newInstance()
    {
        PrivacyFragment fragment = new PrivacyFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.privacy;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootPrivacy;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_privacy, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView)
    {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);
        navigation.init(this, getString(getTitleId()), true);

        termsView = rootView.findViewById(R.id.termsView);
        termsView.setOnlyRead();
    }
}
