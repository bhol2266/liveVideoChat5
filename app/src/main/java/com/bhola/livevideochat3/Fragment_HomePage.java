package com.bhola.livevideochat3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Fragment_HomePage extends Fragment {

    private RecyclerView recyclerView1, recyclerView2, recyclerView3;
    private ImageAdapter imageAdapter, imageAdapter2, imageAdapter3;
    LinearSmoothScroller smoothScroller1, smoothScroller2, smoothScroller3;
    List<Integer> imageList2;
    private Handler handlerAnimation, blinkhandler, countHandler;
    RelativeLayout btnRelativelayout;
    int randomNumber, current_value;
    TextView onlineCountTextview;
    Context context;
    AlertDialog permissionDialog;


    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isCameraPermissionGranted;
    private boolean isMicrophonePermissionGranted;
    private String[] PERMISSIONS;


    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int MICROPHONE_PERMISSION_REQUEST_CODE = 101;


    public Fragment_HomePage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        context = getContext();
        // Inflate the layout for context fragment


        setimagesScrolling(view, context);
        setButtonAnimation(view, context);
        blinkWorldMap(view, context);
        update_onlineCount(view, context);

        PERMISSIONS = new String[]{

                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };

        return view;
    }


    private void update_onlineCount(View view, Context context) {
        Random random = new Random();

//get random number between 1000 - 4000
        int min = 1000;
        int max = 4000;
        randomNumber = random.nextInt(max - min + 1) + min;
        current_value = randomNumber;


        onlineCountTextview = view.findViewById(R.id.onlineCount);
        onlineCountTextview.setText(String.valueOf(randomNumber));
        incrementValueSlowly(view, context, onlineCountTextview);


        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int range[] = {5, 10, 15, 20, 25};
                int randomIndex = new Random().nextInt(range.length);
                int randomElement = range[randomIndex];
                int min1 = randomNumber - randomElement;
                int max1 = randomNumber + randomElement;
                randomNumber = random.nextInt(max1 - min1 + 1) + min1;
                incrementValueSlowly(view, context, onlineCountTextview);
            }
        };

        timer.schedule(task, 0, 5000);
    }


    private void incrementValueSlowly(View view, Context context, TextView onlineCountTextview) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (current_value < randomNumber) {
                                current_value++;
                                onlineCountTextview.setText(String.valueOf(current_value));
                                incrementValueSlowly(view, context, onlineCountTextview);
                            }
                            if (current_value > randomNumber) {
                                current_value--;
                                onlineCountTextview.setText(String.valueOf(current_value));
                                incrementValueSlowly(view, context, onlineCountTextview);
                            }
                        }
                    }, 250); // Delay of 50 milliseconds between each increment
                }
            });

        }

    }

    private void blinkWorldMap(View view, Context context) {
        ImageView worldmap;

        worldmap = view.findViewById(R.id.worldmap);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.blink_animation);
        worldmap.startAnimation(animation);

        blinkhandler = new Handler();
        blinkhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                blinkWorldMap(view, context);
            }
        }, 1000);

    }

    private void setButtonAnimation(View view, Context context) {

        ImageView Shine, worldmap;
        TextView btnTextview;

        btnTextview = view.findViewById(R.id.img);
        btnTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!hasPermissions(context, PERMISSIONS)) {

                    checkPermissionDialog();

                } else {

                    Intent intent = new Intent(context, BeforeVideoCall.class);
                    intent.putExtra("count", onlineCountTextview.getText().toString());
                    startActivity(intent);
                }

            }
        });
        Shine = view.findViewById(R.id.shine);


        btnRelativelayout = view.findViewById(R.id.btnRelativelayout);

        //Start the animations preoidically by calling 'shineStart' method with ScheduledExecutorService
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Animation animation = new TranslateAnimation(0, btnTextview.getWidth() + Shine.getWidth(), 0, 0);
                        animation.setDuration(1500);
                        animation.setFillAfter(false);
                        animation.setInterpolator(new AccelerateDecelerateInterpolator());
                        Shine.startAnimation(animation);
                        startAnimation(context);

                    }
                });
            }
        }, 2, 5, TimeUnit.SECONDS);


        handlerAnimation = new Handler();

    }

    public void checkPermissionDialog() {


        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptView = inflater.inflate(R.layout.dialog_allow_permission, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        LinearLayout notificationLayout = promptView.findViewById(R.id.notificationLayout);
        LinearLayout locationLayout = promptView.findViewById(R.id.locationLayout);
        LinearLayout storageLayout = promptView.findViewById(R.id.storageLayout);

        notificationLayout.setVisibility(View.GONE);
        locationLayout.setVisibility(View.GONE);
        storageLayout.setVisibility(View.GONE);

        TextView allow = promptView.findViewById(R.id.allow);
        allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionDialog.dismiss();
                ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, 1);

            }
        });


        permissionDialog = builder.create();
        permissionDialog.show();
        permissionDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT); //Controlling width and height.


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        permissionDialog.getWindow().setBackgroundDrawable(inset);
        permissionDialog.getWindow().setLayout(750, WindowManager.LayoutParams.WRAP_CONTENT); //Controlling width and height.

    }


    private void startAnimation(Context context) {
        // Assuming you have a view object, e.g., myView
        Animation animationUp = AnimationUtils.loadAnimation(context, R.anim.bottom_scaleup);
        btnRelativelayout.startAnimation(animationUp);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animationDown = AnimationUtils.loadAnimation(context, R.anim.bottom_scaledown);
                btnRelativelayout.startAnimation(animationDown);
            }
        }, 800);


    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView1 != null) {
            moveImages();

        }


    }

    private void setimagesScrolling(View view, Context context) {
        recyclerView1 = view.findViewById(R.id.recyclerView1);
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView3 = view.findViewById(R.id.recyclerView3);


        LinearLayoutManager layoutManager1 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false; // Disable horizontal scrolling for recyclerView1
            }
        };
        recyclerView1.setLayoutManager(layoutManager1);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false; // Disable horizontal scrolling for recyclerView2
            }
        };
        recyclerView2.setLayoutManager(layoutManager2);

        LinearLayoutManager layoutManager3 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false; // Disable horizontal scrolling for recyclerView3
            }
        };
        recyclerView3.setLayoutManager(layoutManager3);


        // Create a list of image resources from the drawable folder
        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.mgirl1);
        imageList.add(R.drawable.mgirl2);
        imageList.add(R.drawable.mgirl3);
        imageList.add(R.drawable.mgirl4);
        imageList.add(R.drawable.mgirl5);
        imageList.add(R.drawable.mgirl6);
        imageList.add(R.drawable.mgirl8);
        imageList.add(R.drawable.mgirl9);

        imageList2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            imageList2.add(R.drawable.mgirl10);
            imageList2.add(R.drawable.mgirl11);
            imageList2.add(R.drawable.mgirl12);
            imageList2.add(R.drawable.mgirl13);
            imageList2.add(R.drawable.mgirl14);
            imageList2.add(R.drawable.mgirl15);
            imageList2.add(R.drawable.mgirl16);
            imageList2.add(R.drawable.mgirl17);
        }


        List<Integer> imageList3 = new ArrayList<>();
        imageList3.add(R.drawable.mgirl18);
        imageList3.add(R.drawable.mgirl19);
        imageList3.add(R.drawable.mgirl20);
        imageList3.add(R.drawable.mgirl21);
        imageList3.add(R.drawable.mgirl22);
        imageList3.add(R.drawable.mgirl23);
        imageList3.add(R.drawable.mgirl24);


        imageAdapter = new ImageAdapter(context, imageList);
        imageAdapter2 = new ImageAdapter(context, imageList2);
        imageAdapter3 = new ImageAdapter(context, imageList3);
        recyclerView1.setAdapter(imageAdapter);
        recyclerView2.setAdapter(imageAdapter2);
        recyclerView3.setAdapter(imageAdapter3);
        recyclerView2.scrollToPosition(imageList2.size());


    }

    private void moveImages() {


        smoothScroller1 = new LinearSmoothScroller(recyclerView1.getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 200; // scroll speed
            }
        };

        smoothScroller1.setTargetPosition(Integer.MAX_VALUE);
        recyclerView1.getLayoutManager().startSmoothScroll(smoothScroller1);

