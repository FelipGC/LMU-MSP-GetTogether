package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private static final String TAG = "MessageAdapter";

    private static List<Message> messages = new ArrayList<Message>();
    private Context context;
    public MessageAdapter(Context context) {
        this.context = context;
    }

    public static void addMessage(Message message) {
        messages.add(message);
        Log.i(TAG , "Added new message to listView.");
        //notifyDataSetChanged(); // to render the list we need to notify
    }

    /*
     ** Clears the list of messages and notifies the adapter
     */
    public void clearContent() {
        Log.i(TAG, "Cleared content.");
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // Handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();

        Log.i(TAG, "View for message bubble created.");

        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);
        if(message.getUserName().equals("SYSTEM")){
            convertView = messageInflater.inflate(R.layout.view_message_system, null);
        }
        else if (message.belongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.view_message_mine, null);
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.view_message_their, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setText(message.getUserName());
            Bitmap bitmap = LocalDataBase.getBitmapFromUser(message.getId());
            holder.avatar.setImageBitmap(bitmap);
            Log.i(TAG,"Loading image into avatar: " + bitmap);
            //GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            //drawable.setColor(Color.GREEN);
        }

        holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
        holder.messageBody.setText(message.getText());
        convertView.setTag(holder);

        return convertView;
    }

}

class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
    public ImageView avatar;
}