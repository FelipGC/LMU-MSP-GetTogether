package de.lmu.msp.gettogether.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import de.lmu.msp.gettogether.Activities.ImageSliderActivity;
import de.lmu.msp.gettogether.R;


public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private final int READ_REQUEST_CODE = 4242;
    private TextView title;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        ImageButton button = view.findViewById(R.id.inboxFilePicker);
        title = view.findViewById(R.id.inboxTitle);
        //Set ClickListener (normal click)
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked on file picker.");
                startSlidePresentation();
            }
        });
        updateInboxFragment();
        return view;
    }

    public void addPicture(Uri uri){
        if (uri != null) {
            //Add uri to image to the list
            ImageSliderActivity.addPicture(uri);
        }
        updateInboxFragment();
    }

    /**
     * Renames and saves the payload to a specific location
     */
    public void updateInboxFragment() {

        Log.i(TAG, "updateInboxFragment()");
        if(title!=null) {
            if (ImageSliderActivity.images == null || ImageSliderActivity.images.size() == 0) {
                title.setText(R.string.no_pictures);
            }else {
                title.setText(getString(R.string.images_count_title, ImageSliderActivity.images.size()));
            }
        }

    }

    /**
     * Displays all received pictures as a presentation in an extra activity
     */
    private void startSlidePresentation() {
        Log.i(TAG, "startSlidePresentation()");
        Intent intent = new Intent(getContext(), ImageSliderActivity.class);
        startActivity(intent);

    }

}
