package es.incidence.core.manager.auto.screen;

import static androidx.car.app.model.Action.BACK;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Template;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.manager.IResponse;
import es.incidence.core.manager.insuranceCall.InsuranceCallController;
import es.incidence.core.manager.insuranceCall.InsuranceCallDelegate;

public class AccidentScreen extends BaseScreen implements InsuranceCallDelegate {

    private Vehicle vehicle;
    private String phone = null;

    public AccidentScreen(@NonNull CarContext carContext, Vehicle vehicle) {
        super(carContext);
        this.vehicle = vehicle;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {

        Action action1 = new Action.Builder()
                .setTitle(getString(R.string.no))
                .setBackgroundColor(CarColor.BLUE)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        InsuranceCallController.locateInsuranceCallPhone(getCarContext(), vehicle, new InsuranceCallController.LocationCallInsuranceListener() {
                            @Override
                            public void onGetPhone(String phone)
                            {
                                if (phone != null && phone.length() > 0)
                                {
                                    AccidentScreen.this.phone = phone;
                                    reportIncidence(Constants.ACCIDENT_TYPE_ONLY_MATERIAL);
                                }
                            }
                        });
                    }
                }).build();

        Action action2 = new Action.Builder()
                .setTitle(getString(R.string.yes))
                .setBackgroundColor(CarColor.RED)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        phone = Constants.PHONE_EMERGENCY;
                        reportIncidence(Constants.ACCIDENT_TYPE_WOUNDED);
                    }
                }).build();

        MessageTemplate template = new MessageTemplate.Builder(getString(R.string.ask_wounded))
                .setTitle(getString(R.string.nombre_app))
                .setHeaderAction(BACK)
                .addAction(action1).addAction(action2).build();


        return template;
    }

    private void reportIncidence(int incidenceId)
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
        makeSuccess();
    }

    @Override
    public void onSuccessReport(Incidence incidence)
    {
        makeSuccess();
    }

    private void makeSuccess()
    {
        Core.callPhone(phone, true);
        getScreenManager().popToRoot();
    }
}
