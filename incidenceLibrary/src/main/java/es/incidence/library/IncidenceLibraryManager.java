package es.incidence.library;

import android.app.Application;

import es.incidence.core.Core;
import es.incidence.library.config.IncidenceLibraryConfig;

public class IncidenceLibraryManager {

    public static IncidenceLibraryManager instance;

    private Application context;

    private IncidenceLibraryManager(final Application context, IncidenceLibraryConfig incidenceLibraryConfig) {
        this.context = context;
    }

    public static void setup(final Application context, IncidenceLibraryConfig incidenceLibraryConfig) {
        if (instance == null) {
            instance = new IncidenceLibraryManager(context, incidenceLibraryConfig);

            Core.init(context);
        }


    }

}
