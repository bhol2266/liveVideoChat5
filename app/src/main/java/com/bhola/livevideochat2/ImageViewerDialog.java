package com.bhola.livevideochat2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;


public class ImageViewerDialog extends Dialog {
    private Context context;
    private ArrayList<String> imageUrls;
    private int selectedIndex;

    public ImageViewerDialog(Context context, ArrayList<String> imageUrls, int selectedIndex) {
        super(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        this.context = context;
        this.imageUrls = imageUrls;
        this.selectedIndex = selectedIndex;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_image_viewer);

        // Initialize ViewPager and its adapter
        ViewPager viewPager = findViewById(R.id.view_pager);
        ImageView cross = findViewById(R.id.cross);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(); // Close the dialog
            }
        });
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(context, imageUrls);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(selectedIndex);

        // Add any other logic or UI customization as needed
    }

}

class ImagePagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> imageUrls;

    public ImagePagerAdapter(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_image2, container, false);
        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view.findViewById(R.id.imageView);


        Picasso.get().load(imageUrls.get(position)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImage(ImageSource.bitmap(bitmap));
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Optional: You can implement placeholder behavior here
            }
        });



//        Picasso.get().load(imageUrls.get(position)).into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
