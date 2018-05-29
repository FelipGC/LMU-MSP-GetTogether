package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.AvailablePresenterFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ShareFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.InboxFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.TabPageAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ParticipantsFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.LiveViewFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class AppLogicActivity extends AppCompatActivity {
    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "SECONDARY_ACTIVITY";

    /**
     * A reference to the nearby connection manager object
     */
    private static ConnectionManager connectionManager;

    /**
     * The role of the user (Presenter/Spectator)
     */
    private static User userRole;

    private AvailablePresenterFragment availablePresenterFragment;
    private ShareFragment shareFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_logic_activity);
        //Get object from intent
        setUserRole((User) getIntent().getSerializableExtra("UserRole"));
        Log.i(TAG, "Secondary activity created as: " + getUserRole().getRoleType());
        //Connection
        connectionManager = ConnectionManager.getInstance(); //Singleton
        connectionManager.setUpConnectionsClient(this);
        connectionManager.setServiceId(getPackageName());
        //Set up tabs
        TabPageAdapter tabPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        switch (getUserRole().getRoleType()) {

            case SPECTATOR:
                startAdvertising();
                //Add tabs for spectator
                tabPageAdapter.addFragment(availablePresenterFragment = new AvailablePresenterFragment(), "Presenters");
                tabPageAdapter.addFragment(new InboxFragment(), "Inbox");
                tabPageAdapter.addFragment(new LiveViewFragment(), "Live View");
                break;
            case PRESENTER:
                startDiscovering();
                //Add tabs for presenter
                tabPageAdapter.addFragment(new ParticipantsFragment(), "Participants");
                tabPageAdapter.addFragment(shareFragment = new ShareFragment(), "Share");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    /**
     * Gets executed when the user selects "Settings" on the activity menu
     */
    public void onClickSettings(MenuItem item){
        Log.i(TAG,"Settings option clicked");
    }
    /**
     * Gets executed when the user selects "About" on the activity menu
     */
    public void onClickAbout(MenuItem item){

        Log.i(TAG,"About option clicked");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("About");
        dialog.setMessage(R.string.aboutTextCredits);
        dialog.setNeutralButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    /**
     * Updates the amount of participants on the GUI
     */
    public void updateParticipantsGUI(int newSize){
        TextView textView = findViewById(R.id.numberOfParticipants);
        textView.setText(newSize + "");
    }
    /**
     * Updates the amount of presenters on the GUI
     */
    public void updatePresentersGUI(){
        availablePresenterFragment.updateDeviceListView();
    }
    /**
     * Gets executed when the user selects "Help" on the activity menu
     */
    public void onClickHelp(MenuItem item){
        Log.i(TAG,"Help option clicked");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Help & Feedback");
        dialog.setMessage(R.string.help_feedback);
        dialog.setNeutralButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    //Advertising and Discovery
    /**
     * Calls startAdvertising() on the connectionManager
     */
    private void startAdvertising() {
        Toast.makeText(this, R.string.startAdvertising, Toast.LENGTH_LONG).show();
        connectionManager.startAdvertising();
    }

    /**
     * Calls stopAdvertising() on the connectionManager
     */
    private void stopAdvertising() {
        connectionManager.stopAdvertising();
    }
    /**
     * Calls startDiscovering() on the connectionManager
     */
    private void startDiscovering() {
        Toast.makeText(this, R.string.startDiscovering, Toast.LENGTH_LONG).show();
        connectionManager.startDiscovering();
    }
    /**
     * Calls stopDiscovering() on the connectionManager
     */
    private void stopDiscovering() {
        connectionManager.stopDiscovering();
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
     * @param view
     */
    public void manageParticipants(View view){
        Log.i(TAG,"Participants button clicked");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectDevices);
        final boolean[] devicesSelectedByDefault = null; //We may want to change that later
        final ConnectionEndpoint[] discoveredDevices = connectionManager.getDiscoveredEndpoints().values().toArray(new ConnectionEndpoint[0]);
        //We found no device
        if(discoveredDevices.length == 0){
            builder.setMessage(R.string.noDevicesFound);
            builder.setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.dismiss();
                }
            });
        }//We found devices
        else {
            final String[] deviceNicknames = new String[discoveredDevices.length];
            //Assign nicknames
            for (int i = 0; i < discoveredDevices.length; i++)
                deviceNicknames[i] = discoveredDevices[i].getName();

            DialogInterface.OnMultiChoiceClickListener dialogInterface = new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int device,
                                    boolean isChecked) {
                    if (isChecked) {
                        // If the user checked the item, add it to the selected items
                        connectionManager.getPendingConnections().put(discoveredDevices[device].getId(), discoveredDevices[device]);
                    } else if (connectionManager.getPendingConnections().containsKey(discoveredDevices[device].getId())) {
                        // Else, if the item is already in the array, remove it
                        connectionManager.getPendingConnections().remove(discoveredDevices[device].getId());
                    }
                }
            };

            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            builder.setMultiChoiceItems(deviceNicknames, devicesSelectedByDefault, dialogInterface);
            // Set the action buttons
            builder.setPositiveButton(R.string.selectAll, null);
            builder.setNegativeButton(R.string.deselectAll, null);
            builder.setNeutralButton(R.string.okay, null );
        }
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Select all
                for (int i = 0; i < discoveredDevices.length; i++)
                    dialog.getListView().setItemChecked(i, true);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Deselect all
                for (int i = 0; i < discoveredDevices.length; i++)
                    dialog.getListView().setItemChecked(i, false);
            }
        });
        //Connect to selected devices after dismissing the dialog!
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                connectionManager.requestConnectionForSelectedDevices();
            }
        });
    }

    /**
     * Gets executed when a presentor presses to "select file" button inside the share_fragment
     */
    public void selectFileButtonClicked(View view) {
        if(shareFragment == null)
            return;
        shareFragment.performFileSearch();
    }
    //Getters and Setters

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
