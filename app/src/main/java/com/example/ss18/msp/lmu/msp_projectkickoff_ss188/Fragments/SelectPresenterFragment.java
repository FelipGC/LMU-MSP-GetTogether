package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Adapters.PresenterAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.OldConnection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.IDiscoveryService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.util.Arrays;
import java.util.HashSet;

public class SelectPresenterFragment extends Fragment {
    private static final String TAG = "SelectPresenter";
    private ListView availablePresenters;
    private ListView connectedPresenters;
    private Button pendingButton;
    private TextView connectedTitle;
    private TextView availableTitle;
    private ConnectionManager cM;
    /**
     * Views to display when at least on endpoint is found
     */
    private HashSet<View> viewDevicesFound = new HashSet<>();
    /**
     * Views to display when no endpoint is found
     */
    private HashSet<View> viewNoDevices = new HashSet<>();

    public SelectPresenterFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available_presenters, container, false);

        viewDevicesFound.addAll(Arrays.asList(
                availablePresenters = view.findViewById(R.id.presentersListView_available),
                connectedPresenters = view.findViewById(R.id.presentersListView_joined),
                availableTitle = view.findViewById(R.id.presentersListViewTitle_available),
                connectedTitle = view.findViewById(R.id.presentersListViewTitle_established),
                pendingButton = view.findViewById(R.id.presentersListViewTitle_pending)));
        viewNoDevices.add(view.findViewById(R.id.noDevicesFound));

        //Set adapters
        availablePresenters.setAdapter(new PresenterAdapter(getContext(),false));
        connectedPresenters.setAdapter(new PresenterAdapter(getContext(),true));

        //Set up clickListeners for the individual items and lists
        //On Click: Displays list of pending connections as a dialog
        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "setOnClickListener() ");

                IDiscoveryService discoveryService = ((AppLogicActivity)getActivity()).getDiscoveryService();

                String pendingNames = "";
                for(ConnectionEndpoint pending : discoveryService.getPendingEndpoints()){
                    pendingNames += pending.getName() + "\n";
                }
                if(pendingNames.isEmpty()) return;
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.pending_devices);
                builder.setMessage(pendingNames);
                builder.create().show();
            }
        });

        //Hide GUI we do not want
        for (View view_it : viewNoDevices)
            view_it.setVisibility(View.VISIBLE);
        for (View view_it : viewDevicesFound)
            view_it.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePresenterLists();
    }

    /**
     * Removes an endPoint from the adapters (for example when he disconnects)
     */
    public void removeEndpointFromAdapters(ConnectionEndpoint connectionEndpoint){
        Log.i(TAG,"REMOVE ENDPOINT FROM ADAPTERS");
        ((PresenterAdapter) availablePresenters.getAdapter()).remove(connectionEndpoint);
        ((PresenterAdapter) connectedPresenters.getAdapter()).remove(connectionEndpoint);
        if(cM == null || cM.getPendingConnections().size() == 0)
            pendingButton.setVisibility(View.GONE);
        else pendingButton.setText(String.format("Pending Connection(s): %d", cM.getPendingConnections().size()));
    }

    public void updatePresenterLists(){
        IDiscoveryService discoveryService = ((AppLogicActivity)getActivity()).getDiscoveryService();

        boolean devicesFound = false;

        if(discoveryService!=null) {
            // Displaying available presenters
            PresenterAdapter availableAdapter = ((PresenterAdapter) availablePresenters.getAdapter());
            availableAdapter.removeAll();
            availablePresenters.setVisibility(View.GONE);
            availableTitle.setVisibility(View.GONE);
            for (ConnectionEndpoint de : discoveryService.getDiscoveredEndpoints()) {
                if (discoveryService.getConnectedEndpoints().contains(de)) {
                    continue;
                }
                availablePresenters.setVisibility(View.VISIBLE);
                availableTitle.setVisibility(View.VISIBLE);
                devicesFound = true;
                availableAdapter.add(de);
            }

            // Displaying connected presenters
            PresenterAdapter connectedAdapter = ((PresenterAdapter) connectedPresenters.getAdapter());
            connectedAdapter.removeAll();
            connectedPresenters.setVisibility(View.GONE);
            connectedTitle.setVisibility(View.GONE);
            for (ConnectionEndpoint ce : discoveryService.getConnectedEndpoints()) {
                connectedTitle.setVisibility(View.VISIBLE);
                connectedPresenters.setVisibility(View.VISIBLE);
                devicesFound = true;
                connectedAdapter.add(ce);
            }

            updatePendingButton();
        }

        // Display "No Devices"-Views when no devices found
        for (View viewNoDevice : viewNoDevices)
            viewNoDevice.setVisibility(devicesFound ? View.GONE : View.VISIBLE);
    }

    public void updatePendingButton(){
        IDiscoveryService discoveryService = ((AppLogicActivity)getActivity()).getDiscoveryService();
        // Displaying Pending button
        pendingButton.setVisibility(View.GONE);
        int pendingCount = discoveryService.getPendingEndpoints().size();
        if(pendingCount>0){
            pendingButton.setVisibility(View.VISIBLE);
            pendingButton.setText(String.format("Pending Connection(s): %d", pendingCount));
        }
    }

    public void updateJoinedPresentersAvatar() {
        ((PresenterAdapter) connectedPresenters.getAdapter()).notifyDataSetChanged();
    }
}
