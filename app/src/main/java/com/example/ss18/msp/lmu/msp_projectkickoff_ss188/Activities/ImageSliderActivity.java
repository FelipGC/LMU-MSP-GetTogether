package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Adapters.ImageSlideAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class ImageSliderActivity extends AppCompatActivity {

    public static final ArrayList<Uri> images = new ArrayList<>();
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
}
