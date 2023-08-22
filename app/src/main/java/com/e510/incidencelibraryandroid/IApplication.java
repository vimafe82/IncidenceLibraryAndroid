package com.e510.incidencelibraryandroid;

import android.app.Application;

import com.e510.incidencelibrary.IncidenceLibraryManager;

public class IApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        IncidenceLibraryManager.init(this);
    }
}
