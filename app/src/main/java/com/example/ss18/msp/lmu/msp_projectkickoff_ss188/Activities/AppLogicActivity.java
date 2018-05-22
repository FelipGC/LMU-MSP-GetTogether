package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ShareFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.InboxFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.TabPageAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ParticipantsFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.LiveViewFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;


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
                tabPageAdapter.addFragment(new InboxFragment(), "Inbox");
                tabPageAdapter.addFragment(new LiveViewFragment(), "Live View");
                break;
            case PRESENTER:
                startDiscovering();
                //Add tabs for presenter
                tabPageAdapter.addFragment(new ParticipantsFragment(), "Participants");
                tabPageAdapter.addFragment(new ShareFragment(), "Share");
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
        textView.setText(newSize);
    }
    /**
     * Gets executed when the user selects "Help" on the activity menu
     */
    public void onClickHelp(MenuItem item){
        Log.i(TAG,"Help option clicked");
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

}
