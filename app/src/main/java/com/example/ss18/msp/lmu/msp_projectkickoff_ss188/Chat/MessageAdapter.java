package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private static List<Message> messages = new ArrayList<Message>();
    private Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public static void addMessage(Message message) {
        messages.add(message);
        Log.i("Eli" , "Hereee add static");
        //notifyDataSetChanged(); // to render the list we need to notify
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
        Log.i("Eli", "hereee in message adapter");
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.belongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

            holder.name.setText(message.getUserName());
            holder.messageBody.setText(message.getText());
            holder.avatar.setImageBitmap(message.getProfilePicture());
            //GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            //drawable.setColor(Color.GREEN);
        }

        return convertView;
    }

}

class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
    public ImageView avatar;
}