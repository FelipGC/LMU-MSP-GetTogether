package de.lmu.msp.gettogether.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.lmu.msp.gettogether.R;

import java.util.ArrayList;

public class ImageSlideAdapter extends PagerAdapter {

    private final String TAG = "ImageSlideAdapter";
    private final ArrayList<Uri> images;
    private LayoutInflater inflater;

    public ImageSlideAdapter(Context context, ArrayList<Uri> images) {
        Log.i(TAG,"images: " + images.toString());
        this.images = images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.image_slider, view, false);
        ImageView imageView = (ImageView) myImageLayout
                .findViewById(R.id.sliderImage);
        imageView.setImageURI(images.get(position));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
