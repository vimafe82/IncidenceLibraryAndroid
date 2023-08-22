package com.e510.incidencelibrary;

import android.app.Application;

public class IncidenceLibraryManager {

    private static Application application;

    public static void init(Application app) {
        application = app;
    }

}
