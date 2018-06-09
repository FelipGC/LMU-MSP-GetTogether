package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsActivity extends BaseActivity {

    private static final String PREFS_NAME = "preferences_title_id";
    private static final String PREF_USER = "preferences_username";
    private static final String PREF_IMAGE = "preferences_image";
    private EditText enteredUsername;
    private ImageView userImage;

    private boolean firstStart = false;

    /**
     * Code id for reading
     */
    private static final int READ_REQUEST_CODE = 42;

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

        loadUserNamePreferences();
        loadImagePreferences(this);

        //Get the intent from MainActivity that someone selected the "Settings" option
        //on the activity menu
        Intent intent = getIntent();
        firstStart = intent.hasExtra("newUser") && intent.getBooleanExtra("newUser",false);
        if(firstStart){
            getSupportActionBar().setTitle(R.string.register_new);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSignUpButtonClicked();
                }
            });
        }else {
            signUpButton.setVisibility(View.GONE);
            getSupportActionBar().setTitle(R.string.settings_user);
            enteredUsername.setText(LocalDataBase.getUserName());
        }
    }

    @Override
    public void onBackPressed() {
        if(!LocalDataBase.getUserName().equals(enteredUsername.getText().toString())){
            if(!setUsername()){
                return;
            }
        }
        super.onBackPressed();
    }

    private void onSignUpButtonClicked(){
        if(setUsername()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     * (Minimum API is 19)
     */
    public void performFileSearch() {
        Log.i(TAG,"Performing file search");

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
                //Getting the Bitmap from Gallery
                Bitmap toEncode = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //Resize the image/bitmap
                //TODO: Maybe rather corp the image instead of resizing
                toEncode = Bitmap.createScaledBitmap (toEncode, 200,200,true);
                //Compress the file so that the JAVA Binder doesn't crash
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                toEncode.compress(Bitmap.CompressFormat.PNG, 100, out);
                Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                userImage.setImageBitmap(bitmap);
                LocalDataBase.setProfilePicture(bitmap);
                saveImagePreferences(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Calling super is mandatory!
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Gets called when the user clicks the plus button to choose a photo
     */
    public void onClickChangePhoto(View button){
        Log.i(TAG,"Change photo option clicked");
        performFileSearch();
    }

    /**
     * Saves the username to the preferences
     */
    private void saveUserNamePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit
        Log.i(TAG,"Save username: " + LocalDataBase.getUserName());
        editor.putString(PREF_USER, LocalDataBase.getUserName());
        editor.commit();
    }

    /**
     * Saves the user image to the preferences
     */
    private void saveImagePreferences(final SettingsActivity activity) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        // Edit and commit
        String bitmapString = LocalDataBase.getProfilePictureAsString();
        Log.i(TAG,"Save user image: " + bitmapString);
        editor.putString(PREF_IMAGE, bitmapString);
        editor.commit();
    }

    /**
     * Loads the username form the preferences
     */
    private void loadUserNamePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        // Set username if already existing
        if(!firstStart)
        {
            LocalDataBase.setUserName(settings.getString(PREF_USER,LocalDataBase.getUserName()));
            Log.i(TAG,"Load user name: " + LocalDataBase.getUserName());
        }
    }

    /**
     * Loads the username form the preferences
     */
    private static void loadImagePreferences(final SettingsActivity activity) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                        Context.MODE_PRIVATE);
                // Set username if already existing
                if (settings.contains(PREF_IMAGE))
                {
                    String stringImage = settings.getString(PREF_IMAGE, LocalDataBase.getProfilePictureAsString());
                    LocalDataBase.setProfilePicture(LocalDataBase.stringToBitmap(stringImage));
                    Log.i(TAG, "Load user image: " + LocalDataBase.getProfilePicture());
                }
                return LocalDataBase.getProfilePicture();
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                activity.userImage.setImageBitmap(bitmap);
                super.onPostExecute(bitmap);
            }
        }.execute();
    }

    /**
     * Saves the username, that the user picked
     */
    private boolean setUsername() {
        if (!enteredUsername.getText().toString().isEmpty()) {
            LocalDataBase.setUserName(enteredUsername.getText().toString());
            saveUserNamePreferences();
            if (!firstStart) {
                Toast.makeText(SettingsActivity.this, R.string.changedInfo,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.welcomeUser, LocalDataBase.getUserName()),
                        Toast.LENGTH_LONG).show();
            }
            return true;
        }
        Toast.makeText(SettingsActivity.this,
                R.string.username_empty,
                Toast.LENGTH_LONG).show();
        return false;
    }
}