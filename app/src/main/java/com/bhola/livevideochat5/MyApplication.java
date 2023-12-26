package com.bhola.livevideochat5;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.bhola.livevideochat5.Models.UserModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;

public class MyApplication extends Application {
    private static Context context;
    static String TAG = "TAGA";


    public static String terms_service_link = "https://sites.google.com/view/desi-girls-live-video-chat/terms_service";
    public static String privacy_policy_link = "https://sites.google.com/view/desi-girls-live-video-chat/privacypolicy";


    public static UserModel userModel;
    public static String Notification_Intent_Firebase = "inactive";
    public static String Ad_Network_Name = "facebook";
    public static String Refer_App_url2 = "https://play.google.com/store/apps/developer?id=UK+DEVELOPERS";
    public static String Ads_State = "inactive";
    public static String App_updating = "active";
    public static String databaseURL_video = "https://SplashScreen.class.ap-south-1.linodeobjects.com//"; //default
    public static String databaseURL_images = "https://bucket2266.blr1.digitaloceanspaces.com/"; //default

    public static String exit_Refer_appNavigation = "inactive";
    public static String Notification_ImageURL = "https://hotdesipics.co/wp-content/uploads/2022/06/Hot-Bangla-Boudi-Ki-Big-Boobs-Nangi-Selfies-_002.jpg";
    public static int Login_Times = 0;


    //Google login
    public static boolean userLoggedIn = false;
    public static int coins = 0;
    public static String userLoggedIAs = "not set";
    public static String authProviderName = "";
    public static FirebaseUser firebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;


    //location
    public static String currentCity = "";
    public static String currentCountry = "";


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                startTransferProcess();

            }
        }, 5000);


    }


}
