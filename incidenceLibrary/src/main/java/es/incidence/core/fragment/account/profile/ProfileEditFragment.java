package es.incidence.core.fragment.account.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.commons.fragment.BaseFragment;
import com.e510.commons.utils.DateUtils;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;

import es.incidence.core.Constants;
import com.e510.incidencelibrary.R;
import es.incidence.core.adapter.ListAdapter;
import es.incidence.core.domain.IdentityType;
import es.incidence.core.entity.ListItem;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.manager.Api;
import es.incidence.core.manager.IRequestListener;
import es.incidence.core.manager.IResponse;
import es.incidence.core.utils.view.IButton;
import es.incidence.core.utils.view.INotification;

public class ProfileEditFragment extends ProfileFragment
{
    private IButton btnContinue;
    private boolean hasChanges = false;

    public static ProfileEditFragment newInstance()
    {
        ProfileEditFragment fragment = new ProfileEditFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public ListAdapter.Type getType() {
        return ListAdapter.Type.EDITABLE;
    }

    @Override
    public void setupUI(View rootView) {
        super.setupUI(rootView);

        layoutBottom.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_continue_dismiss, null);
        FontUtils.setTypeValueText(view, Constants.FONT_REGULAR, getContext());
        TextView txtAddLater = view.findViewById(R.id.txtAddLater);
        txtAddLater.setText(getString(R.string.cancel));
        txtAddLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelClose();
            }
        });
        FontUtils.setTypeValueText(txtAddLater, Constants.FONT_SEMIBOLD, getContext());

        btnContinue = view.findViewById(R.id.btnContinueColor);
        btnContinue.setPrimaryColors();
        btnContinue.setText(getString(R.string.save));
        FontUtils.setTypeValueText(btnContinue, Constants.FONT_SEMIBOLD, getContext());
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(false);
            }
        });

        layoutBottom.addView(view);
    }

    @Override
    public void loadData() {
        super.loadData();

        setNavigationButtonRight(R.drawable.icon_close);
        setNavigationButtonRightTintColor(Utils.getColor(getContext(), R.color.black600));
    }

    @Override
    public void onClickRightNavButton()
    {
        if (hasChanges && !btnContinue.isDisabled())
        {
            showSavePopUp(true);
        }
        else
        {
            mListener.cleanAllBackStackEntries();
        }
    }

    private void save(boolean cleanAll)
    {
        String name = getItem(ROW_NAME).subtitle;
        String phone = getItem(ROW_PHONE).subtitle;
        phone = phone.replaceAll(" ", "");

        ListItem listItemDNI = getItem(ROW_DNI);
        IdentityType identityType = new IdentityType();
        identityType.name = listItemDNI.titleDrop;

        if (identityType.name != null && identityType.name.equals("DNI")) {
            identityType.id = 1;
        } else if (identityType.name != null && identityType.name.equals("NIE")) {
            identityType.id = 2;
        } else if (identityType.name != null && identityType.name.equals("CIF")) {
            identityType.id = 3;
        } else {
            identityType.id = 1;
        }

        String dni = getItem(ROW_DNI).subtitle;
        String email = getItem(ROW_EMAIL).subtitle;
        String birthday = getItem(ROW_BIRTHDAY).subtitle;
        if (birthday != null && !birthday.equals("-") && birthday.length() > 0)
        {
            Date date = DateUtils.parseDate(birthday, DateUtils.DATE_ES);
            birthday = DateUtils.dateToString(date, DateUtils.DATE);
        }
        else
        {
            birthday = null;
        }

        if (name != null && name.length() > 0)
        {
            if (email != null && email.length() > 0)
            {
                if (dni != null && dni.length() > 0)
                {
                    showHud();
                    Api.updateUser(new IRequestListener() {
                        @Override
                        public void onFinish(IResponse response) {
                            hideHud();
                            if (response.isSuccess())
                            {
                                hasChanges = false;

                                BaseFragment baseFragment = mListener.getPenultimFragment();
                                if (baseFragment != null) {
                                    baseFragment.reloadData();
                                }

                                EventBus.getDefault().post(new Event(EventCode.USER_UPDATED));

                                if (cleanAll) {
                                    mListener.cleanAllBackStackEntries();
                                } else {
                                    closeThis();
                                }
                            }
                            else
                            {
                                onBadResponse(response);
                            }
                        }
                    }, name, phone, identityType.id+"", dni, email, birthday, null);
                }
                else
                {
                    showAlert(R.string.alert_inform_dni);
                }
            }
            else
            {
                showAlert(R.string.alert_inform_email);
            }
        }
        else
        {
            showAlert(R.string.alert_inform_name);
        }
    }

    @Override
    public void onChangeValues() {

        String name = getItem(ROW_NAME).subtitle;
        String dni = getItem(ROW_DNI).subtitle;
        String email = getItem(ROW_EMAIL).subtitle;

        if (name != null && name.length() > 0 && dni != null && dni.length() > 0 && email != null && email.length() > 0)
        {
            hasChanges = true;
            btnContinue.setPrimaryColors();
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    save(false);
                }
            });
        }
        else
        {
            btnContinue.setDisabledColors();
            btnContinue.setOnClickListener(null);
        }
    }

    @Override
    public boolean onBackPressed()
    {
        if (hasChanges && !btnContinue.isDisabled())
        {
            showSavePopUp(false);

            return true;
        }

        return super.onBackPressed();
    }

    private void cancelClose()
    {
        if (hasChanges && !btnContinue.isDisabled())
        {
            showSavePopUp(false);
        }
        else
        {
            closeThis();
        }
    }


    private void showSavePopUp(boolean cleanAll)
    {
        hideKeyboard();

        String title = getString(R.string.wish_continue);;
        String message = getString(R.string.no_saved_changes);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.save_and_close));
        options.add(getString(R.string.no_save));
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
                        //save_and_close
                        save(cleanAll);
                    }
                    else if (index == 1)
                    {
                        //no_save
                        hasChanges = false;
                        if (cleanAll) {
                            mListener.cleanAllBackStackEntries();
                        } else {
                            closeThis();
                        }
                    }
                }
            }
        };

        RelativeLayout layoutToShow = getBaseActivity().findViewById(R.id.mainBaseLayout);
        INotification.shared(getContext()).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }
}
