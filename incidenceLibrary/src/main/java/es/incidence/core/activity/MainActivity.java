package es.incidence.core.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.e510.commons.fragment.FragmentAnimation;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;
import com.e510.incidencelibrary.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mobeedev.library.SlidingMenuBuilder;
import com.mobeedev.library.SlidingNavigation;
import com.mobeedev.library.gravity.SlideGravity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import es.incidence.core.Constants;
import es.incidence.core.Core;
import es.incidence.core.domain.User;
import es.incidence.core.entity.event.Event;
import es.incidence.core.entity.event.EventCode;
import es.incidence.core.fragment.account.AccountFragment;
import es.incidence.core.fragment.account.help.HelpFragment;
import es.incidence.core.fragment.add.AddFragment;
import es.incidence.core.fragment.beacon.BeaconListFragment;
import es.incidence.core.fragment.common.SelectLanguageFragment;
import es.incidence.core.fragment.ecommerce.EcommerceFragment;
import es.incidence.core.fragment.home.HomeFragment;
import es.incidence.core.fragment.sign.SignInFragment;
import es.incidence.core.fragment.vehicle.VehicleListFragment;
import es.incidence.core.fragment.welcome.WelcomeFragment;
import es.incidence.core.manager.SettingsContentObserver;
import es.incidence.core.manager.SpeechManager;
import es.incidence.core.utils.view.INotification;

public class MainActivity extends IActivity
{
    private View sideMenu;
    private TextView txtHeader;
    private ImageView imgLanguage;
    private TextView txtLanguage;
    private SlidingNavigation slidingNavigation;

    private SettingsContentObserver mSettingsContentObserver;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //setButtonText("Bluetooth off");
                        checkBluetoothConnection();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //setButtonText("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //setButtonText("Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //setButtonText("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main_incidence);
        prepareToolbar();

