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
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.IDiscoveryService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Adapters.PresenterAdapter;

import java.util.Arrays;
import java.util.HashSet;

public class SelectPresenterFragment extends Fragment {
    private static final String TAG = "SelectPresenter";
    private ListView availablePresenters;
    private ListView establishedPresenters;
    private Button pendingButton;
    private TextView joinedTitle;
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
        cM = AppLogicActivity.getConnectionManager();

        viewDevicesFound.addAll(Arrays.asList(
                availablePresenters = view.findViewById(R.id.presentersListView_available),
                establishedPresenters = view.findViewById(R.id.presentersListView_joined),
                availableTitle = view.findViewById(R.id.presentersListViewTitle_available),
                joinedTitle = view.findViewById(R.id.presentersListViewTitle_established),
                pendingButton = view.findViewById(R.id.presentersListViewTitle_pending)));
        viewNoDevices.add(view.findViewById(R.id.noDevicesFound));

        //Set adapters
        availablePresenters.setAdapter(new PresenterAdapter(getContext(),false));
        establishedPresenters.setAdapter(new PresenterAdapter(getContext(),true));

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

    /**
     * Removes an endPoint from the adapters (for example when he disconnects)
     */
    public void removeEndpointFromAdapters(ConnectionEndpoint connectionEndpoint){
        Log.i(TAG,"REMOVE ENDPOINT FROM ADAPTERS");
        ((PresenterAdapter) availablePresenters.getAdapter()).remove(connectionEndpoint);
        ((PresenterAdapter) establishedPresenters.getAdapter()).remove(connectionEndpoint);
        if(cM == null || cM.getPendingConnections().size() == 0)
            pendingButton.setVisibility(View.GONE);
        else pendingButton.setText(String.format("Pending Connection(s): %d", cM.getPendingConnections().size()));
    }

    /**
     * Updates the list view displaying all devices an advertiser can connect/
     * or has already connected to
     */
   /* public synchronized void updateDeviceList(ConnectionEndpoint endpoint) {
        Log.i(TAG, "updateDeviceList( "+endpoint+" )");
        //We found no device
        if (cM == null || cM.getDiscoveredEndpoints().size() == 0) {
            for (View view : viewDevicesFound)
                view.setVisibility(View.GONE);
            for (View viewNoDevice : viewNoDevices)
                viewNoDevice.setVisibility(View.VISIBLE);
        }//We found devices
        else{
            //Hide GUI we do not want
            for (View view : viewNoDevices)
                view.setVisibility(View.GONE);
            for (View view : viewDevicesFound)
                view.setVisibility(View.VISIBLE);
            //Update lists
            updateListViews(endpoint);
        }
    }*/

    public void updatePresenterLists(){

        IDiscoveryService discoveryService = ((AppLogicActivity)getActivity()).getDiscoveryService();

        boolean devicesFound = false;

        // Displaying available presenters
        PresenterAdapter availableAdapter = ((PresenterAdapter)availablePresenters.getAdapter());
        for(ConnectionEndpoint discoveredEndpoints : discoveryService.getDiscoveredEndpoints()){
            availablePresenters.setVisibility(View.VISIBLE);
            availableTitle.setVisibility(View.VISIBLE);
            devicesFound = true;
            if(!availableAdapter.contains(discoveredEndpoints.getId())){
                availableAdapter.add(discoveredEndpoints);
            }
        }

        // Displaying Pending button
        pendingButton.setVisibility(View.GONE);
        int pendingCount = 0;
        for(ConnectionEndpoint pendingEndpoints : discoveryService.getPendingEndpoints()){
            pendingCount++;
        }
        if(pendingCount>0){
            devicesFound = true;
            pendingButton.setVisibility(View.VISIBLE);
            pendingButton.setText(String.format("Pending Connection(s): %d", pendingCount));
        }

        if(devicesFound) {
            for (View viewNoDevice : viewNoDevices)
                viewNoDevice.setVisibility(View.GONE);
        }
    }

    /**
     * Removes and endpoint from all listviews but in our specified one, where the endpoint will
     * be added
     */
    private void updateListViews(ConnectionEndpoint endpoint) {
        ListView targetListView = null;
        if (cM.getEstablishedConnections().containsKey(endpoint.getId()))
            targetListView = establishedPresenters;
        else if (!cM.getPendingConnections().containsKey(endpoint.getId()))
            targetListView = availablePresenters;
        //Add or replace element form listView
        HashSet<ListView> listViews = new HashSet<>(Arrays.asList(establishedPresenters, availablePresenters,null));
        for (ListView listView : listViews) {
            PresenterAdapter presenterAdapter = null;
            if(listView != null)
                presenterAdapter = (PresenterAdapter) listView.getAdapter();
            //Rename is necessary
            final String displayName = endpoint.getName();
            //Update listView
            if (listView == targetListView) {
                //Add endpoint to list
                if(presenterAdapter != null) {
                    presenterAdapter.add(endpoint);
                    Log.i(TAG, "Added to list: " + displayName);
                }
            } else if (listView != null) {
                //Remove endpoint from list
                presenterAdapter.remove(endpoint);
                //Hide if empty
                if (presenterAdapter.getCount() == 0) {
                    listView.setVisibility(View.GONE);
                    if (listView == availablePresenters)
                        availableTitle.setVisibility(View.GONE);
                    else if (listView == establishedPresenters)
                        joinedTitle.setVisibility(View.GONE);
                }
            }
            if(cM.getPendingConnections().size() == 0)
                pendingButton.setVisibility(View.GONE);
            else pendingButton.setText(String.format("Pending Connection(s): %d", cM.getPendingConnections().size()));
        }
    }

    public void updateJoinedPresentersAvatar() {
        ((PresenterAdapter) establishedPresenters.getAdapter()).notifyDataSetChanged();
    }
}
