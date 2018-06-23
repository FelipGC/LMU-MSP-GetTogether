package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyAdvertiseService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Adapters.ViewerAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.MessageFactory;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.ServiceBinder;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Voice.VoiceTransmission;

import java.util.Objects;

import static android.content.Context.BIND_AUTO_CREATE;

public class SelectParticipantsFragment extends Fragment implements MessageFactory,ServiceBinder {
    private static final String TAG = "SelectParticipants";
    private View mainView;
    private ViewerAdapter viewerAdapter;
    private VoiceTransmission voiceTransmission;
    private ProgressBar progressBar;
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_participants,container,false);
        ListView listView = mainView.findViewById(R.id.viewerList);
        viewerAdapter = new ViewerAdapter(getContext());
        progressBar = mainView.findViewById(R.id.progressBar);
        if(viewerAdapter == null)
            viewerAdapter = new ViewerAdapter(getContext());
        else if(viewerAdapter.getCount() > 0)
            progressBar.setVisibility(View.GONE);
        listView.setAdapter(viewerAdapter);
        bindToService();
        updateParticipantsGUI(null,mService.getConnectedEndpointsSize(),
                mService.getPendingEndpointsSize());
        //Define pokeItem
        BottomNavigationItemView pokeItem = mainView.findViewById(R.id.vibrieren);
        pokeItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    startPoking();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    endPoking();
                }
                return true;
            }
        });
        //Define VoiceChatButton
        BottomNavigationItemView voiceChatItem = mainView.findViewById(R.id.voiceChat);
        voiceChatItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecordingVoice();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopRecordingVoice();
                }
                return true;
            }
        });
        voiceTransmission = AppLogicActivity.getVoiceTransmission();

        BottomNavigationItemView gpsButton = mainView.findViewById(R.id.gps);
        gpsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("GPS Entfernungen:");
                    String[] list = new String[mService.getConnectedEndpointsSize()];
                    int index = 0;
                    for (ConnectionEndpoint e : mService.getConnectedEndpoints()) {
                        list[index] = e.getName() + ": " + e.getLastKnownDistance();
                    }
                    builder.setItems(list,null);
                    builder.create();
                    builder.show();
                }
                return true;
            }
        });

        return mainView;
    }
    /**
     * Updates the amount of participants on the GUI
     */
    public void updateParticipantsGUI(ConnectionEndpoint e,int newSize, int maxSize){
        TextView textView = mainView.findViewById(R.id.numberOfParticipants);
        if(maxSize == 0) {
            textView.setText(R.string.leer);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            textView.setText(newSize + "|" + maxSize);
            progressBar.setVisibility(View.GONE);
        }
        if(newSize == 0){
            progressBar.setVisibility(View.VISIBLE);
        }
        //Update listView
        if(e == null)
            return;
        Log.i(TAG,"UpdateParticipantsGUI ID: " + e.getId());
        for(ConnectionEndpoint endpoint : mService.getConnectedEndpoints()){
            if(endpoint.getId().equals(e.getId())){
                viewerAdapter.add(e);
                viewerAdapter.notifyDataSetChanged();
                return;
            }
        }
        viewerAdapter.remove(e);
        viewerAdapter.notifyDataSetChanged();
        return;
    }

    /**
     * Displays options to manage (allow/deny) file sharing with devices.
     * That is selecting devices you want to enable file sharing
     * @param view
     */
    public void manageParticipants(View view){
        Log.i(TAG,"Participants button clicked");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.selectDevices);
        final ConnectionEndpoint[] discoveredDevices = new ConnectionEndpoint[mService.getConnectedEndpointsSize()];
        int index = 0;
        for(ConnectionEndpoint endpoint : mService.getConnectedEndpoints()){
            discoveredDevices[index++] = endpoint;
        }
        final boolean[] selectedDevices = new boolean[discoveredDevices.length]; //Default to be true(selected)

        //We found no device
        if(discoveredDevices.length == 0){
            builder.setMessage(R.string.noDevicesFound);
            builder.setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }//We found devices
        else {
            final String[] deviceNicknames = new String[discoveredDevices.length];
            //Assign nicknames
            for (int i = 0; i < discoveredDevices.length; i++) {
                deviceNicknames[i] = discoveredDevices[i].getName();
                if(mService.isConnected(discoveredDevices[i].getId()))
                    selectedDevices[i] = true;
            }

            DialogInterface.OnMultiChoiceClickListener dialogInterface = new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int device,
                                    boolean isChecked) {
                    Log.i(TAG,"Device checked: " + isChecked + " | " + discoveredDevices[device].getName());
                    selectedDevices[device] = isChecked;
                }
            };

            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            builder.setMultiChoiceItems(deviceNicknames, selectedDevices, dialogInterface);
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
                for (int i = 0; i < discoveredDevices.length; i++) {
                    dialog.getListView().setItemChecked(i, true);
                    selectedDevices[i] = true;
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Deselect all
                for (int i = 0; i < discoveredDevices.length; i++) {
                    dialog.getListView().setItemChecked(i, false);
                    selectedDevices[i] = false;
                }
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG,"Presenter window onDismiss()");
                for (int device = 0; device < selectedDevices.length; device++) {
                    ConnectionEndpoint endpoint = discoveredDevices[device];
                    boolean isChecked = selectedDevices[device];
                    boolean newEndpoint = !mService.isConnected(endpoint.getId())
                            && !mService.isPending(endpoint.getId());
                    if (isChecked && newEndpoint) {
                            Log.i(TAG,"Accepting connection for " + endpoint.getName());
                        // If the user checked the item, add it to the selected items, if not already connected
                        mService.acceptRequest(endpoint.getId());
                    } else if(!isChecked && !newEndpoint)
                        mService.disconnectFromUser(endpoint.getId());
                }
            }
        });
    }

    /**
     * Starts sending voice
     */
    private void startRecordingVoice(){
        //TODO: Display something
        voiceTransmission.startRecordingVoice();
        Toast.makeText(getContext(), "Zeichnet auf...", Toast.LENGTH_SHORT).show();

    }

    /**
     * Ends recording voice
     */
    private void stopRecordingVoice(){
        //TODO: Display something
        voiceTransmission.stopRecordingVoice();
        Toast.makeText(getContext(), "Aufzeichnung beendet.", Toast.LENGTH_SHORT).show();

    }
    /**
     * Sends vibration message to viewers
     */
    private void startPoking(){
        fabricateMessage("S");
    }

    /**
     * Sends STOP vibration message to viewers
     */
    private void endPoking(){
        fabricateMessage("E");
    }
    public void updateParticipantsAvatar() {
        viewerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        endPoking();
        super.onDestroy();
    }

    public void reset() {
        viewerAdapter = null;
    }

    @Override
    public String fabricateMessage(String... message) {
        Log.i(TAG, "sendPokeMessage()");
        // Adding the POKE_S tag to identify start vibration messages on receive.
        String fabricatedMessage = "POKE:" + message;
        // Send the name of the payload/file as a bytes payload first!
        return  fabricatedMessage;
    }

    private NearbyAdvertiseService mService;

    @Override
    public void transferFabricatedMessage(String message) {
        String fabricatedMessage = fabricateMessage(message);
        mService.broadcastMessage(fabricatedMessage);
    }
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NearbyAdvertiseService.NearbyAdvertiseBinder binder = (NearbyAdvertiseService.NearbyAdvertiseBinder) service;
            mService = (NearbyAdvertiseService) binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };
    @Override
    public void bindToService() {
        //Bind toService
        Intent intent = new Intent(getContext(), NearbyAdvertiseService.class);
        Objects.requireNonNull(getContext()).bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }
}
