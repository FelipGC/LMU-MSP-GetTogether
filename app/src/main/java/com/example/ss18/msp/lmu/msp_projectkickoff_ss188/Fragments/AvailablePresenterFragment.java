package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class AvailablePresenterFragment extends Fragment {
    private static final String TAG = "AvailablePresenterFragment";
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.available_presenters_fragment,container,false);
        updateDeviceListView();
        return view;
    }

    /**
     * Updates the list view displaying all devices an advertiser can connect/
     * or has already connected to
     */
    public void updateDeviceListView() {
        final ConnectionEndpoint[] discoveredDevices = AppLogicActivity.getConnectionManager().
                getDiscoveredEndpoints().values().toArray(new ConnectionEndpoint[0]);
        //We found no device
        if (discoveredDevices.length == 0) {
            view.findViewById(R.id.presentersListView).setVisibility(View.GONE);
            view.findViewById(R.id.presentersListViewTitle).setVisibility(View.GONE);
            view.findViewById(R.id.noDevicesFound).setVisibility(View.VISIBLE);
        }//We found devices
        else {
            final String[] deviceNicknames = new String[discoveredDevices.length];
            //Assign nicknames
            for (int i = 0; i < discoveredDevices.length; i++)
                deviceNicknames[i] = discoveredDevices[i].getName();
            ListAdapter listAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_checked,deviceNicknames);
            ListView listView = view.findViewById(R.id.presentersListView);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckedTextView textview = (CheckedTextView) view;
                    textview.setChecked(!textview.isChecked());
                    if(textview.isChecked()) {
                        Toast.makeText(getContext(), String.format(String.format("Subscribed to: %s",
                                deviceNicknames[position])), Toast.LENGTH_SHORT).show();
                        AppLogicActivity.getConnectionManager().acceptConnection(true,discoveredDevices[position]);
                    }
                    else
                        Toast.makeText(getContext(),String.format( String.format("Unsubscribed from: %s",
                                deviceNicknames[position])),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