        if (Core.getUser() == null)
        {
            String userSignOut = Core.loadData(Constants.KEY_USER_SIGNOUT);
            if (userSignOut != null)
            {
                //Ya no eliminamos, mantenemos siempre en pantalla login. (BH05. Login. LOGIN tras cerrar sesión)
                //Core.removeData(Constants.KEY_USER_SIGNOUT);

                showInitialFragment(R.id.layout_activity_main, SignInFragment.newInstance());
                setToolbarColor(Utils.getColor(this, R.color.incidence100), Utils.getColor(this, R.color.black700), true);
            }
            else
            {
                showInitialFragment(WelcomeFragment.newInstance());
            }
        }
        else
        {
            showHome();
            checkBluetoothConnection();
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        mSettingsContentObserver = new SettingsContentObserver(new Handler(), this);
        getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                mSettingsContentObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
        SpeechManager.destroy();
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    @Override
    public void checkDispatchTouchEvent() {
        //sobreescribimos porque no queremos que el parent actue que sino no hace caso al botón X de limpiar text4o en los fields.
    }

    private void showHome()
    {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        findViewById(R.id.toolbar).setVisibility(View.GONE);

        showInitialFragment(HomeFragment.newInstance());

        sideMenu = View.inflate(this, R.layout.side_menu, null);
        FontUtils.setTypeValueText(sideMenu, Constants.FONT_REGULAR, this);
        txtHeader = sideMenu.findViewById(R.id.txtHeader);
        FontUtils.setTypeValueText(txtHeader, Constants.FONT_SEMIBOLD, this);
        imgLanguage = sideMenu.findViewById(R.id.menuRowLanguageImgLeft);
        txtLanguage = sideMenu.findViewById(R.id.menuRowLanguageTxtTitle);
        printUserInfo();

        sideMenu.findViewById(R.id.menuRow0).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuRow1).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuRow2).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuRow3).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuRow4).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuAdd).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuRowLanguage).setOnClickListener(this);
        sideMenu.findViewById(R.id.menuRowHelp).setOnClickListener(this);

        slidingNavigation = new SlidingMenuBuilder(this)
                .withDragDistance(275) //Horizontal translation of a view. Default == 180dp
                .withRootViewScale(1.0f) //Content view's scale will be interpolated between 1f and 0.7f. Default == 0.65f;
                .withRootViewElevation(10) //Content view's elevation will be interpolated between 0 and 10dp. Default == 8.
                .withRootViewYTranslation(0) //Content view's translationY will be interpolated between 0 and 4. Default == 0
                //.addRootTransformation(customTransformation)
                .withMenuView(sideMenu)
                .withGravity(SlideGravity.RIGHT)
                .inject();

        setToolbarColor(Utils.getColor(this, R.color.incidence100), Utils.getColor(this, R.color.black700), true);
    }

    private void printUserInfo()
    {
        if (txtHeader != null)
        {
            User user = Core.getUser();
            String name = user.alias != null ? user.alias : user.name;
            txtHeader.setText(getString(R.string.hola_nombre, name));
        }

        if (imgLanguage != null && txtLanguage != null)
        {
            String language = Core.getLanguage();
            if (language != null)
            {
                if (language.equals("en"))
                {
                    imgLanguage.setImageDrawable(Utils.getDrawable(this, R.drawable.flag_en));
                    txtLanguage.setText(getString(R.string.lang_english));
                }
                else
                {
                    imgLanguage.setImageDrawable(Utils.getDrawable(this, R.drawable.flag_es));
                    txtLanguage.setText(getString(R.string.lang_spanish));
                }
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.imgHamburgerClick)
        {
            if (slidingNavigation.isMenuOpened())
                slidingNavigation.closeMenu(true);
            else if (slidingNavigation.isMenuClosed())
                slidingNavigation.openMenu(true);
        }
        else if (v.getId() == R.id.menuRow0)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(AccountFragment.newInstance(), FragmentAnimation.NONE_PUSH);
        }
        else if (v.getId() == R.id.menuRow1)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(VehicleListFragment.newInstance(VehicleListFragment.FROM_VEHICLES), FragmentAnimation.NONE_PUSH);
        }
        else if (v.getId() == R.id.menuRow2)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(VehicleListFragment.newInstance(VehicleListFragment.FROM_INCIDENCES), FragmentAnimation.NONE_PUSH);
        }
        else if (v.getId() == R.id.menuRow3)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(BeaconListFragment.newInstance(), FragmentAnimation.NONE_PUSH);

        }
        else if (v.getId() == R.id.menuRow4)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(EcommerceFragment.newInstance(), FragmentAnimation.NONE_PUSH);

        }
        else if (v.getId() == R.id.menuAdd)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(AddFragment.newInstance(), FragmentAnimation.NONE_PUSH);
        }
        else if (v.getId() == R.id.menuRowLanguage)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(SelectLanguageFragment.newInstance(), FragmentAnimation.PUSH);

        }
        else if (v.getId() == R.id.menuRowHelp)
        {
            slidingNavigation.closeMenu(true);
            slidingNavigation.setMenuLocked(true);
            addFragment(HelpFragment.newInstance(), FragmentAnimation.PUSH);

        }
    }

    private void checkBluetoothConnection()
    {
        boolean initDetection = Core.hasAnyVehicleWithBeacon();

        if (initDetection)
        {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                showBeaconAlertDisconnected();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled :)
                showBeaconAlertDisconnected();
            } else {
                // Bluetooth is enabled
            }
        }
    }

    private void showBeaconAlertDisconnected()
    {
        String title = getString(R.string.alert_bluetooth_title);
        String message = getString(R.string.alert_bluetooth_message);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.alert_bluetooth_btn_activate));
        options.add(getString(R.string.cancel));
        ArrayList<Integer> optionsColors = new ArrayList<>();
        optionsColors.add(Utils.getColor(this, R.color.black600));
        optionsColors.add(Utils.getColor(this, R.color.error));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null)
                {
                    int index = (int)view.getTag();
                    if (index == 0)
                    {
                        //activate
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intentOpenBluetoothSettings);
                    }
                    else if (index == 1)
                    {
                        //cancel
                    }
                }
            }
        };

        RelativeLayout layoutToShow = findViewById(R.id.layout_activity_main);
        INotification.shared(this).showOptionsNotification(layoutToShow, title, message, options, optionsColors, listener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event)
    {
        if (event.code == EventCode.USER_UPDATED)
        {
            printUserInfo();
        }
        else if (event.code == EventCode.HOME_MENU_UNLOCK)
        {
            slidingNavigation.setMenuLocked(false);
        }
        else if (event.code == EventCode.APP_DID_BECOME_ACTIVE)
        {
            checkBluetoothConnection();
        }
    }
}
