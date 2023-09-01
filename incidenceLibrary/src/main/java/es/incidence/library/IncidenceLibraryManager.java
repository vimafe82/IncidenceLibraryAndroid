package es.incidence.library;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.e510.commons.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import es.incidence.core.activity.SimpleMainActivity;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.library.config.IncidenceLibraryConfig;

public class IncidenceLibraryManager {

    public static final String SCREEN_OK = "SCREEN_OK";
    public static final String SCREEN_KO = "SCREEN_KO";
    public static final String NO_VALID_API_KEY = "NO_VALID_API_KEY";
    public static final String CALLIG_VALIDATE_API_KEY = "CALLIG_VALIDATE_API_KEY";
    public static IncidenceLibraryManager instance;

    private Application context;

    private Boolean validApiKey = null;
    private List<String> screens = new ArrayList<>();
    private ArrayList<BaseActivity> activities = new ArrayList<>();
    protected int stateCounter = 0;


    private IncidenceLibraryManager(final Application context, IncidenceLibraryConfig incidenceLibraryConfig) {
        this.context = context;
    }

    public static void setup(final Application context, IncidenceLibraryConfig incidenceLibraryConfig) {
        if (instance == null) {
            instance = new IncidenceLibraryManager(context, incidenceLibraryConfig);

            Core.init(context);
        }

        instance.validateApiKey();
    }

    private void validateApiKey() {
        Api.validateApiKey(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {

                if (response.isSuccess())
                {
                    instance.validApiKey = true;

                    instance.screens.add(Constants.SCREEN_DEVELOPER);
                    instance.screens.add(Constants.SCREEN_DEVICE_LIST);
                } else {
                    instance.validApiKey = false;
                }
            }
        });
    }

    private String validateScreen(String screen) {
        if (validApiKey == null) {
            return CALLIG_VALIDATE_API_KEY;
        } else if (validApiKey == false) {
            return NO_VALID_API_KEY;
        } else if (instance.screens.contains(screen)) {
            return SCREEN_OK;
        } else {
            return SCREEN_KO;
        }
    }

    public Intent getDeviceListViewController() {
        String res = validateScreen(Constants.SCREEN_DEVICE_LIST);
        if (res == SCREEN_OK) {
            //let viewModel = DeviceListViewModel()
            //let viewController = DeviceListViewController.create(with: viewModel)
            //return viewController

            Intent intent = new Intent(context, SimpleMainActivity.class);
            Bundle b = new Bundle();
            b.putString("scree", Constants.SCREEN_DEVICE_LIST);
            intent.putExtras(b);
            return intent;
        } else {
            return processScreenError(res);
        }
    }

    private Intent processScreenError(String error) {
        /*
        if (error == "NO_VALID_API_KEY") {
            let viewController = ErrorViewController.create()
            return viewController
        }
         */
        Intent intent = new Intent(context, SimpleMainActivity.class);
        Bundle b = new Bundle();
        b.putString("screen", Constants.SCREEN_ERROR);
        b.putString("error", error);
        intent.putExtras(b);
        return intent;
    }

    public void activityCreated(BaseActivity activity)
    {
        if (activity != null && !activities.contains(activity))
            activities.add(activity);
    }
    public void activityDestroyed(BaseActivity activity)
    {
        if (activity != null)
            activities.remove(activity);
    }

    public BaseActivity getCurrentActivity()
    {
        BaseActivity baseActivity = null;
        if (activities.size() > 0) {
            baseActivity = activities.get(activities.size()-1);
        }

        return baseActivity;
    }

    public int getActivitiesCount()
    {
        return activities.size();
    }

    public void activityStarted()
    {
        stateCounter++;
    }

    public void activityStopped()
    {
        stateCounter--;
    }

    public int getStateCounter()
    {
        return stateCounter;
    }

}
