package es.incidence.core.manager.auto.screen;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.manager.IResponse;
import es.incidence.core.manager.insuranceCall.InsuranceCallController;
import es.incidence.core.manager.insuranceCall.InsuranceCallDelegate;

public class InsuranceCallingLoadingScreen  extends BaseScreen implements InsuranceCallDelegate
{
    private int incidenceId;
    private Vehicle vehicle;

    public InsuranceCallingLoadingScreen(@NonNull CarContext carContext, int incidenceId, Vehicle vehicle) {
        super(carContext);
        this.incidenceId = incidenceId;
        this.vehicle = vehicle;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        String titulo = getString(R.string.nombre_app);
        if (vehicle != null && vehicle.insurance != null && vehicle.insurance.name != null)
        {
            titulo += " · " + vehicle.insurance.name;
        }

        MessageTemplate template = new MessageTemplate.Builder(getString(R.string.loading))
                .setTitle(titulo)
                .setIcon(
                        new CarIcon.Builder(
                                IconCompat.createWithResource(
                                        getCarContext(),
                                        R.drawable.loading))
                                .setTint(CarColor.BLUE)
                                .build())
                .build();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                reportIncidence();
            }
        }, 1000);

        return template;
    }

    private void reportIncidence()
    {
        InsuranceCallController.reportIncidence(getCarContext(), this, incidenceId, vehicle, null, false);
    }

    @Override
    public void onLocationErrorResult()
    {
        getScreenManager().push(new AlertScreen(getCarContext(), getString(R.string.alert_error_get_location_message)));
    }

    @Override
    public void onBadResponseReport(IResponse response)
    {
        String message = getString(R.string.alert_error_ws);
        if (response.message != null)
        {
            message = response.message;
        }
        else if (response.status != null && response.status.equals(IResponse.RESPONSE_ERROR_CONNECTION)) {
            message = getString(R.string.alert_error_ws_connection);
        }

        getScreenManager().push(new AlertScreen(getCarContext(), message));
    }

    @Override
    public void onSuccessReportToCall(Incidence incidence)
    {
        makeSuccess(incidence);
    }

    @Override
    public void onSuccessReport(Incidence incidence)
    {
        makeSuccess(incidence);
    }

    private void makeSuccess(Incidence incidence)
    {
        if (incidence != null && incidence.openApp != null && incidence.androidAuto != 0)
        {
            Core.cleanAndroidAuto();
            getScreenManager().popToRoot();

            Core.startNewApp(getCarContext(), incidence.openApp.androidPackage, incidence.openApp.androidDeeplink, incidence.openApp.androidGooglePlayURL);
        }
        else
        {
            InsuranceCallController.locateInsuranceCallPhone(getCarContext(), vehicle, new InsuranceCallController.LocationCallInsuranceListener() {
                @Override
                public void onGetPhone(String phone) {
                    if (phone != null && phone.length() > 0)
                    {
                        getScreenManager().push(new IncidenceReportedScreen(getCarContext(), vehicle, incidence, phone));
                    }
                }
            });
        }
    }
}