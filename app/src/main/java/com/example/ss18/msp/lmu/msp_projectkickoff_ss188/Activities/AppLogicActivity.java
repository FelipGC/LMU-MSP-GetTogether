package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.AbstractConnectionService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyAdvertiseService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyDiscoveryService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ChatFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.SelectPresenterFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ShareFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.InboxFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.TabPageAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.SelectParticipantsFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.LiveViewFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.PresentationFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.AppContext;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Voice.VoiceTransmission;

public class AppLogicActivity extends BaseActivity implements AppContext {
    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "SECONDARY_ACTIVITY";

    /**
     * The role of the user (Presenter/Spectator)
     */
    private static User userRole;

    private SelectPresenterFragment selectPresenterFragment;
    private ShareFragment shareFragment;
    private SelectParticipantsFragment selectParticipantsFragment;
    private InboxFragment inboxFragment;
    private ChatFragment chatFragment;
    private TabPageAdapter tabPageAdapter;
    private final static VoiceTransmission voiceTransmission = new VoiceTransmission();

    private boolean mBound = false;
    private NearbyAdvertiseService mAdvertiseService;
    private NearbyDiscoveryService mDiscoveryService;
    private AbstractConnectionService mService;
    private static AppLogicActivity appLogicActivity;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mAdvertiseConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NearbyAdvertiseService.NearbyAdvertiseBinder binder = (NearbyAdvertiseService.NearbyAdvertiseBinder) service;
            mService = mAdvertiseService = (NearbyAdvertiseService) binder.getService();
            mDiscoveryService = null;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mAdvertiseService = null;
            mDiscoveryService = null;
            mService = null;
        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mDiscoveryConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NearbyDiscoveryService.NearbyDiscoveryBinder binder = (NearbyDiscoveryService.NearbyDiscoveryBinder) service;
            mService = mDiscoveryService = (NearbyDiscoveryService) binder.getService();
            mAdvertiseService = null;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mAdvertiseService = null;
            mDiscoveryService = null;
            mService = null;
        }
    };

    public static AppLogicActivity getInstance() {
        return appLogicActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_app_logic);

        appLogicActivity = this;
        getSupportActionBar().setTitle(LocalDataBase.getUserName()); //TODO

        //Get object from intent
        setUserRole((User) getIntent().getSerializableExtra("UserRole"));
        Log.i(TAG, "Secondary activity created as: " + getUserRole().getRoleType());

        //Set up tabs
        tabPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        switch (getUserRole().getRoleType()) {

            case SPECTATOR:
                startDiscovering();
                //Add tabs for spectator
                tabPageAdapter.addFragment(selectPresenterFragment = new SelectPresenterFragment(), "Presenters");
                tabPageAdapter.addFragment(inboxFragment = new InboxFragment(), "Inbox");
                tabPageAdapter.addFragment(new LiveViewFragment(), "Live");
                tabPageAdapter.addFragment(chatFragment = new ChatFragment(), "Chat");
                selectPresenterFragment.reset();
                break;
            case PRESENTER:
                startAdvertising();
                //Add tabs for presenter
                tabPageAdapter.addFragment(selectParticipantsFragment = new SelectParticipantsFragment(), "Participants");
                tabPageAdapter.addFragment(new PresentationFragment(), getString(R.string.presentation_tabName));
                tabPageAdapter.addFragment(shareFragment = new ShareFragment(), "Share");
                tabPageAdapter.addFragment(chatFragment = new ChatFragment(), "Chat");
                selectParticipantsFragment.reset();
                break;
            default:
                Log.e(TAG, "Role type missing!");
                return;

        }
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabPageAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Updates the amount of participants on the GUI
     */
    public void updateParticipantsGUI(ConnectionEndpoint e, int newSize, int maxSize) {
        selectParticipantsFragment.updateParticipantsGUI(e, newSize, maxSize);
    }

    /**
     * Updates the amount of presenters on the GUI
     */
    public void updatePresentersGUI(ConnectionEndpoint endpoint) {
        if (selectPresenterFragment != null)
            selectPresenterFragment.updateDeviceList(endpoint);
    }


    /**
     * Starts a Nearby service with a given serviceID
     *
     * @param serviceID the ID of theservice to start
     */
    private void startNearbyService(String serviceID) {
        stopNearbyService();
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(serviceID);
        startService(serviceIntent);
    }

    /**
     * Stops all potential Nearby-Services
     */
    private void stopNearbyService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(".Connection.NearbyAdvertiseService");
        stopService(serviceIntent);
        serviceIntent.setAction(".Connection.NearbyDiscoveryService");
        stopService(serviceIntent);
    }

    /**
     * Calls startAdvertising() on the connectionManager
     */
    private void startAdvertising() {
        Toast.makeText(this, R.string.startDiscovering, Toast.LENGTH_LONG).show();
        startNearbyService(".Connection.NearbyAdvertiseService");
        //Bind to NearbyAdvertiseService
        Intent intent = new Intent(this, NearbyAdvertiseService.class);
        bindService(intent, mAdvertiseConnection, BIND_AUTO_CREATE);
    }

    /**
     * Calls startDiscovering() on the connectionManager
     */
    private void startDiscovering() {
        Toast.makeText(this, R.string.startAdvertising, Toast.LENGTH_LONG).show();
        startNearbyService(".Connection.NearbyDiscoveryService");
        //Bind to NearbyDiscoveryService
        Intent intent = new Intent(this, NearbyDiscoveryService.class);
        bindService(intent, mDiscoveryConnection, BIND_AUTO_CREATE);
    }

    //Getters & Setters
    public static User getUserRole() {
        return userRole;
    }

    public static void setUserRole(User userRole) {
        Log.i(TAG, "User changed his role to: " + userRole.getRoleType().toString());
        AppLogicActivity.userRole = userRole;
    }

    /**
     * Displays options to manage (allow/deny) file sharing with devices.
     * That is selecting devices you want to enable file sharing
     *
     * @param view
     */
    public void manageParticipants(View view) {
        selectParticipantsFragment.manageParticipants(view);
    }

    /**
     * Gets executed when a presentor presses to "select file" button inside the fragment_share
     */
    public void selectFileButtonClicked(View view) {
        if (shareFragment == null)
            return;
        shareFragment.performFileSearch();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy() -> terminating nearby connection");
        stopNearbyService();
        if (chatFragment != null) {
            chatFragment.clearContent();
        }
        super.onDestroy();
    }
    //Getters and Setters

    public InboxFragment getInboxFragment() {
        return inboxFragment;
    }

    public ChatFragment getChatFragment() {
        return chatFragment;
    }

    public SelectPresenterFragment getSelectPresenterFragment() {
        return selectPresenterFragment;
    }

    public SelectParticipantsFragment getSelectParticipantsFragment() {
        return selectParticipantsFragment;
    }

    public static VoiceTransmission getVoiceTransmission() {
        return voiceTransmission;
    }

    @Override
    public void displayShortMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public NearbyAdvertiseService getmAdvertiseService() {
        return mAdvertiseService;
    }

    public NearbyDiscoveryService getmDiscoveryService() {
        return mDiscoveryService;
    }

    public AbstractConnectionService getmService() {
        return mService;
    }

}
