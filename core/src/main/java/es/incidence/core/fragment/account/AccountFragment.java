package es.incidence.core.fragment.account;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.Core;
import es.incidence.core.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.DeviceNotification;
import es.incidence.core.entity.ListItem;
import es.incidence.core.fragment.account.help.HelpFragment;
import es.incidence.core.fragment.account.profile.ProfileFragment;
import es.incidence.core.fragment.account.sessions.ActiveSessionsFragment;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.INotification;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class AccountFragment extends ListFragment
{
    private static final String TAG = makeLogTag(AccountFragment.class);

    private static final int ROW_MY_DATA = 0;
    private static final int ROW_ACTIVE_SESSIONS = 1;
    private static final int ROW_NOTIFICATIONS = 2;
    private static final int ROW_HELP = 3;
    private static final int ROW_SIGN_OUT = 4;
    private static final int ROW_DELETE_ACCOUNT = 5;

    private static final int NOTIFICATIONS_SWITCH_ID = 1;

    public static AccountFragment newInstance()
    {
        AccountFragment fragment = new AccountFragment();

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
    public void loadData()
    {
        setNavigationTitle(getString(getTitleId()));

        ArrayList<ListItem> temp = new ArrayList<>();

        int color = Utils.getColor(getContext(), R.color.black600);
        Drawable drawable = Utils.getDrawable(getContext(), R.drawable.icon_account_user);
        drawable.setTint(color);
        ListItem l1 = new ListItem(getString(R.string.my_data), ROW_MY_DATA);
        l1.leftDrawable = drawable;
        temp.add(l1);

        drawable = Utils.getDrawable(getContext(), R.drawable.icon_smartphone);
        drawable.setTint(color);
        ListItem l2 = new ListItem(getString(R.string.active_sessions), ROW_ACTIVE_SESSIONS);
        l2.leftDrawable = drawable;
        temp.add(l2);

        drawable = Utils.getDrawable(getContext(), R.drawable.icon_bell);
        drawable.setTint(color);
        ListItem l3 = new ListItem(getString(R.string.notifications), ROW_NOTIFICATIONS);
        l3.leftDrawable = drawable;
        l3.checkable = true;
        DeviceNotification dn = Core.getDeviceNotification(NOTIFICATIONS_SWITCH_ID);
        boolean checked = (dn != null && dn.status == 0) ? false : true;
        l3.setChecked(checked);
        temp.add(l3);
        /*
        drawable = Utils.getDrawable(getContext(), R.drawable.icon_help);
        drawable.setTint(color);
        ListItem l4 = new ListItem(getString(R.string.help), ROW_HELP);
        l4.leftDrawable = drawable;
        temp.add(l4);
        */
        renewItems(temp);

        //bottom
        ListItem lBottom1 = new ListItem(getString(R.string.sign_out), ROW_SIGN_OUT);
        addRowBottom(lBottom1);
        ListItem lBottom = new ListItem(getString(R.string.delete_account), ROW_DELETE_ACCOUNT);
        lBottom.titleColor = Utils.getColor(getContext(), R.color.error);
        lBottom.arrowColor = Utils.getColor(getContext(), R.color.error);
        addRowBottom(lBottom);
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem listItem = (ListItem) object;
            int row = (int) listItem.object;

            if (row == ROW_MY_DATA)
            {
                mListener.addFragmentAnimated(ProfileFragment.newInstance());
            }
            else if (row == ROW_ACTIVE_SESSIONS)
            {
                mListener.addFragmentAnimated(ActiveSessionsFragment.newInstance());
            }
            else if (row == ROW_NOTIFICATIONS)
            {
                if (!listItem.isChecked())
                {
                    showNotificationsPopUp(listItem);
                }
                else
                {
                    showHud();
                    Api.setDeviceNotifications(new IRequestListener() {
                        @Override
                        public void onFinish(IResponse response)
                        {
                            hideHud();
                            if (!response.isSuccess())
                            {
                                listItem.setChecked(false);
                                reloadData();
                            }

                        }
                    }, NOTIFICATIONS_SWITCH_ID+"", 1+"");
                    //mListener.addFragmentAnimated(NotificationsFragment.newInstance());
                }
            }
            else if (row == ROW_HELP)
            {
                mListener.addFragmentAnimated(HelpFragment.newInstance());
            }
            else if (row == ROW_SIGN_OUT)
            {
                showSignOutPopUp();
            }
            else if (row == ROW_DELETE_ACCOUNT)
            {
                showDeleteAccountPopUp();
            }
        }
    }

    private void showNotificationsPopUp(ListItem listItem)
    {
        hideKeyboard();

        String title = null;
        String message = getString(R.string.notifications_disable_message);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.cancel));
        options.add(getString(R.string.accept));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //cancela
                        listItem.setChecked(true);
                        reloadData();
                    }
                    else if (index == 1)
                    {
                        showHud();
                        Api.setDeviceNotifications(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response)
                            {
                                hideHud();
                                if (!response.isSuccess())
                                {
                                    listItem.setChecked(true);
                                    reloadData();
                                }

                            }
                        }, NOTIFICATIONS_SWITCH_ID+"", 0+"");
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    private void showSignOutPopUp()
    {
        hideKeyboard();

        String title = null;
        String message = getString(R.string.sign_out_message);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.cancel));
        options.add(getString(R.string.sign_out));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //cancela
                    }
                    else if (index == 1)
                    {
                        //cierra sesión
                        Core.signOut();
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    private void showDeleteAccountPopUp()
    {
        hideKeyboard();

        String title = null;
        String message = getString(R.string.delete_account_message);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.cancel));
        options.add(getString(R.string.delete));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(getContext(), R.color.black600));
        optionsColors.add(Utils.getColor(getContext(), R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //cancela
                    }
                    else if (index == 1)
                    {
                        //elimina
                        showHud();
                        Api.deleteAccount(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response) {
                                if (response.isSuccess())
                                {
                                    Core.signOut();
                                }
                                else
                                {
                                    hideHud();
                                    onBadResponse(response);
                                }
                            }
                        });
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }
}