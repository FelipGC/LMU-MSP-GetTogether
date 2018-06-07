package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import java.io.IOException;

public class SettingsActivity extends BaseActivity {

    private static final String PREFS_NAME = "preferences_title";
    private static final String PREF_USER = "preferences_username";
    private static final String PREF_IMAGE = "preferences_image";
    private boolean userNameAlreadyEntered = false;
    private EditText enteredUsername;
    private Button signUpButton;
    private TextView settingsText;
    private ImageView userImage;

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
        getSupportActionBar().setTitle(R.string.settings_user);

        //Get the intent from MainActivity that someone selected the "Settings" option
        //on the activity menu
        Intent intent = getIntent();
        signUpButton = (Button) findViewById(R.id.sign_up);
        settingsText = (TextView) findViewById(R.id.settings_text);
        userImage = (ImageView) findViewById(R.id.user_image);

        enteredUsername = (EditText) findViewById(R.id.enter_username);
        loadUserNamePreferences();
        loadImagePreferences(this);

        if (userNameAlreadyEntered) {
            signUpButton.setText("Save");
            settingsText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     * (Minimum API is 19)
     */
    public void performFileSearch() {
        Log.i(TAG,"Performing file search");

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter what we want to search for (*/* == everything)
        //Only select images
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Log.i(TAG, "Received onActivityResult");

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getUserName().
            Uri uri = resultData.getData();
            Log.i(TAG, "Uri: " + uri.toString());
            //dataToPayload
            try {
                //Getting the Bitmap from Gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //Resize the image/bitmap
                //TODO: Maybe rather corp the image instead of resizing
                bitmap = Bitmap.createScaledBitmap (bitmap, 200,200,true);
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
     * Gets called when the user clicks the "Save" button
     */
    public void onClickSaveChanges(View button){
        Log.i(TAG,"Save button clicked");
        setUsername();
        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(intent);

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
    private static void saveImagePreferences(final SettingsActivity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();

                // Edit and commit
                String bitmapString = LocalDataBase.getProfilePictureAsString();
                Log.i(TAG,"Save user image: " + bitmapString);
                editor.putString(PREF_IMAGE, bitmapString);
                editor.commit();
                return null;
            }
        }.execute();

    }

    /**
     * Loads the username form the preferences
     */
    private void loadUserNamePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        // Set username if already existing
        if(userNameAlreadyEntered = settings.contains(PREF_USER))
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
                    LocalDataBase.setProfilePicture(LocalDataBase.getProfilePictureAsBitmap(stringImage));
                    Log.i(TAG, "Load user image: " + LocalDataBase.getProfilePicture());
                }
                return LocalDataBase.getProfilePicture();
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                activity.getUserImage().setImageBitmap(bitmap);
                super.onPostExecute(bitmap);
            }
        }.execute();
    }

    /**
     * Saves the username, that the user picked
     */
    private void setUsername() {

        if (!enteredUsername.getText().toString().isEmpty()) {
            LocalDataBase.setUserName(enteredUsername.getText().toString());
            saveUserNamePreferences();
            if (userNameAlreadyEntered) {
                Toast.makeText(SettingsActivity.this, R.string.changedInfo,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.welcomeUser, LocalDataBase.getUserName()),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public ImageView getUserImage() {
        return userImage;
    }
}