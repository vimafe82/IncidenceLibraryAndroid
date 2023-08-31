package es.incidence.core.manager.auto.screen;

import static androidx.car.app.model.Action.BACK;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

import java.util.ArrayList;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.IncidenceType;
import es.incidence.core.domain.Vehicle;

public class FaultListScreen extends BaseScreen
{
    private int parent;
    private Vehicle vehicle;

    public FaultListScreen(@NonNull CarContext carContext, int parent, Vehicle vehicle) {
        super(carContext);
        this.parent = parent;
        this.vehicle = vehicle;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        ItemList.Builder builder = new ItemList.Builder();

        ArrayList<IncidenceType> items = Core.getIncidencesTypes(parent);
        for (int i = 0; i < items.size(); i++)
        {
            IncidenceType it = items.get(i);

            Row row = new Row.Builder().setTitle(it.name)
                    .setBrowsable(true)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick() {
                            reportIncidence(it.id);
                        }
                    }).build();

            builder.addItem(row);
        }


        ItemList list = builder.build();


        String titulo = getString(R.string.ask_fault_simple);
        if (vehicle != null && vehicle.insurance != null && vehicle.insurance.name != null)
        {
            titulo = vehicle.insurance.name + " · " + getString(R.string.ask_fault_simple);
        }

        ListTemplate template = new ListTemplate.Builder()
                .setTitle(titulo)
                .setSingleList(list)
                .setHeaderAction(BACK)
                .build();


        return template;
    }

    private void reportIncidence(int incidenceTypeId)
    {
        ArrayList<IncidenceType> list = Core.getIncidencesTypes(incidenceTypeId);
        if (list != null && list.size() > 0)
        {
            getScreenManager().push(new FaultListScreen(getCarContext(), incidenceTypeId, vehicle));
        }
        else
        {
            //getScreenManager().push(new InsuranceCallingScreen(getCarContext(), incidenceTypeId, vehicle));

            Core.ANDROID_AUTO_vehicle = vehicle;
            Core.ANDROID_AUTO_incidenceId = incidenceTypeId;
            getScreenManager().popToRoot();
        }
    }
}