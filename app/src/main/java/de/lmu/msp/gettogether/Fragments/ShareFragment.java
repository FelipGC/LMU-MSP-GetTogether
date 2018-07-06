package de.lmu.msp.gettogether.Fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.Payload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.lmu.msp.gettogether.Connection.ConnectionManager;
import de.lmu.msp.gettogether.Connection.PayloadSender;
import de.lmu.msp.gettogether.DataBase.LocalDataBase;
import de.lmu.msp.gettogether.R;

import static de.lmu.msp.gettogether.Connection.ConnectionManager.getAppLogicActivity;

/**
 * Class for selecting data/files and sharing them.
 * Read @see <a https://developer.android.com/guide/topics/providers/document-provider>this</a>
 * to see how it works in detail
 */
public class ShareFragment extends Fragment {
    private static final String TAG = "ShareFragment";
    private PayloadSender payloadSender;
    private Uri uri;
    /**
     * Code id for reading
     */
    private static final int READ_REQUEST_CODE = 42;

    private static ConnectionManager cM;
    private boolean connected = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,name +"SERVICE DISCCONECTED");
            if(getAppLogicActivity() != null)
                getAppLogicActivity().serviceConnections.remove(this);
            connected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.ConnectionManagerBinder myBinder = (ConnectionManager.ConnectionManagerBinder) service;
            cM = myBinder.getService();
            connected = true;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        payloadSender = new PayloadSender();
        if(!connected) {
            Intent intent = new Intent(getAppLogicActivity(), ConnectionManager.class);
            getAppLogicActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getAppLogicActivity().serviceConnections.add(mServiceConnection);
        }
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
            uri = resultData.getData();
            Log.i(TAG, "Uri: " + uri.toString());

            displayConfirmationDialog(uri);
        }
        //Calling super is mandatory!
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /*
     **Method to compress the bitmaps as they are too large to send
     */
    private void compressImage(Bitmap bitmap) {
        //Get Bitmap from the uri and turn it into byte array to be used by the BitmapFactory
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Log.i(TAG, "First size is: " + bitmap.getByteCount());

        //Decode first to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        //Calculate the compression
        options.inSampleSize = calculateInSampleSize(options, 200, 200);
        //Compress the image
        options.inJustDecodeBounds = false;
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

        Log.i(TAG, "Second size is: " + compressedBitmap.getByteCount());
        saveImage(compressedBitmap, compressedBitmap.toString());
    }

    /*
     **Saves the compressed bitmap and generates a new uri to be sent to the endpoint
     */
    private void saveImage(Bitmap finalBitmap, String imageName) {

        String root = Environment.getExternalStorageDirectory().toString();
        File directory = new File(root);
        directory.mkdirs();
        String fileName = imageName+ ".jpg";
        File file = new File(directory, fileName);
        if (file.exists()) file.delete();
        Log.i("Saved", root + fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        uri = Uri.fromFile(file);

        try {
            sendDataToEndpoint(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Calculates the compression as not all images are the same size
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
        builder.setTitle(getString(R.string.confirm_send_title,filename));
        builder.setMessage(getString(R.string.confirm_send_body));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dataToPayload
                try {
                    final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                    new Thread(new Runnable() {
                        public void run() {
                            compressImage(bitmap);
                        }
                    }).start();
                } catch (IOException e) {
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
        Log.i(TAG,"START PAYLOAD SENDING");
        LocalDataBase.urisSent.add(uri);
        for (final String endpointId : cM.getEstablishedConnections().keySet()) {
            sendDataToEndpoint(endpointId,uri);
        }
        //Display Toast
        //Toast.makeText(getContext(),R.string.image_sent,Toast.LENGTH_SHORT).show();
    }

    public void sendDataToEndpoint(String endpointId,Uri uri) throws FileNotFoundException{
        Payload payload = dataToPayload(uri);
        // Mapping the ID of the file payload to the filename
        String payloadStoringName = payload.getId() + ":IMAGE_PIC:" + uri.getLastPathSegment() + "uri :" + uri;
        try {
            Log.i(TAG, "sendPayloadFile :"+ payloadStoringName +" + to: " + endpointId);
            payloadSender.sendPayloadFile(endpointId, payload, payloadStoringName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
