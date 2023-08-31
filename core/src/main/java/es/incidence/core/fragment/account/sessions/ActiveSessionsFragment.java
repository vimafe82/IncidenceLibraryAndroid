package es.incidence.core.fragment.account.sessions;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.e510.commons.utils.Utils;

import java.util.ArrayList;

import es.incidence.core.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.Session;
import es.incidence.core.entity.ListItem;
import es.incidence.core.fragment.common.ListFragment;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.INotification;

import static com.e510.commons.utils.LogUtil.makeLogTag;

public class ActiveSessionsFragment  extends ListFragment
{
    private static final String TAG = makeLogTag(ActiveSessionsFragment.class);

    public static ActiveSessionsFragment newInstance()
    {
        ActiveSessionsFragment fragment = new ActiveSessionsFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getTitleId() {
        return R.string.active_sessions;
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
        setNavigationTitle(getString(getTitleId()));
    }

    @Override
    public void loadData()
    {
        showHud();
        Api.getSessions(new IRequestListener() {
            @Override
            public void onFinish(IResponse response) {
                hideHud();
                if (response.isSuccess())
                {
                    ArrayList<ListItem> temp = new ArrayList<>();
                    Drawable rightDrawable = Utils.getDrawable(getContext(), R.drawable.icon_sign_out);
                    int drSize = Utils.dpToPx(24);

                    ArrayList<Session> items = response.getList("sessions", Session.class);
                    for (int i = 0; i < items.size(); i++)
                    {
                        Session s = items.get(i);

                        int idDrawable = (s.manufacturer != null && s.manufacturer.equalsIgnoreCase("apple")) ? R.drawable.icon_apple : R.drawable.icon_android;
                        Drawable drawable = Utils.getDrawable(getContext(), idDrawable);

                        ListItem l1 = new ListItem(s.getName(), drawable, s);
                        l1.rightDrawable = rightDrawable;
                        l1.rightDrawableSize = drSize;
                        temp.add(l1);
                    }

                    renewItems(temp);
                }
                else
                {
                    onBadResponse(response);
                }
            }
        });
    }

    private void onRemoveSession(Session session)
    {
        ArrayList<ListItem> temp = new ArrayList<>();
        for (int i = 0; i < items.size(); i++)
        {
            ListItem item = items.get(i);
            Session s = (Session) item.object;
            if (!s.id.equals(session.id))
            {
                temp.add(item);
            }
        }

        renewItems(temp);
    }

    @Override
    public void onClickRow(Object object)
    {
        if (object instanceof ListItem)
        {
            ListItem item = (ListItem) object;
            Session activeSession = (Session) item.object;
            showSignOutPopUp(activeSession);
        }
    }

    private void showSignOutPopUp(Session session)
    {
        hideKeyboard();

        String title = null;
        String message = getString(R.string.sign_out_on_device, session.getName());
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
                        showHud();
                        Api.deleteSession(new IRequestListener() {
                            @Override
                            public void onFinish(IResponse response) {
                                hideHud();
                                if (response.isSuccess())
                                {
                                    onRemoveSession(session);
                                }
                                else
                                {
                                    onBadResponse(response);
                                }
                            }
                        }, session.id);
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }
}
