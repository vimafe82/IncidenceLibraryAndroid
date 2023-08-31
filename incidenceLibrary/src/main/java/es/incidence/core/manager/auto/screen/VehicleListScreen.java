package es.incidence.core.manager.auto.screen;

import static androidx.car.app.model.Action.BACK;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

import java.util.ArrayList;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Vehicle;

public class VehicleListScreen extends BaseScreen
{
    private boolean isAccident;

    public VehicleListScreen(@NonNull CarContext carContext, boolean isAccident) {
        super(carContext);
        this.isAccident = isAccident;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        ItemList.Builder builder = new ItemList.Builder();

        ArrayList<Vehicle> items = Core.getVehicles();

        for (int i = 0; i < items.size(); i++)
        {
            Vehicle it = items.get(i);

            Row row = new Row.Builder().setTitle(it.getName())
                    .setBrowsable(true)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick() {
                            reportIncidence(it);
                        }
                    }).build();

            builder.addItem(row);
        }



        ItemList list = builder.build();

        ListTemplate template = new ListTemplate.Builder()
                .setTitle(getString(R.string.ask_report_choose_vehicle_simple))
                .setSingleList(list)
                .setHeaderAction(BACK)
                .build();


        return template;
    }

    private void reportIncidence(Vehicle vehicle)
    {
        if (isAccident)
        {
            getScreenManager().push(new AccidentScreen(getCarContext(), vehicle));
        }
        else
        {
            int parent = 2; //Avería es 2
            getScreenManager().push(new FaultListScreen(getCarContext(), parent, vehicle));
        }
    }
}
