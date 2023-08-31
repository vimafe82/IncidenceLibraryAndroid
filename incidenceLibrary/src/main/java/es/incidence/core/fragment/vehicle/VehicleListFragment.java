package es.incidence.core.fragment.vehicle;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import com.e510.incidencelibrary.R;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.add.AddVehicleFragment;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.fragment.incidence.IncidenceListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;

public class VehicleListFragment extends ListFragment
{
    private static final String TAG = makeLogTag(VehicleListFragment.class);

    public static final int FROM_VEHICLES = 0;
    public static final int FROM_INCIDENCES = 1;

    public static final String KEY_FROM = "KEY_FROM";
    public int from;

    private View addVehicleView;

    public static VehicleListFragment newInstance(int from)
    {
        VehicleListFragment fragment = new VehicleListFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_FROM, from);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            from = getArguments().getInt(KEY_FROM);
        }
    }

    @Override
    public int getTitleId() {
        return R.string.vehicles;
    }

    @Override
    public boolean needEventBus() {
        return true;
    }


    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        if (from == FROM_INCIDENCES)
        {
            setNavigationTitle(getString(R.string.incidences));
        }

        /*
        LayoutInflater inflater = LayoutInflater.from(getContext());
        addVehicleView = inflater.inflate(R.layout.row_list, null);
        FontUtils.setTypeValueText(addVehicleView, Constants.FONT_REGULAR, getContext());
        addVehicleView.findViewById(R.id.imgLeft).setVisibility(View.GONE);
        TextView txtTitle = addVehicleView.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.add_new_vehicle));
        txtTitle.setTextColor(Utils.getColor(getContext(), R.color.incidence500));
        ImageView imgRight = addVehicleView.findViewById(R.id.imgRight);
        imgRight.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.icon_plus));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgRight.getLayoutParams();
        params.width = Utils.dpToPx(14);
        params.height = Utils.dpToPx(14);
        params.rightMargin = Utils.dpToPx(21);
        imgRight.setLayoutParams(params);

        addVehicleView.setVisibility(View.GONE);
        addVehicleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addFragmentAnimated(AddVehicleFragment.newInstance(false));
            }
        });
        layoutContent.addView(addVehicleView);
        */
    }

    @Override
    public void loadData()
    {
        showHud();
        Api.getVehicles(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {

                hideHud();

                if (response.isSuccess())
                {
                    ArrayList<ListItem> temp = new ArrayList<>();

                    Drawable exclamationDrawable = Utils.getDrawable(getContext(), R.drawable.icon_ellipse_exclamation_blue);
                    ArrayList<Vehicle> items = response.getList("vehicles", Vehicle.class);
                    if (items != null)
                    {
                        for (int i = 0; i < items.size(); i++)
                        {
                            Vehicle vehicle = items.get(i);
                            ListItem li = new ListItem(vehicle.getName(), vehicle.image, vehicle);
                            li.exclamation = vehicle.hasIncidencesActive();
                            li.exclamationDrawable = exclamationDrawable;
                            temp.add(li);
                        }
                    }

                    //Añadimos el añadir
                    if (from != FROM_INCIDENCES)
                    {
                        ListItem li = new ListItem(getString(R.string.add_new_vehicle), null);
                        li.object = "add_item";
                        li.titleColor = Utils.getColor(getContext(), R.color.incidence500);
                        li.rightDrawable = Utils.getDrawable(getContext(), R.drawable.icon_plus);
                        li.rightDrawableSize = Utils.dpToPx(14);
                        temp.add(li);
                    }

                    renewItems(temp);
                    /*if (temp.size() == 0)
                    {
                        addVehicleView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        addVehicleView.setVisibility(View.GONE);
                    }*/
                }
                else
                {
                    onBadResponse(response);
                }
            }
        });
    }

    @Override
    public void onClickRow(Object object) {

        /*
        Vehicle vehicle = new Vehicle();
        vehicle.brand = "Volkswagen";
        vehicle.model = "Golf";
        vehicle.licensePlate = "5156 BQB";
        vehicle.registrationYear = "2000";
        vehicle.colorId = "White";

        Policy insurance = new Policy();
        insurance.name = "Allianz";
        insurance.phone = "123457890";
        insurance.documentTitular = "01234567L";
        insurance.caducity = "21/12/2028";
        vehicle.insurancePolicy = insurance;

        Beacon beacon = new Beacon();
        beacon.name = "Ford Focus";
        beacon.model = "Help Flash Smart";
        vehicle.beacon = beacon;

        ArrayList<Incidence> incidences = new ArrayList<>();
        Incidence i1 = new Incidence();
        i1.title = "Pinchazo en Lugo";
        i1.address = "c/ Gran vía";
        i1.date = "12 ene 2021";
        i1.status = "1";
        i1.valoration = 4;
        incidences.add(i1);
        Incidence i2 = new Incidence();
        i2.title = "Accidente en Lugo";
        i2.address = "c/ Gran Vía";
        i2.date = "12 ene 2021";
        i2.status = "0";
        incidences.add(i2);
        Incidence i3 = new Incidence();
        i3.title = "Avería en Lugo";
        i3.address = "c/ Gran Vía ejemplo de text...";
        i3.date = "12 ene 2021";
        i3.status = "0";
        incidences.add(i3);
        vehicle.incidences = incidences;


        ArrayList<Driver> drivers = new ArrayList<>();
        Driver d1 = new Driver();
        d1.name = "Pedro García";
        drivers.add(d1);
        Driver d2 = new Driver();
        d2.name = "María Turela";
        drivers.add(d2);
        vehicle.drivers = drivers;
        */

        ListItem listItem = (ListItem) object;

        if (listItem.object instanceof String)
        {
            mListener.addFragmentAnimated(AddVehicleFragment.newInstance(false));
        }
        else
        {
            Vehicle vehicle = (Vehicle) listItem.object;

            if (from == FROM_VEHICLES)
            {
                mListener.addFragmentAnimated(VehicleOptionsFragment.newInstance(vehicle));
            }
            else if (from == FROM_INCIDENCES)
            {
                mListener.addFragmentAnimated(IncidenceListFragment.newInstance(vehicle));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.VEHICLE_UPDATED)
        {
            ArrayList<ListItem> list = new ArrayList<>();

            Vehicle temp = (Vehicle) event.object;
            for (int i = 0; i < items.size(); i++)
            {
                ListItem listItem = items.get(i);
                if (listItem.object instanceof Vehicle)
                {
                    Vehicle vehicle = (Vehicle) listItem.object;
                    if (temp.id.equals(vehicle.id))
                    {
                        listItem = new ListItem(temp.getName(), temp.image, temp);
                    }
                }

                list.add(listItem);
            }

            renewItems(list);
        }
        else if (event.code == EventCode.VEHICLE_DELETED)
        {
            ArrayList<ListItem> list = new ArrayList<>();

            Vehicle temp = (Vehicle) event.object;
            for (int i = 0; i < items.size(); i++)
            {
                ListItem listItem = items.get(i);
                if (listItem.object instanceof Vehicle)
                {
                    Vehicle vehicle = (Vehicle) listItem.object;
                    if (!temp.id.equals(vehicle.id))
                    {
                        list.add(listItem);
                    }
                }
                else
                {
                    list.add(listItem);
                }
            }

            renewItems(list);
        }
    }
}
