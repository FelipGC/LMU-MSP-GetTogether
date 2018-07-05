package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.util.ArrayList;

/**
 * Adapter for storing/handling (available) presenters for the "SelectPresenterFragment"
 */
public class ViewerAdapter extends BaseAdapter {

    private final String TAG = "PresenterAdapter";
    private final ArrayList<ConnectionEndpoint> endpointList = new ArrayList<ConnectionEndpoint>();
    private Context context;

    public ViewerAdapter(Context context) {
        this.context = context;
    }

    public boolean contains(String id) {
        for (ConnectionEndpoint e : endpointList) {
            if (e.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void add(ConnectionEndpoint endpoint) {
        //Add the new endpoint
        this.endpointList.add(endpoint);
        Log.i(TAG, "Added to PresenterAdapter: " + endpoint.getName() + " with id: " + endpoint.getId());
        notifyDataSetChanged(); // to render the list we need to notify
    }

    /**
     * Removes an ConnectionEndpoint from the list if it exists
     *
     * @param endpoint The endpoint to remove
     */
    public void remove(ConnectionEndpoint endpoint) {
        Log.i(TAG, "Remove from PresenterAdapter: " + endpoint.getName() + " with id: " + endpoint.getId());
        for (ConnectionEndpoint e : endpointList) {
            if (e.getId().equals(endpoint.getId())) {
                this.endpointList.remove(e);
                break;
            }
        }
        notifyDataSetChanged(); // to render the list we need to notify
    }

    @Override
    public int getCount() {
        return endpointList.size();
    }

    @Override
    public Object getItem(int position) {
        return endpointList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates a presenter entry
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView() for " + position);
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final ConnectionEndpoint connectionEndpoint = endpointList.get(position);
        convertView = messageInflater.inflate(R.layout.list_item_presenter, null);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        Switch s = convertView.findViewById(R.id.switch1);
        s.setVisibility(View.INVISIBLE);
        ImageView picture = (ImageView) convertView.findViewById(R.id.avatar);
        name.setText(connectionEndpoint.getName());
        picture.setImageURI(connectionEndpoint.getProfilePicture());
        convertView.setTag(picture);
        return convertView;
    }
}