//-----------------------------------------------------------------------------------------

        smoothScroller2 = new LinearSmoothScroller(recyclerView2.getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 200; // scroll speed
            }
        };

        smoothScroller2.setTargetPosition(-Integer.MAX_VALUE);
        recyclerView2.getLayoutManager().startSmoothScroll(smoothScroller2);

//-----------------------------------------------------------------------------------------


        smoothScroller3 = new LinearSmoothScroller(recyclerView3.getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 200; // scroll speed
            }
        };
        smoothScroller3.setTargetPosition(Integer.MAX_VALUE);
        recyclerView3.getLayoutManager().startSmoothScroll(smoothScroller3);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handlerAnimation.removeCallbacksAndMessages(null);
        blinkhandler.removeCallbacksAndMessages(null);

    }


    private boolean hasPermissions(Context context, String... PERMISSIONS) {

        if (context != null && PERMISSIONS != null) {

            for (String permission : PERMISSIONS) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            boolean permissionGranted = true;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Camera Permission is granted", Toast.LENGTH_SHORT).show();
            } else {
                permissionGranted = false;
                Toast.makeText(context, "Camera Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Record Audio is granted", Toast.LENGTH_SHORT).show();
            } else {
                permissionGranted = false;
                Toast.makeText(context, "Record Audio is denied", Toast.LENGTH_SHORT).show();
            }


        }
    }

}


