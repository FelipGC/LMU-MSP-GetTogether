package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Inbox;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Adapter for displaying received files
 */
public class InboxAdapter extends BaseAdapter {

    private final String TAG = "InboxAdapter";
    /**
     *An array list which stores/displays all
     *data or files received from a particular presenter (advertiser)
     */
    private final ArrayList<InboxAdapterItem> inboxList = new ArrayList<>();
    private Context context;

    public InboxAdapter(Context context) {
        this.context = context;
    }

    public void add(ConnectionEndpoint endpoint, File file) {
        Log.i(TAG, "Add to inBoxList: " + file.getName());
        boolean containsID = false;
        for (InboxAdapterItem h : inboxList ){
            if(h.getClass().equals(endpoint.getId())){
                h.getFiles().add(file);
                containsID = true;
            }
            break;
        }
        // If not already existing, create entry.
        if(!containsID)
            inboxList.add(new InboxAdapterItem(endpoint, file));
        // To render the list we need to notify.
        notifyDataSetChanged();
    }

    public void remove(String endpointID,File file) {
        Log.i(TAG, "Remove from inBoxList: " + file.getName());
        for (InboxAdapterItem h : inboxList ){
            if(h.getClass().equals(endpointID))
                h.getFiles().remove(file);
            break;
        }
        // To render the list we need to notify.
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return inboxList.size();
    }

    @Override
    public Object getItem(int position) {
        return inboxList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates list entry with an inflated UI
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView()");
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = messageInflater.inflate(R.layout.inbox_list_entry, null);
        convertView.setTag(inboxList.get(position));
        return convertView;
    }
}
