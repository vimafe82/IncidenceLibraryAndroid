package es.incidence.core.activity;

import android.os.Bundle;
import android.view.View;

import com.e510.incidencelibrary.R;

import es.incidence.core.Constants;
import es.incidence.core.fragment.beacon.BeaconListFragment;

public class SimpleMainActivity extends IActivity
{
    private String screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_activity_main);

        Bundle b = getIntent().getExtras();
        screen = b.getString("screen");

        findViewById(R.id.toolbar).setVisibility(View.GONE);

        if (Constants.SCREEN_DEVICE_LIST.equals(screen)) {
            showInitialFragment(BeaconListFragment.newInstance());
        }

    }
}
