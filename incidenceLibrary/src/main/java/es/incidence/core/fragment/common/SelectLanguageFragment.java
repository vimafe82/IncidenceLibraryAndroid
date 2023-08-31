package es.incidence.core.fragment.common;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;

public class SelectLanguageFragment  extends IFragment {
    private static final String TAG = makeLogTag(SelectLanguageFragment.class);

    public static SelectLanguageFragment newInstance() {
        SelectLanguageFragment fragment = new SelectLanguageFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootSelLanguage;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_language, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        RelativeLayout menuEllipseLayout = rootView.findViewById(R.id.menuEllipseLayout);
        menuEllipseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeThis();
            }
        });

        RelativeLayout menuRowLanguageSpanish = rootView.findViewById(R.id.menuRowLanguageSpanish);
        menuRowLanguageSpanish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("es");
            }
        });

        RelativeLayout menuRowLanguageEnglish = rootView.findViewById(R.id.menuRowLanguageEnglish);
        menuRowLanguageEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("en");
            }
        });
    }

    private void changeLanguage(String newLanguage)
    {
        String language = Core.getLanguage();

        if (!language.equals(newLanguage))
        {
            Core.saveData(Constants.KEY_USER_LANG, newLanguage);
            Api.updateLang();
            Api.getGeneralData(null);
            showHud();
            Api.getGlobals(null);
        }
        else
        {
            closeThis();
        }
    }
}