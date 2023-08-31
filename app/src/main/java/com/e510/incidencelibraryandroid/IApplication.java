package com.e510.incidencelibraryandroid;

import android.app.Application;

import com.e510.incidencelibrary.IncidenceLibraryManager;
import com.e510.incidencelibrary.config.IncidenceLibraryConfig;

public class IApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        String apiKey = "mapfre";
        IncidenceLibraryConfig config = new IncidenceLibraryConfig.Builder().setApikey(apiKey).createIncidenceLibraryConfig();

        IncidenceLibraryManager.setup(this, config);
    }
}
