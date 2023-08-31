package es.incidence.core.manager.auto.screen;

import static androidx.car.app.model.Row.IMAGE_TYPE_ICON;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Pane;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Vehicle;

public class InsuranceCallingScreen extends BaseScreen
{
    private int incidenceId;
    private Vehicle vehicle;

    public InsuranceCallingScreen(@NonNull CarContext carContext, int incidenceId, Vehicle vehicle) {
        super(carContext);
        this.incidenceId = incidenceId;
        this.vehicle = vehicle;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        ItemList.Builder builder = new ItemList.Builder();
        Pane.Builder pane = new Pane.Builder();



        Row row1 = new Row.Builder()
                .setTitle(getString(R.string.incidence_tip_beacon))
                .setImage(new CarIcon.Builder(
                        IconCompat.createWithResource(
                                getCarContext(),
                                R.drawable.icon_tip_beacon)).build(), IMAGE_TYPE_ICON)
                .build();
        builder.addItem(row1);
        pane.addRow(row1);

        Row row2 = new Row.Builder()
                .setTitle(getString(R.string.incidence_tip_lights))
                .setImage(new CarIcon.Builder(
                        IconCompat.createWithResource(
                                getCarContext(),
                                R.drawable.icon_tip_sun)).build(), IMAGE_TYPE_ICON)
                .build();
        builder.addItem(row2);
        pane.addRow(row2);

        Row row3 = new Row.Builder()
                .setTitle(getString(R.string.incidence_tip_vest))
                .setImage(new CarIcon.Builder(
                        IconCompat.createWithResource(
                                getCarContext(),
                                R.drawable.icon_tip_vest)).build(), IMAGE_TYPE_ICON)
                .build();
        builder.addItem(row3);
        pane.addRow(row3);

        Row row4 = new Row.Builder()
                .setTitle(getString(R.string.incidence_tip_exit_car))
                .setImage(new CarIcon.Builder(
                        IconCompat.createWithResource(
                                getCarContext(),
                                R.drawable.icon_tip_vehicle)).build(), IMAGE_TYPE_ICON)
                .build();
        builder.addItem(row4);
        pane.addRow(row4);



        ItemList list = builder.build();

        String titleInsurance = getString(R.string.your_insurance_contact_you);
        if (vehicle != null && vehicle.insurance != null && vehicle.insurance.textIncidenceAndroid != null) {
            titleInsurance = vehicle.insurance.textIncidenceAndroid;
        }
        else if (vehicle != null && vehicle.insurance != null && vehicle.insurance.textIncidence != null) {
            titleInsurance = vehicle.insurance.textIncidence;
        }

        ListTemplate template = new ListTemplate.Builder()
                .setTitle(titleInsurance)
                //.setHeaderAction(BACK)
                .setSingleList(list)
                .setActionStrip(
                        new ActionStrip.Builder()
                                .addAction(new Action.Builder()
                                        .setIcon(
                                                new CarIcon.Builder(
                                                        IconCompat.createWithResource(
                                                                getCarContext(),
                                                                R.drawable.ic_close))
                                                        .build())
                                        .setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick()
                                            {
                                                Core.cleanAndroidAuto();
                                                getScreenManager().popToRoot();
                                            }
                                        })
                                        .build())
                                .addAction(
                                        new Action.Builder()
                                                .setTitle(getString(R.string.accept))
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick()
                                                    {
                                                        reportIncidence();
                                                    }
                                                })
                                                .build())
                                .build())
                .build();


        /*
        PaneTemplate template1 = new PaneTemplate.Builder(pane.build())
                .setTitle(getString(R.string.your_insurance_contact_you))
                .setHeaderAction(BACK)
                .setActionStrip(
                        new ActionStrip.Builder()
                                .addAction(
                                        new Action.Builder()
                                                .setTitle(getString(R.string.accept))
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick()
                                                    {
                                                        reportIncidence();
                                                    }
                                                })
                                                .build())
                                .build())
                .build();
        */

        return template;
    }

    private void reportIncidence()
    {
        //InsuranceCallController.reportIncidence(getCarContext(), this, incidenceId, vehicle, null, false);
        getScreenManager().push(new InsuranceCallingLoadingScreen(getCarContext(), incidenceId, vehicle));
    }
}