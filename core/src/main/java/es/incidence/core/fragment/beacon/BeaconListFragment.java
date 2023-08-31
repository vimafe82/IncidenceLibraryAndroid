package es.incidence.core.fragment.beacon;

import static com.e510.commons.utils.LogUtil.makeLogTag;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.R;
import es.incidence.core.domain.Beacon;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.add.AddBeaconFragment;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;

public class BeaconListFragment extends ListFragment
{
    private static final String TAG = makeLogTag(BeaconListFragment.class);

    private ArrayList<Beacon> allBeacons = new ArrayList<>();

    private View addBeaconView;

    public static BeaconListFragment newInstance()
    {
        BeaconListFragment fragment = new BeaconListFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
        }
    }

    @Override
    public boolean needEventBus() {
        return true;
    }

    @Override
    public int getTitleId() {
        return R.string.devices;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        /*
        LayoutInflater inflater = LayoutInflater.from(getContext());
        addBeaconView = inflater.inflate(R.layout.row_list, null);
        FontUtils.setTypeValueText(addBeaconView, Constants.FONT_REGULAR, getContext());
        addBeaconView.findViewById(R.id.imgLeft).setVisibility(View.GONE);
        TextView txtTitle = addBeaconView.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.add_new_device));
        txtTitle.setTextColor(Utils.getColor(getContext(), R.color.incidence500));
        ImageView imgRight = addBeaconView.findViewById(R.id.imgRight);
        imgRight.setImageDrawable(Utils.getDrawable(getContext(), R.drawable.icon_plus));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgRight.getLayoutParams();
        params.width = Utils.dpToPx(14);
        params.height = Utils.dpToPx(14);
        params.rightMargin = Utils.dpToPx(21);
        imgRight.setLayoutParams(params);

        addBeaconView.setVisibility(View.GONE);
        addBeaconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addFragmentAnimated(AddBeaconFragment.newInstance(0, 1));
            }
        });
        layoutContent.addView(addBeaconView);
        */
    }

    @Override
    public void loadData()
    {
        showHud();
        Api.getBeacons(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {

                hideHud();

                if (response.isSuccess())
                {
                    allBeacons = response.getList("beacons", Beacon.class);
                    refresh();
                }
                else
                {
                    onBadResponse(response);
                }
            }
        });
    }

    private void refresh()
    {
        ArrayList<ListItem> temp = new ArrayList<>();

        if (allBeacons != null)
        {
            for (int i = 0; i < allBeacons.size(); i++)
            {
                Beacon b1 = allBeacons.get(i);
                String drawable = b1.beaconType != null && b1.beaconType.id == 1 ? "beacon_icon_smart" : "beacon_icon_iot";
                ListItem li = new ListItem(b1.name, drawable, b1);
                li.leftDrawableSize = Utils.dpToPx(24);
                li.exclamation = b1.vehicle == null;
                temp.add(li);
            }
        }

        //Añadimos el añadir
        ListItem li = new ListItem(getString(R.string.add_new_device), null);
        li.object = "add_item";
        li.titleColor = Utils.getColor(getContext(), R.color.incidence500);
        li.rightDrawable = Utils.getDrawable(getContext(), R.drawable.icon_plus);
        li.rightDrawableSize = Utils.dpToPx(14);
        temp.add(li);

        renewItems(temp);

        /*
        if (temp.size() == 0)
        {
            addBeaconView.setVisibility(View.VISIBLE);
        }
        else
        {
            addBeaconView.setVisibility(View.GONE);
        }
        */
    }

    @Override
    public void onClickRow(Object object)
    {
        ListItem listItem = (ListItem) object;
        if (listItem.object instanceof String)
        {
            mListener.addFragmentAnimated(AddBeaconFragment.newInstance(0, 1, null,true));
        }
        else
        {
            Beacon beacon = (Beacon) listItem.object;
            mListener.addFragmentAnimated(BeaconDataFragment.newInstance(beacon));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.BEACON_DELETED)
        {
            Beacon beacon = (Beacon) event.object;
            ArrayList <Beacon> temp = new ArrayList<>();

            if (allBeacons != null) {
                for (int i = 0; i < allBeacons.size(); i++) {
                    Beacon b1 = allBeacons.get(i);
                    if (b1.id == beacon.id) {
                    } else {
                        temp.add(b1);
                    }
                }
            }
            allBeacons = temp;
            refresh();
        }
        else if (event.code == EventCode.BEACON_UPDATED)
        {
            Beacon beacon = (Beacon) event.object;
            ArrayList <Beacon> temp = new ArrayList<>();

            if (allBeacons != null) {
                for (int i = 0; i < allBeacons.size(); i++) {
                    Beacon b1 = allBeacons.get(i);
                    if (b1.id == beacon.id) {
                        temp.add(beacon);
                    } else {
                        temp.add(b1);
                    }
                }
            }
            allBeacons = temp;
            refresh();
        }
    }
}
