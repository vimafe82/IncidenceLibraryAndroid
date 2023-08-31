package es.incidence.core.fragment.account.profile;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.e510.commons.domain.Device;
import com.e510.commons.utils.DateUtils;
import com.e510.commons.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.Date;

import es.incidence.core.Core;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.User;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.ListItemListener;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.utils.IUtils;
import es.incidence.core.utils.view.IField;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class ProfileFragment extends ListFragment
{
    private static final String TAG = makeLogTag(ProfileFragment.class);

    public static final int ROW_NAME = 0;
    public static final int ROW_PHONE = 1;
    public static final int ROW_EMAIL = 2;
    public static final int ROW_DNI = 3;
    public static final int ROW_BIRTHDAY = 4;
    public static final int ROW_APP_VERSION = 5;


    public static ProfileFragment newInstance()
    {
        ProfileFragment fragment = new ProfileFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.vehicles;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
        }
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.TITLE_SUBTITLE;
    }

    @Override
    public void loadData()
    {
        User user = Core.getUser();
        if (user != null)
        {
            setNavigationTitle(getString(R.string.account));
            setNavigationButtonRight(R.drawable.icon_edit);
            setOnNavigationButtonRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickRightNavButton();
                }
            });

            ArrayList<ListItem> temp = new ArrayList<>();

            ListItem l1 = new ListItem(getString(R.string.name), user.name);
            l1.object = ROW_NAME;
            l1.type = IField.TYPE_TEXT;
            l1.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l1);

            String formatted = IUtils.formatPhoneNumber(user.phone);
            ListItem l2 = new ListItem(getString(R.string.mobile_phone), formatted);
            l2.object = ROW_PHONE;
            l2.type = IField.TYPE_PHONE;
            l2.editable = false;
            l2.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l2);

            ListItem l3 = new ListItem(getString(R.string.email), user.email);
            l3.object = ROW_EMAIL;
            l3.type = IField.TYPE_EMAIL;
            l3.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l3);

            ListItem l4 = new ListItem(getString(R.string.nif_doc_identity), user.dni);
            l4.object = ROW_DNI;
            l4.type = IField.TYPE_TEXT;
            l4.dropfield = true;
            l4.titleDrop = (user.identityType != null && user.identityType.id == 2) ? getString(R.string.nie) : getString(R.string.nif);
            l4.menuDrop = R.menu.popup_menu_nif;
            l4.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l4);

            String birthday = user.birthday;
            boolean exclamation = false;
            String exclamationMessage = null;
            if (birthday == null)
            {
                birthday = "-";
                exclamation = true;
                exclamationMessage = getString(R.string.include_birthday_to_complete);
            }
            else
            {
                Date date = DateUtils.parseDate(birthday, DateUtils.DATE);
                birthday = DateUtils.dateToString(date, DateUtils.DATE_ES);
            }
            ListItem l5 = new ListItem(getString(R.string.birthday), birthday);
            l5.object = ROW_BIRTHDAY;
            l5.type = IField.TYPE_DATE;
            l5.exclamation = exclamation;
            l5.exclamationMessage = exclamationMessage;
            l5.listItemListener = new ListItemListener() {
                @Override
                public void onChangeValue() {
                    onChangeValues();
                }
            };
            temp.add(l5);

            Device device = DeviceUtils.getDevice(getContext());
            ListItem l6 = new ListItem(getString(R.string.version_app), device.getAppVersion() + " (" + device.getBuild() + ")");
            l6.object = ROW_APP_VERSION;
            l6.type = IField.TYPE_TEXT;
            l6.editable = false;
            temp.add(l6);

            renewItems(temp);
        }
    }

    @Override
    public void reloadData() {
        loadData();
    }

    @Override
    public void onClickRow(Object object)
    {
    }

    public void onClickRightNavButton()
    {
        mListener.addFragmentAnimated(ProfileEditFragment.newInstance());
    }

    public void onChangeValues()
    {
    }
}