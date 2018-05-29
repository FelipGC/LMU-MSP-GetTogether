package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.util.List;

public class AvailablePresenterFragment extends Fragment {
    private static final String TAG = "AvailablePresenter";
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.available_presenters_fragment,container,false);
        return view;
    }

    /**
     * Updates the list view displaying all devices an advertiser can connect/
     * or has already connected to
     */
    public synchronized void updateDeviceListView() {
        Log.i(TAG,"updateDeviceListView()");
        final ConnectionEndpoint[] discoveredDevices = AppLogicActivity.getConnectionManager().
                getDiscoveredEndpoints().values().toArray(new ConnectionEndpoint[0]);
        //We found no device
        if (discoveredDevices.length == 0) {
            view.findViewById(R.id.presentersListView).setVisibility(View.GONE);
            view.findViewById(R.id.presentersListViewTitle).setVisibility(View.GONE);
            view.findViewById(R.id.noDevicesFound).setVisibility(View.VISIBLE);
        }//We found devices
        else {
            view.findViewById(R.id.presentersListView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.presentersListViewTitle).setVisibility(View.VISIBLE);
            view.findViewById(R.id.noDevicesFound).setVisibility(View.GONE);

            final String[] deviceNicknames = new String[discoveredDevices.length];
            ListView listView = (ListView) view.findViewById(R.id.presentersListView);
            //Assign nicknames
            for (int i = 0; i < discoveredDevices.length; i++) {
                deviceNicknames[i] = discoveredDevices[i].getName();
                if(AppLogicActivity.getConnectionManager().getEstablishedConnections().
                        containsKey(discoveredDevices[i].getId())){
                    CheckedTextView textview = (CheckedTextView) ((ListView) listView).getAdapter().getItem(i);
                    Log.i(TAG,listView.getItemAtPosition(i) + "");
                    textview.setChecked(true);
                }
            }
            ListAdapter listAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_multiple_choice,deviceNicknames);
            listView = view.findViewById(R.id.presentersListView);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckedTextView checkBox = (CheckedTextView) view;
                    checkBox.setChecked(!checkBox.isChecked());
                    if(checkBox.isChecked()) {
                        //Click and ticked
                        Toast.makeText(getContext(), String.format(String.format("Subscribed to: %s",
                                deviceNicknames[position])), Toast.LENGTH_SHORT).show();
                        AppLogicActivity.getConnectionManager().getPendingConnections()
                                .put(discoveredDevices[position].getId(),discoveredDevices[position]);
                        AppLogicActivity.getConnectionManager().requestConnection(discoveredDevices[position]);
                    }
                    else {
                        //Click and not ticked
                        Toast.makeText(getContext(), String.format(String.format("Unsubscribed from: %s",
                                deviceNicknames[position])), Toast.LENGTH_SHORT).show();
                        if(AppLogicActivity.getConnectionManager().getPendingConnections().containsKey(discoveredDevices[position].getId())) {
                            AppLogicActivity.getConnectionManager().acceptConnection(false, discoveredDevices[position]);
                            AppLogicActivity.getConnectionManager().getPendingConnections()
                                    .remove(discoveredDevices[position].getId());
                        }
                    }
                }
            });
        }
    }
}
