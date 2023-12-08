package com.bhola.livevideochat3;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    ChatItem_ModelClass modelClass;
    AlertDialog block_user_dialog = null;
    AlertDialog report_user_dialog = null;
    AlertDialog report_userSucessfully_dialog = null;
    GridLayout gridLayout;
    public static TextView send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyApplication.Ads_State.equals("active")) {
//            showAds();
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile_girl);
//        getSupportActionBar().hide();

//        fullscreenMode();

        getModalClass();


        bindDetails();
        actionbar();
        setImageinGridLayout();
    }


    private void bindDetails() {
        ImageView profileImage = findViewById(R.id.profileImage);
        Picasso.get().load(modelClass.getUserProfile()).into(profileImage);


        TextView profileName = findViewById(R.id.profileName);
        profileName.setText(modelClass.getUserName());

        TextView age = findViewById(R.id.age);
        age.setText(modelClass.getAge());

        TextView country = findViewById(R.id.country);
        country.setText(modelClass.getCountry());

        TextView users = findViewById(R.id.users);
        users.setText(modelClass.getUsers());

        setRatingBar();


        CardView voiceCall = findViewById(R.id.voiceCall);
        voiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, VipMembership.class));
            }
        });

        LottieAnimationView videoCall = findViewById(R.id.videoCall);
        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, VipMembership.class));

            }
        });

        CardView chat = findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, VipMembership.class));
            }
        });

    }

    private void actionbar() {
        ImageView backArrow = findViewById(R.id.backArrow);
        ImageView warningSign = findViewById(R.id.warningSign);
        ImageView menuDots = findViewById(R.id.menuDots);


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        warningSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockUserDialog();
            }
        });

        menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportUserDialog();
            }
        });


    }

    private void blockUserDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_block_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);
        TextView cancel = promptView.findViewById(R.id.cancel);


        block_user_dialog = builder.create();
        block_user_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Profile.this, "User blocked succesfully", Toast.LENGTH_SHORT).show();
                block_user_dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block_user_dialog.dismiss();
            }
        });

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        block_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView report = promptView.findViewById(R.id.reportBtn);
        ImageView cross = promptView.findViewById(R.id.cross);


        report_user_dialog = builder.create();
        report_user_dialog.show();


        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
                reportUserSucessfullDialog();
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserSucessfullDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user_sucessfull, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);


        report_userSucessfully_dialog = builder.create();
        report_userSucessfully_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Profile.this, "User Reported", Toast.LENGTH_SHORT).show();
                report_userSucessfully_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_userSucessfully_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void getModalClass() {

        String userName = getIntent().getStringExtra("userName");
        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {
            if (Fragment_Messenger.userListTemp.get(i).getUserName().equals(userName)) {
                modelClass = Fragment_Messenger.userListTemp.get(i);
            }
        }

    }


    private void setRatingBar() {

        RatingBar ratingBar = findViewById(R.id.ratingBar);
        float percentage = Float.parseFloat(modelClass.getAnswerRate()); // Example percentage value

        float maxRating = ratingBar.getNumStars(); // Get the total number of stars in the RatingBar
        float rating = (percentage / 100) * maxRating; // Calculate the rating value
        ratingBar.setRating(rating);

        TextView answerRatingTextview = findViewById(R.id.answerRatingTextview);
        answerRatingTextview.setText("AnswerRate: " + modelClass.getAnswerRate() + "%");
    }

    private void setImageinGridLayout() {

        gridLayout = findViewById(R.id.gridLayout);
        ArrayList<Map<String, String>> imageList = new ArrayList<>();

        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("url", modelClass.getUserProfile());
        stringMap.put("type", "free");
        imageList.add(stringMap);

        for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {
            String extraMsg = "";
            extraMsg = modelClass.getUserBotMsg().get(i).getExtraMsg();
            if (extraMsg.length() > 5 && extraMsg.contains(".jpg") || extraMsg.contains(".png") || extraMsg.contains(".jpeg")) {

                Map<String, String> stringMap1 = new HashMap<>();
                stringMap1.put("url", extraMsg);
                stringMap1.put("type", "free");
                imageList.add(stringMap1);
            }
        }

        if (modelClass.getContentImages() != null) {
            for (int i = 0; i < modelClass.getContentImages().size(); i++) {

                Map<String, String> stringMap1 = new HashMap<>();
                stringMap1.put("url", modelClass.getContentImages().get(i));
                stringMap1.put("type", "premium");
                imageList.add(stringMap1);
            }
        }


        for (int i = 0; i < imageList.size(); i++) {


            // Create a new CardView
            CardView cardView = new CardView(this);

// Set corner radius and elevation for the CardView
            int cornerRadius = (int) (20 * getResources().getDisplayMetrics().density); // Set the desired corner radius in dp
            float elevation = 0 * getResources().getDisplayMetrics().density; // Set the desired elevation in dp
            cardView.setRadius(cornerRadius);
            cardView.setElevation(elevation);


            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Picasso.get().load(imageList.get(i).get("url")).into(imageView);

            if (MyApplication.coins == 0) {

                if (imageList.get(i).get("type").equals("premium")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        imageView.setRenderEffect(RenderEffect.createBlurEffect(40, 40, Shader.TileMode.MIRROR));
                    }
                }
            }

// Set layout parameters for width and height
            int widthInPixels = (int) (80 * getResources().getDisplayMetrics().density); // Desired width in dp
            int heightInPixels = (int) ((widthInPixels * 4) / 3.5); // Calculated height to maintain 4:3 ratio

            CardView.LayoutParams layoutParams = new CardView.LayoutParams(widthInPixels, heightInPixels);
            layoutParams.setMargins(10, 10, 10, 10);

// Add the ImageView to the CardView
            cardView.addView(imageView, layoutParams);
            int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int originalScreenWidth = displayMetrics.widthPixels;
                    int screenHeight = displayMetrics.heightPixels;


                    // Decrease the screen width by 15%
                    int screenWidth = (int) (originalScreenWidth * 0.85);
                    Fragment_LargePhotoViewer fragment = Fragment_LargePhotoViewer.newInstance(Profile.this, (ArrayList<Map<String, String>>) imageList, finalI, screenWidth, screenHeight);

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment) // Replace with your container ID
                            .addToBackStack(null) // Optional, for back navigation
                            .commit();
                }
            });


// Add the CardView to the GridLayout
            gridLayout.addView(cardView);
        }


    }


    private void fullscreenMode() {


//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
//        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());
//        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
//

//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void showAds() {
        if (MyApplication.Ad_Network_Name.equals("admob")) {
            ADS_ADMOB.Interstitial_Ad(this);
        } else {
            com.facebook.ads.InterstitialAd facebook_IntertitialAds = null;
            ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
        }
    }

}