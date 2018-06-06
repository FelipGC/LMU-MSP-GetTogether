package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Presenter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Spectator;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;

public class  MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    private ImageButton presenter;
    private ImageButton spectator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Views
        presenter = findViewById(R.id.buttonPresenter);
        spectator = findViewById(R.id.buttonSpectator);

        //Animations
        Animation logoMoveAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_up_animation);
        presenter.startAnimation(logoMoveAnimation);
        spectator.startAnimation(logoMoveAnimation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Gets executed when the user selects "Settings" on the activity menu
     * @param item the clicked Menuitem
     */
    public void onClickSettings(MenuItem item){
        Log.i(TAG,"Settings option clicked");
        Intent myIntent = new Intent(MainActivity.this,SettingsActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    /**
     * Gets executed when the user selects "About" on the activity menu
     * @param item The clicked Menuitem
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
     * Gets executed when the user selects "Help" on the activity menu
     * @param item The clicked Menuitem
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
     * @param userRole Implementation of User with specific role
     */
    private void createSecondaryActivity(User userRole){
        Intent intent = new Intent(this, AppLogicActivity.class);
        intent.putExtra("UserRole", userRole);
        startActivity(intent);
    }
}
