package es.incidence.core.fragment.account.notifications;

import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.entity.ListItem;
import es.incidence.core.fragment.common.ListFragment;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class NotificationsFragment extends ListFragment
{
    private static final String TAG = makeLogTag(NotificationsFragment.class);

    private static final int ROW_VEHICLE_BEACON = 0;
    private static final int ROW_PROMOTIONS = 1;

    public static NotificationsFragment newInstance()
    {
        NotificationsFragment fragment = new NotificationsFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.account;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
        }
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.TITLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        //bottom
        TextView textView = new TextView(getContext());
        FontUtils.setTypeValueText(textView, Constants.FONT_REGULAR, getContext());
        textView.setTextColor(Utils.getColor(getContext(), R.color.black400));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            textView.setLineHeight(Utils.dpToPx(22));
        }
        textView.setText(R.string.notifs_beacon_description);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = Utils.dpToPx(24);
        params.rightMargin = Utils.dpToPx(24);
        params.bottomMargin = Utils.dpToPx(50);
        textView.setLayoutParams(params);

        layoutBottom.addView(textView);
    }

    @Override
    public void loadData()
    {
        setNavigationTitle(getString(getTitleId()));

        ArrayList<ListItem> temp = new ArrayList<>();

        ListItem l1 = new ListItem(getString(R.string.vehicle_and_beacon), ROW_VEHICLE_BEACON);
        l1.checkable = true;
        temp.add(l1);

        ListItem l2 = new ListItem(getString(R.string.privacy), ROW_PROMOTIONS);
        l2.checkable = true;
        temp.add(l2);

        renewItems(temp);
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            int row = (int) listItem.object;
        }
    }
}