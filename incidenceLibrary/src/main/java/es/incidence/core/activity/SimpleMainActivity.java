package es.incidence.core.activity;

import android.os.Bundle;

import com.e510.incidencelibrary.R;

public class SimpleMainActivity extends IActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_activity_main);
    }
}
