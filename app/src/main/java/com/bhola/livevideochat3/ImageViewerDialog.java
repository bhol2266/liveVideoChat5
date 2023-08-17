package com.bhola.livevideochat3;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;


public class ImageViewerDialog extends Dialog {
    private Context context;
    private ArrayList<Map<String, String>> imageUrls;
    private int selectedIndex;

    public ImageViewerDialog(Context context, ArrayList<Map<String, String>> imageUrls, int selectedIndex) {
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
    private ArrayList<Map<String, String>> imageUrls;

    public ImagePagerAdapter(Context context, ArrayList<Map<String, String>> imageUrls) {
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
        ImageView imageView = view.findViewById(R.id.imageView);

        Picasso.get().load(imageUrls.get(position).get("url")).into(imageView);

        if (SplashScreen.coins ==0 ) {
            if (imageUrls.get(position).get("type").equals("premium")) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    imageView.setRenderEffect(RenderEffect.createBlurEffect(100, 100, Shader.TileMode.MIRROR));
                }
                ImageView lock = view.findViewById(R.id.lock);
                lock.setVisibility(View.VISIBLE);
                lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startActivity(new Intent(context, VipMembership.class));
                    }
                });
            }

        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
