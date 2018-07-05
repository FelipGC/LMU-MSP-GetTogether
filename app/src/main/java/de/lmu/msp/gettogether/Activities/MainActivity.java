package de.lmu.msp.gettogether.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import de.lmu.msp.gettogether.Connection.ConnectionManager;
import de.lmu.msp.gettogether.DataBase.AppPreferences;
import de.lmu.msp.gettogether.DataBase.LocalDataBase;
import de.lmu.msp.gettogether.R;
import de.lmu.msp.gettogether.Users.Presenter;
import de.lmu.msp.gettogether.Users.Spectator;
import de.lmu.msp.gettogether.Users.User;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    private ImageButton presenter;
    private ImageButton spectator;
    private DrawerLayout mDrawerLayout;
    private ImageView userImage;
    private NavigationView navigationView;
    private AppPreferences preferences;

    @Override
    protected void onStart() {
        Log.i(TAG,"onStart()");
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), ConnectionManager.class);
        stopService(intent);
        preferences = AppPreferences.getInstance(this);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24px);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        userImage = navigationView.getHeaderView(0).findViewById(R.id.user_image_header);
        Bitmap bitmap = preferences.getUserImageBitmap();
        if(bitmap != null)
            userImage.setImageBitmap(bitmap);

        //Views
        presenter = findViewById(R.id.buttonPresenter);
        spectator = findViewById(R.id.buttonSpectator);

        //Animations
        Animation logoMoveAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_up_animation);
        presenter.startAnimation(logoMoveAnimation);
        spectator.startAnimation(logoMoveAnimation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_main);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets executed when the user selects "Settings" on the activity menu
     *
     * @param item the clicked Menuitem
     */
    public void onClickSettings(MenuItem item) {
        Log.i(TAG, "Settings option clicked");
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    /**
     * Gets executen when the user presses the endApplication button and ends the application
     *
     * @param item The clicked Menuitem
     */
    public void endApplication(MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit_app_title);
        builder.setMessage(R.string.exit_app_body);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "User closed the app!");
                Intent intent = new Intent(getApplicationContext(), ConnectionManager.class);
                stopService(intent);
                finishAffinity();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    /**
     * Gets executed when the user selects "About" on the activity menu
     *
     * @param item The clicked Menuitem
     */
    public void onClickAbout(MenuItem item) {

        Log.i(TAG, "About option clicked");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.about);
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
     * Gets executed when the user selects "Help" on the activity menu
     *
     * @param item The clicked Menuitem
     */
    public void onClickHelp(MenuItem item) {
        Log.i(TAG, "Help option clicked");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.help);
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

    /**
     * Gets executed if the user chooses to be a "Presenter"  by pressing
     * the corresponding button
     *
     * @param view The Button
     */
    public void presenterButtonClicked(View view) {
        Log.i(TAG, "User chose to be a PRESENTER." + LocalDataBase.getUserName());
        createSecondaryActivity(new Presenter());
    }

    /**
     * Gets executed if the user chooses to be a "Spectator"  by pressing
     * the corresponding button
     *
     * @param view The button
     */
    public void spectatorButtonClicked(View view) {
        Log.i(TAG, "User chose to be a SPECTATOR.");
        createSecondaryActivity(new Spectator());
    }

    /**
     * Creates a new (secondary) activity
     *
     * @param userRole Implementation of User with specific role
     */
    private void createSecondaryActivity(User userRole) {
        Intent intent = new Intent(this, AppLogicActivity.class);
        intent.putExtra("UserRole", userRole);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }
}
