package de.lmu.msp.gettogether.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import de.lmu.msp.gettogether.Adapters.ImageSlideAdapter;
import de.lmu.msp.gettogether.R;
import me.relex.circleindicator.CircleIndicator;

public class ImageSliderActivity extends AppCompatActivity {

    public static ArrayList<Uri> images;
    private static ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slider);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new ImageSlideAdapter(this, (ArrayList<Uri>) images.clone()));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }
    public static void addPicture(Uri uri){
        if(images==null) images = new ArrayList<>();
        images.add(uri);
    }
}
