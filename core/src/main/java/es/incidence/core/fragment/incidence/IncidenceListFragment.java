package es.incidence.core.fragment.incidence;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import es.incidence.core.R;
import es.incidence.core.adapter.IncidenceListAdapter;
import es.incidence.core.domain.Incidence;
import es.incidence.core.domain.Vehicle;
import es.incidence.core.fragment.IFragment;
import es.incidence.core.utils.view.INavigation;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class IncidenceListFragment extends IFragment
{
    private static final String TAG = makeLogTag(IncidenceListFragment.class);

    private INavigation navigation;
    private ListView listView;
    public ArrayList<Incidence> items;
    private IncidenceListAdapter adapter;

    public static final String KEY_VEHICLE = "KEY_VEHICLE";
    public Vehicle vehicle;

    public static IncidenceListFragment newInstance(Vehicle vehicle)
    {
        IncidenceListFragment fragment = new IncidenceListFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VEHICLE, vehicle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            vehicle = getArguments().getParcelable(KEY_VEHICLE);
        }
    }

    @Override
    public int getTitleId() {
        return R.string.empty;
    }

    @Override
    public int getLayoutRootId() {
        return R.id.layoutRootIncidences;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_incidence_list, container, false);
        setupUI(view);

        return view;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        navigation = rootView.findViewById(R.id.inavigation);
        navigation.init(this, vehicle.getName(), true);

        listView = rootView.findViewById(R.id.listView);
        items = new ArrayList<>();
        adapter = new IncidenceListAdapter(this, items);
        listView.setAdapter(adapter);
    }

    @Override
    public void loadData()
    {
        if (vehicle != null && vehicle.incidences != null)
        {
            items.clear();
            items.addAll(vehicle.incidences);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClickRow(Object object) {
        if (object instanceof Incidence)
        {
            Incidence incidence = (Incidence) object;
            mListener.addFragmentAnimated(IncidenceDetailFragment.newInstance(vehicle, incidence));
        }
    }
}
