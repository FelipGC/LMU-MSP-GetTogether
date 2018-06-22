package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.AppPreferences;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.RandomNameGenerator;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class SettingsActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {

    private AppPreferences preferences;

    private EditText enteredUsername;
    private ImageView userImage;

    private boolean firstStart = false;

    /**
     * Code id for reading
     */
    private static final int READ_REQUEST_CODE = 42;
    private static final int CAMERA_REQUEST_CODE = 1;

    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "SETTINGS_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_settings);

        Button signUpButton = (Button) findViewById(R.id.btn_signup);
        TextView settingsText = (TextView) findViewById(R.id.settings_text);
        userImage = (ImageView) findViewById(R.id.user_image);
        enteredUsername = (EditText) findViewById(R.id.enter_username);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpButtonClicked();
            }
        });

        preferences = AppPreferences.getInstance(this);

        //Get the intent from MainActivity that someone selected the "Settings" option
        //on the activity menu
        Intent intent = getIntent();
        firstStart = intent.hasExtra("newUser") && intent.getBooleanExtra("newUser", false);
        if (firstStart) {
            getSupportActionBar().setTitle(R.string.register_new);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        } else {
            settingsText.setVisibility(View.GONE);
            signUpButton.setText("Speichern");
            getSupportActionBar().setTitle(R.string.settings_user);
            enteredUsername.setText(preferences.getUsername());
            userImage.setImageURI(preferences.getUserImage());
        }
    }

    @Override
    public void onBackPressed() {
        if (!firstStart && !preferences.getUsername().equals(enteredUsername.getText().toString())) {
            if (!saveUsername()) {
                generateRandomName();
                return;
            }
        }
        super.onBackPressed();
    }

    private void onSignUpButtonClicked() {
        if (saveUsername()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            generateRandomName();
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     * (Minimum API is 19)
     */
    public void performFileSearch() {
        Log.i(TAG, "Performing file search");

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Log.i(TAG, "Received onActivityResult");

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null) {
            Uri uri = resultData.getData();
            Log.i(TAG, "Uri: " + uri.toString());

            try {
                //Get Bitmap from the uri and turn it into byte array to be used by the BitmapFactory
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //Compress the image
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                options.inJustDecodeBounds = false;
                Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

                userImage.setImageBitmap(compressedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //TODO: Resize the image (300,300) and save it somewhere and overwrite uri = new file!!!!!!!

            //preferences.setUserImage(uri.toString());
            //setImage();
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null) {
            Log.i(TAG, "Image taken.");

            Bitmap image = (Bitmap) resultData.getExtras().get("data");
            userImage.setImageBitmap(image);
        }
        //Calling super is mandatory!
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Gets called when the user clicks the plus button to choose a photo
     */
    public void onClickChangePhoto(View button) {
        Log.i(TAG, "Change photo option clicked");
        PopupMenu popup = new PopupMenu(this, button);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_camera);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_picture:
                performFileSearch();
                return true;
            case R.id.take_picture:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                return true;
            default:
                return false;
        }
    }

    private void setImage() {
        Uri uri = preferences.getUserImage();
        Log.i(TAG, "Load user image: " + uri.toString());
        //Add default profile picture in case it is null
        if (uri == null) {
            Log.i(TAG, "Add default image in case the user didn't choose one.");
            userImage.setImageResource(R.drawable.user_image);
        }
        userImage.setImageURI(uri);
        preferences.setUserImage(uri.toString());
    }


    /**
     * Saves the username, that the user picked
     */
    private boolean saveUsername() {
        if (!enteredUsername.getText().toString().isEmpty()) {
            boolean r = preferences.setUsername(enteredUsername.getText().toString());
           if (!firstStart) {
                Toast.makeText(SettingsActivity.this, R.string.changedInfo,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.welcomeUser, LocalDataBase.getUserName()),
                        Toast.LENGTH_LONG).show();
            }
           // return true;
            return r;
        }
        return false;
    }
    private void displayRandomName(){

        final String username = RandomNameGenerator.generate(getApplicationContext());
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wie wäre es mit...?");
        builder.setMessage(username);
        builder.setNegativeButton("Generieren", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayRandomName();
            }
        });
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enteredUsername.setText(username);
                onSignUpButtonClicked();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(SettingsActivity.this,
                        R.string.username_empty,
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.create().show();
    }
    /**
     * Displays anoption to generate a random name
     */
    private void generateRandomName(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.no_name_entered);
        builder.setMessage(R.string.generate_random_name);
        builder.setNegativeButton("Generieren", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayRandomName();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(SettingsActivity.this,
                        R.string.username_empty,
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.create().show();
    }
}