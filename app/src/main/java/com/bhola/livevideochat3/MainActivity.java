package com.bhola.livevideochat3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private int CAMERA_PERMISSION_REQUEST_CODE = 123;
    final int NOTIFICATION_REQUEST_CODE = 112;
    public static TextView badge_text;
    public static int unreadMessage_count;
    public static ViewPager2 viewPager2;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }


        Button startVideoBtn = findViewById(R.id.startVideoBtn);
        startVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    startActivity(new Intent(MainActivity.this, CameraActivity.class));
                }
            }
        });

        initializeBottonFragments();
        askForNotificationPermission(); //Android 13 and higher



    }

    private void initializeBottonFragments() {
        viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new PagerAdapter(MainActivity.this));
        TabLayout tabLayout = findViewById(R.id.tabLayout);


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setIcon(R.drawable.videocall);

                        View view1 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.videocall);
                        tab.setCustomView(view1);

                        //By default tab 0 will be selected to change the tint of that tab
                        View tabView = tab.getCustomView();
                        ImageView tabIcon = tabView.findViewById(R.id.icon);
                        tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.themeColor));
                        break;
                    case 1:
                        tab.setIcon(R.drawable.chat);


                        View view2 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view2.findViewById(R.id.icon).setBackgroundResource(R.drawable.chat);
                        tab.setCustomView(view2);
                        unreadMessage_count = getUndreadMessage_Count();

                        badge_text = view2.findViewById(R.id.badge_text);
                        badge_text.setVisibility(View.GONE);

                        if (!Fragment_Messenger.retreive_sharedPreferences(MainActivity.this)) {
                            //logged in first time
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //First time
                                    badge_text.setVisibility(View.VISIBLE);
                                    badge_text.setText("1");
                                    badge_text.setBackgroundResource(R.drawable.badge_background);
//                                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.message_received);
//                                    mediaPlayer.start();

                                }
                            }, 6000);

                        } else {
                            if (unreadMessage_count != 0) {
                                badge_text.setVisibility(View.VISIBLE);
                                badge_text.setText(String.valueOf(unreadMessage_count));
                                badge_text.setBackgroundResource(R.drawable.badge_background);

                            } else {
                                badge_text.setVisibility(View.GONE);
                            }
                        }

                        break;


                    case 2:
                        tab.setIcon(R.drawable.info_2);


                        View view3 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view3.findViewById(R.id.icon).setBackgroundResource(R.drawable.info_2);
                        tab.setCustomView(view3);
                        break;
                    default:
                        tab.setIcon(R.drawable.user2);
                        View view4 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view4.findViewById(R.id.icon).setBackgroundResource(R.drawable.user2);
                        tab.setCustomView(view4);
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Get the custom view of the selected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    // Find the ImageView in the custom view
                    ImageView tabIcon = tabView.findViewById(R.id.icon);

                    // Set the background tint color for the selected tab
                    tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.themeColor));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Get the custom view of the unselected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    // Find the ImageView in the custom view
                    ImageView tabIcon = tabView.findViewById(R.id.icon);

                    // Set the background tint color for the unselected tab
                    tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, com.google.android.ads.mediationtestsuite.R.color.gmts_light_gray));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab reselected, no action needed
            }
        });
    }

    private int getUndreadMessage_Count() {

        ArrayList<ChatItem_ModelClass> userListTemp=new ArrayList<>();
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("messenger_chats", Context.MODE_PRIVATE);

// Retrieve the JSON string from SharedPreferences
        String json="";
        if (SplashScreen.userLoggedIn && SplashScreen.userLoggedIAs.equals("Google")) {
            json = sharedPreferences.getString("userListTemp_Google", null);
        } else {
            json = sharedPreferences.getString("userListTemp_Guest", null);
        }

// Convert the JSON string back to ArrayList
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ChatItem_ModelClass>>() {
        }.getType();


        if (json == null) {
            // Handle case when no ArrayList is saved in SharedPreferences
            return 0;
        } else {
            userListTemp = gson.fromJson(json, type);

            int count = 0;
            for (int i = 0; i < userListTemp.size(); i++) {

                ChatItem_ModelClass modelclass = userListTemp.get(i);

                for (int j = 0; j < modelclass.getUserBotMsg().size(); j++) {
                    UserBotMsg userBotMsg = modelclass.getUserBotMsg().get(j);
                    if (userBotMsg.getSent() == 1 && userBotMsg.getRead() == 0) {
                        count = count + 1;
                    }
                }
                if (modelclass.isContainsQuestion()) {
                    if (modelclass.getQuestionWithAns().getSent() == 1 && modelclass.getQuestionWithAns().getRead() == 0) {
                        count = count + 1;
                    }
                }
            }
            return count;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, proceed with camera setup
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            } else {
                // Camera permission denied, handle it gracefully (e.g., display a message or disable camera functionality)
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
            } else {
                // Explain to the user that the feature is unavailable because
                // the feature requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
            }
        }
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Allow Notification for Daily new Stories ", Toast.LENGTH_LONG).show();
                    }
                }, 1000);
            }
        }
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });


    @Override
    public void onBackPressed() {
        exit_dialog();
        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }
    }

    private void exit_dialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_exit_app, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView exit = promptView.findViewById(R.id.confirm);
        TextView cancel = promptView.findViewById(R.id.cancel);


        AlertDialog exitDialog = builder.create();
        exitDialog.show();


        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SplashScreen.exit_Refer_appNavigation.equals("active") && SplashScreen.Login_Times < 2 && SplashScreen.Refer_App_url2.length() > 10) {

                    Intent j = new Intent(Intent.ACTION_VIEW);
                    j.setData(Uri.parse(SplashScreen.Refer_App_url2));
                    try {
                        startActivity(j);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finishAffinity();
                    System.exit(0);
                    finish();
                    exitDialog.dismiss();

                } else {

                    finishAffinity();
                    finish();
                    System.exit(0);
                    finish();
                    exitDialog.dismiss();

                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.cancel();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        exitDialog.getWindow().setBackgroundDrawable(inset);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showAds() {
        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            if (!SplashScreen.homepageAdShown) {
                ADS_ADMOB.Interstitial_Ad(this);
                SplashScreen.homepageAdShown = true;
            }
        } else {
            if (!SplashScreen.homepageAdShown) {
                ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
                SplashScreen.homepageAdShown = true;
            }
        }
    }


}