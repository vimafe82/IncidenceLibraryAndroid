package es.incidence.core.manager.auto.screen;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;

import org.greenrobot.eventbus.EventBus;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;

public class IncidenceReportedScreen extends BaseScreen
{
    private Vehicle vehicle;
    private Incidence incidence;
    private String phoneCall;

    protected IncidenceReportedScreen(@NonNull CarContext carContext, Vehicle vehicle, Incidence incidence, String phoneCall)
    {
        super(carContext);
        this.vehicle = vehicle;
        this.incidence = incidence;
        this.phoneCall = phoneCall;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        Action action1 = new Action.Builder()
                .setTitle(getString(R.string.call_to, phoneCall))
                .setBackgroundColor(CarColor.BLUE)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        Core.callPhone(phoneCall, true);

                        Core.cleanAndroidAuto();
                        getScreenManager().popToRoot();
                    }
                }).build();

        Action action2 = new Action.Builder()
                .setTitle(getString(R.string.cancel))
                .setBackgroundColor(CarColor.RED)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        /*
                        //Cancelamos incidencia
                        Api.cancelIncidence(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response)
                            {
                                if (response.isSuccess())
                                {
                                    Core.removeData(Constants.KEY_LAST_INCIDENCE_REPORTED_DATE);

                                    EventBus.getDefault().post(new Event(EventCode.INCIDENCE_REPORTED));

                                    Core.cleanAndroidAuto();
                                    getScreenManager().popToRoot();
                                }
                            }
                        }, incidence.id);

                        */

                        Core.cleanAndroidAuto();
                        getScreenManager().popToRoot();


                    }
                }).build();

        String titleInsurance = getString(R.string.your_insurance_contact_you);
        if (vehicle != null && vehicle.insurance != null && vehicle.insurance.textIncidenceAndroid != null) {
            titleInsurance = vehicle.insurance.textIncidenceAndroid;
        } else if (vehicle != null && vehicle.insurance != null && vehicle.insurance.textIncidence != null) {
            titleInsurance = vehicle.insurance.textIncidence;
        }

        String titulo = getString(R.string.nombre_app);
        if (vehicle != null && vehicle.insurance != null && vehicle.insurance.name != null)
        {
            titulo += " · " + vehicle.insurance.name;
        }


        MessageTemplate template = new MessageTemplate.Builder(titleInsurance)
                .setTitle(titulo)
                .addAction(action1)
                .addAction(action2)
                .build();

        return template;
    }
}

