package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.PayloadSender;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.google.android.gms.nearby.connection.Payload;

import java.io.FileNotFoundException;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Class for selecting data/files and sharing them.
 * Read @see <a https://developer.android.com/guide/topics/providers/document-provider>this</a>
 * to see how it works in detail
 */
public class ShareFragment extends Fragment {
    private static final String TAG = "ShareFragment";
    private PayloadSender payloadSender;
    /**
     * Code id for reading
     */
    private static final int READ_REQUEST_CODE = 42;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        payloadSender = new PayloadSender();
        return view;
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     * (Minimum API is 19)
     */
    public void performFileSearch() {
        Log.i(TAG, "Performing file search");

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter what we want to search for (*/* == everything)
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
            displayConfirmationDialog(uri);
        }
        //Calling super is mandatory!
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Asks the user whether he really wishes to send this file/data
     *
     * @param uri The URI of the file/data we want to send
     */
    private void displayConfirmationDialog(final Uri uri) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Extract the filename from the URI
        String filename = uri.getLastPathSegment().toString();
        int divider = filename.lastIndexOf("/");
        filename = "\""+filename.substring(divider+1)+"\"";
        builder.setTitle(String.format("Send %s?",filename));
        builder.setMessage("Are you sure you want to send this file to all your viewers?");
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dataToPayload
                try {
                    sendDataToEndpoint(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    /**
     * Sends data from a uri to (all) endpoints
     *
     * @param uri
     */
    private void sendDataToEndpoint(Uri uri) throws FileNotFoundException {
        Payload payload = dataToPayload(uri);
        // Mapping the ID of the file payload to the filename
        String payloadStoringName = payload.getId() + ":IMAGE_PIC:" + uri.getLastPathSegment();
        payloadSender.sendPayloadFile(payload, payloadStoringName);
        //Display Toast
        Toast.makeText(getContext(),"File sent!",Toast.LENGTH_SHORT).show();
    }

    /**
     * Transforms data (pictures, pdfs, etc..) from a URI into a payload so we can send data between
     * different devices
     * See @see <a https://developers.google.com/nearby/connections/android/exchange-data>this</a> for
     * more information
     */
    private Payload dataToPayload(Uri uri) throws FileNotFoundException {
        // Open the ParcelFileDescriptor for this URI with read access.
        ParcelFileDescriptor file = getContext().getContentResolver().openFileDescriptor(uri, "r");
        return Payload.fromFile(file);
    }

}
