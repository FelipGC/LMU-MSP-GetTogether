package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.MainActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.util.Arrays;
import java.util.HashSet;

import static junit.runner.BaseTestRunner.savePreferences;

public class AvailablePresenterFragment extends Fragment {
    private static final String TAG = "AvailablePresenter";
    private ListView availablePresenters;
    private ListView pendingPresenters;
    private ListView establishedPresenters;
    private TextView pendingTitle;
    private TextView joinedTitle;
    private TextView availableTitle;
    private ConnectionManager cM;
    /**
     * Views to display when at least on endpoint is found
     */
    private HashSet<View> viewDevicesFound;
    /**
     * Views to display when no endpoint is found
     */
    private HashSet<View> viewNoDevices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.available_presenters_fragment, container, false);
        cM = AppLogicActivity.getConnectionManager();

        viewDevicesFound.addAll(Arrays.asList(
                availablePresenters = view.findViewById(R.id.presentersListView),
                pendingPresenters = view.findViewById(R.id.presentersListView),
                establishedPresenters = view.findViewById(R.id.presentersListView),
                availableTitle = view.findViewById(R.id.presentersListView_available),
                joinedTitle = view.findViewById(R.id.presentersListView_joined),
                pendingTitle = view.findViewById(R.id.presentersListView_pending)));
        viewNoDevices.add(view.findViewById(R.id.noDevicesFound));

        //Set adapters
        availablePresenters.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_multiple_choice));
        pendingPresenters.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1));
        establishedPresenters.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1));

        //Set up clickListeners for the individual items and lists
        setUpItemListeners();

        return view;
    }

    /**
     * Defines the OnTimeClick listeners for the (3) listViews
     */
    private void setUpItemListeners() {
        //OnClick: Add to pending list
        availablePresenters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkBox = (CheckedTextView) view;
                checkBox.setChecked(!checkBox.isChecked());
                if (checkBox.isChecked()) {
                    //Click and ticked
                    ConnectionEndpoint endpoint = cM.getDiscoveredEndpoints().get(availablePresenters.getAdapter().getItem(position));
                    Toast.makeText(getContext(), String.format(String.format("Subscribed to: %s",
                            endpoint.getName())), Toast.LENGTH_SHORT).show();
                    cM.getPendingConnections().put(endpoint.getId(), endpoint);
                    cM.requestConnection(endpoint);
                    updateDeviceList(endpoint);
                }
            }
        });
        //On Click: Abort connection request
        pendingPresenters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Click and ticked
                ConnectionEndpoint endpoint = cM.getDiscoveredEndpoints().get(availablePresenters.getAdapter().getItem(position));
                Toast.makeText(getContext(), String.format(String.format("Abort Connection request to: %s",
                        endpoint.getName())), Toast.LENGTH_SHORT).show();
                cM.getPendingConnections().remove(endpoint.getId());
                //Disconnect from endpoint
                cM.acceptConnection(false, endpoint);
                cM.disconnectFromEndpoint(endpoint.getId());
                updateDeviceList(endpoint);
            }
        });
        //On Click: Disconnect from endpoint (must be a long click)
        pendingPresenters.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ConnectionEndpoint endpoint = cM.getDiscoveredEndpoints().get(availablePresenters.getAdapter().getItem(position));
                //Display dialog
                displayRemovePresenterDialog(endpoint);
                return true; //click redeemed, cant be used for further actions
            }
        });
    }

    /**
     * Displays a dialog after for asking the user if he really wants to unsubscribe and
     * disconnect from a presenter (endpoint).
     */
    private void displayRemovePresenterDialog(final ConnectionEndpoint connectionEndpoint) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.leave_presentation);
        dialog.setMessage(String.format(String.valueOf(R.string.sure_to_leave_group), connectionEndpoint.getName()));
        dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Click and ticked
                Toast.makeText(getContext(), String.format(String.format("Canceled unsubscribing from: %s",
                        connectionEndpoint.getName())), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Click and ticked
                Toast.makeText(getContext(), String.format(String.format("Unsubscribed from: %s",
                        connectionEndpoint.getName())), Toast.LENGTH_SHORT).show();
                //Disconnect from endpoint
                cM.disconnectFromEndpoint(connectionEndpoint.getId());
                cM.getEstablishedConnections().remove(connectionEndpoint.getId());
                updateDeviceList(connectionEndpoint);
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    /**
     * Updates the list view displaying all devices an advertiser can connect/
     * or has already connected to
     */
    public synchronized void updateDeviceList(ConnectionEndpoint endpoint) {
        Log.i(TAG, "updateDeviceList()");
        //We found no device
        if (cM.getDiscoveredEndpoints().size() == 0) {
            for (View view : viewDevicesFound)
                view.setVisibility(View.GONE);
            for (View viewNoDevice : viewNoDevices)
                viewNoDevice.setVisibility(View.VISIBLE);
        }//We found devices
        else {
            //Update lists
            updateListViews(endpoint);
        }
    }

    /**
     * Removes and endpoint from all listviews but in our specified one, where the endpoint will
     * be added
     */
    private void updateListViews(ConnectionEndpoint endpoint) {
        ListView targetListView;
        if (cM.getEstablishedConnections().containsKey(endpoint.getId()))
            targetListView = establishedPresenters;
        else if (cM.getPendingConnections().containsKey(endpoint.getId()))
            targetListView = pendingPresenters;
        else
            targetListView = availablePresenters;
        //Hide GUI we do not want
        for (View view : viewDevicesFound)
            view.setVisibility(View.GONE);
        for (View view : viewDevicesFound)
            view.setVisibility(View.VISIBLE);
        //Add or remove element form listView
        HashSet<ListView> listViews = new HashSet<>(Arrays.asList(establishedPresenters, availablePresenters, pendingPresenters));
        for (ListView listView : listViews) {
            final String displayName = String.format("%s : ID=%s", endpoint.getName(), endpoint.getId());
            ArrayAdapter<String> listAdapter = (ArrayAdapter<String>) listView.getAdapter();
            if (listView == targetListView) {
                //Add endpoint to list
                listAdapter.add(displayName);
                Log.i(TAG, "Added to list: " + displayName);
            } else {
                //Remove endpoint from list
                //TODO: What happens when two devices have the same name, since name != id. So we need to mention the id, which looks ugly
                listAdapter.remove(displayName);
                if (listAdapter.getCount() == 0) {
                    listView.setVisibility(View.GONE);
                    if (listView == availablePresenters)
                        availableTitle.setVisibility(View.GONE);
                    else if (listView == establishedPresenters)
                        joinedTitle.setVisibility(View.GONE);
                    else pendingPresenters.setVisibility(View.GONE);
                }
            }
        }
    }
}
