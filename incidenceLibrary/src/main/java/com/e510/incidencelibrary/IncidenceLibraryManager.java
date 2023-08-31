package com.e510.incidencelibrary;

import android.content.Context;
import android.util.Log;

import com.e510.incidencelibrary.config.IncidenceLibraryConfig;


public class IncidenceLibraryManager {

    private static final String LOG_TAG = IncidenceLibraryManager.class.getSimpleName();

    public static IncidenceLibraryManager instance;

    private Context context;

    private IncidenceLibraryManager(final Context context, IncidenceLibraryConfig incidenceLibraryConfig) {
        this.context = context;
    }

    public static void setup(final Context context, IncidenceLibraryConfig incidenceLibraryConfig) {
        if (instance == null) {
            instance = new IncidenceLibraryManager(context, incidenceLibraryConfig);

            //Api.init(context);
            Log.e(LOG_TAG, "IncidenceLibraryManager setup");
        }


    }

}
