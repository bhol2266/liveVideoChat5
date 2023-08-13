package com.bhola.livevideochat2;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatScreen_User extends Activity {


    ChatItem_ModelClass modelClass = null;
    AlertDialog block_user_dialog = null;
    AlertDialog report_user_dialog = null;
    AlertDialog report_userSucessfully_dialog = null;
    ArrayList<Chats_Modelclass> chatsArrayList;
    ChatsAdapter chatAdapter;
    RecyclerView recylerview;
    DatabaseReference chatRef;
    MediaPlayer mediaPlayer;

    private Handler handler;
    private Runnable myRunnable;
    private Thread myThread;
    static ArrayList<ChatItem_ModelClass> userListTemp;

    LinearLayout answerslayout, ll2;   //ll2 is message writting box


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen_user);

        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }

        getModalClass();
        sendDataRecyclerview();
        actionbar();
        bottomBtns();


    }

    private void showAds() {
        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            ADS_ADMOB.Interstitial_Ad(this);
        } else {
            com.facebook.ads.InterstitialAd facebook_IntertitialAds = null;
            ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
        }
    }

    private void bottomBtns() {

        CardView sendbtnn = findViewById(R.id.sendbtnn);
        sendbtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatScreen_User.this, VipMembership.class));
            }
        });

        ImageView sendImage = findViewById(R.id.sendImage);
        ImageView videoCall = findViewById(R.id.videoCall);
        ImageView voiceCall = findViewById(R.id.voiceCall);

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatScreen_User.this, VipMembership.class));
            }
        });
        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatScreen_User.this, VipMembership.class));
            }
        });
        voiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatScreen_User.this, VipMembership.class));
            }
        });
    }


    private void getModalClass() {

        retreive_sharedPreferences(ChatScreen_User.this);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        for (int i = 0; i < userListTemp.size(); i++) {
            if (userListTemp.get(i).getUserName().equals(userName)) {
                modelClass = userListTemp.get(i);
            }
        }
        if (modelClass != null) {
            Fragment_Messenger.currentActiveUser = modelClass.getUserName();
        } else {
            startActivity(new Intent(ChatScreen_User.this, MainActivity.class));
        }

    }

    public static boolean retreive_sharedPreferences(Context context) {
        userListTemp = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("messenger_chats", Context.MODE_PRIVATE);
// Retrieve the JSON string from SharedPreferences

        String json = "";
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
            return false;
        } else {
            userListTemp = gson.fromJson(json, type);
            return true;
        }
    }


    private void sendDataRecyclerview() {

        chatsArrayList = new ArrayList<Chats_Modelclass>();


        if (modelClass.isContainsQuestion()) {


            UserQuestionWithAns userQuestionWithAns = modelClass.getQuestionWithAns();
            Chats_Modelclass chats_modelclass = new Chats_Modelclass(userQuestionWithAns.getQuestion(), "mimeType/text", "", modelClass.getUserProfile(), userQuestionWithAns.getDateTime(), 2);
            chatsArrayList.add(chats_modelclass);

            if (modelClass.getQuestionWithAns().getRead() == 0) {
                modelClass.getQuestionWithAns().setRead(1);
                update_userListTemp(0);

            }

            if (userQuestionWithAns.getReply().length() == 0) {
                setAnwswerOptions(userQuestionWithAns);
            } else {
                Chats_Modelclass chats_modelclass2 = new Chats_Modelclass(userQuestionWithAns.getReply(), "mimeType/text", "", modelClass.getUserProfile(), userQuestionWithAns.getDateTime(), 1);
                chatsArrayList.add(chats_modelclass2);

                for (int i = 0; i < modelClass.getQuestionWithAns().getReplyToUser().size(); i++) {
                    UserBotMsg userBotMsg = modelClass.getQuestionWithAns().getReplyToUser().get(i);
                    if (userBotMsg.getSent() == 1) {
                        Chats_Modelclass chats_modelclass3 = new Chats_Modelclass(userBotMsg.getMsg(), "mimeType/text", "", modelClass.getUserProfile(), userBotMsg.getDateTime(), 2);
                        chatsArrayList.add(chats_modelclass3);
                        update_userListTemp_replyMessage(i);
                    }
                }


            }

        } else {
            for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {

                if (modelClass.getUserBotMsg().get(i).getSent() == 1) {
                    UserBotMsg userBotMsg = modelClass.getUserBotMsg().get(i);
                    Chats_Modelclass chats_modelclass = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), modelClass.getUserProfile(), userBotMsg.getDateTime(), 2);
                    chatsArrayList.add(chats_modelclass);

                    if (userBotMsg.getRead() == 0) {
                        userBotMsg.setRead(1);
                        update_userListTemp(i);

                    }
                }
            }
        }


        recylerview = findViewById(R.id.recylerview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatScreen_User.this);
        linearLayoutManager.setStackFromEnd(true);
        recylerview.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatsAdapter(ChatScreen_User.this, chatsArrayList, recylerview, mediaPlayer, modelClass);
        recylerview.setAdapter(chatAdapter);

        scrollrecycelrvewToBottom();

        load_UnsentMessage();

    }

    private void setAnwswerOptions(UserQuestionWithAns userQuestionWithAns) {
        answerslayout = findViewById(R.id.answerslayout);
        ll2 = findViewById(R.id.ll2);


        answerslayout.setVisibility(View.VISIBLE);
        ll2.setVisibility(View.GONE);


        TextView option1, option2;
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);

        option1.setText(userQuestionWithAns.getAnswers().get(0));
        option2.setText(userQuestionWithAns.getAnswers().get(1));

        Date currentTime = new Date();


        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userQuestionWithAns.getAnswers().get(0), "mimeType/text", "", modelClass.getUserProfile(), String.valueOf(currentTime.getTime()), 1);
                chatsArrayList.add(chats_modelclass);
                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);

                modelClass.getQuestionWithAns().setReply(userQuestionWithAns.getAnswers().get(0));
                update_userListTemp(0);


                for (int i = 0; i < Fragment_Messenger.adapter.userList.size(); i++) {
                    ChatItem_ModelClass modelClass1 = Fragment_Messenger.adapter.userList.get(i);
                    if (modelClass1.getUserName().equals(modelClass.getUserName())) {
                        Fragment_Messenger.adapter.notifyItemChanged(i);
                    }
                }

                answerslayout.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userQuestionWithAns.getAnswers().get(1), "mimeType/text", "", modelClass.getUserProfile(), String.valueOf(currentTime.getTime()), 1);
                chatsArrayList.add(chats_modelclass);
                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);

                modelClass.getQuestionWithAns().setReply(userQuestionWithAns.getAnswers().get(1));
                update_userListTemp(0);

                for (int i = 0; i < Fragment_Messenger.adapter.userList.size(); i++) {
                    ChatItem_ModelClass modelClass1 = Fragment_Messenger.adapter.userList.get(i);
                    if (modelClass1.getUserName().equals(modelClass.getUserName())) {
                        Fragment_Messenger.adapter.notifyItemChanged(i);
                    }
                }

                answerslayout.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
            }
        });


    }

    private void update_userListTemp(int index) {

        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {
            if (Fragment_Messenger.userListTemp.get(i).getUserName().equals(modelClass.getUserName())) {

                if (Fragment_Messenger.userListTemp.get(i).isContainsQuestion()) {
                    Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().setRead(1);
                    Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().setSent(1);
                    Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().setReply(modelClass.getQuestionWithAns().getReply());
                } else {
                    Fragment_Messenger.userListTemp.get(i).getUserBotMsg().get(index).setRead(1);
                    Fragment_Messenger.userListTemp.get(i).getUserBotMsg().get(index).setSent(1);
                }
            }
        }
        Fragment_Messenger.save_sharedPrefrence(ChatScreen_User.this, Fragment_Messenger.userListTemp);

    }

    private void update_userListTemp_replyMessage(int index) {

        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {
            if (Fragment_Messenger.userListTemp.get(i).getUserName().equals(modelClass.getUserName())) {

                for (int j = 0; j < modelClass.getQuestionWithAns().getReplyToUser().size(); j++) {
                    if (j == index) {

                        Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().getReplyToUser().get(index).setRead(1);
                        Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().getReplyToUser().get(index).setSent(1);
                        Log.d(SplashScreen.TAG, "update_userListTemp_replyMessage: " + Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().getReplyToUser().get(index).getRead());
                        Log.d(SplashScreen.TAG, "update_userListTemp_replyMessage: " + Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().getReplyToUser().get(index).getSent());
                        Log.d(SplashScreen.TAG, "index: " + j);
                    }

                }
            }
        }
        Fragment_Messenger.save_sharedPrefrence(ChatScreen_User.this, Fragment_Messenger.userListTemp);

    }


    private void scrollrecycelrvewToBottom() {
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollview);
        // Replace R.id.recyclerView with the correct ID of your RecyclerView
        if (recylerview == null || chatsArrayList == null || chatsArrayList.size() == 0) {
            return;
        }
        recylerview.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int lastItemPosition = chatsArrayList.size() - 1;
                    int y = recylerview.getChildAt(lastItemPosition).getTop();
                    nestedScrollView.smoothScrollTo(0, y);
                } catch (Exception e) {
                    // Handle any exception that might occur while scrolling
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private void load_UnsentMessage() {

        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                checkForUpdate();
                // Schedule the task to run again after 1 second
                handler.postDelayed(this, 500);
            }
        };

        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Start the task on the new thread
                handler.postDelayed(myRunnable, 1000);
            }
        });

        myThread.start();

    }

    private void checkForUpdate() {
        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {
            if (Fragment_Messenger.userListTemp.get(i).getUserName().equals(modelClass.getUserName())) {


                if (modelClass.isContainsQuestion()) {
                    for (int j = 0; j < Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().getReplyToUser().size(); j++) {
                        UserBotMsg userBotMsg = Fragment_Messenger.userListTemp.get(i).getQuestionWithAns().getReplyToUser().get(j);

                        if (userBotMsg.getSent() == 1) {
                            if (userBotMsg.getRead() == 0) {
                                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), modelClass.getUserProfile(), userBotMsg.getDateTime(), 2);
                                chatsArrayList.add(chats_modelclass);

                                modelClass.getQuestionWithAns().getReplyToUser().get(j).setRead(1);
                                update_userListTemp_replyMessage(j);
                                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);
                                scrollrecycelrvewToBottom();
                            }
                        }
                    }
                } else {


                    for (int j = 0; j < Fragment_Messenger.userListTemp.get(i).getUserBotMsg().size(); j++) {
                        UserBotMsg userBotMsg = Fragment_Messenger.userListTemp.get(i).getUserBotMsg().get(j);


                        if (userBotMsg.getSent() == 1) {
                            if (modelClass.getUserBotMsg().get(j).getRead() == 0) {


                                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), modelClass.getUserProfile(), userBotMsg.getDateTime(), 2);
                                chatsArrayList.add(chats_modelclass);
                                modelClass.getUserBotMsg().get(j).setSent(1);
                                modelClass.getUserBotMsg().get(j).setRead(1);
                                update_userListTemp(j);
                                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);
                                scrollrecycelrvewToBottom();
                            }
                        }
                    }
                }

            }
        }
    }


    private void actionbar() {
        ImageView backArrow = findViewById(R.id.backArrow);
        ImageView warningSign = findViewById(R.id.warningSign);
        ImageView menuDots = findViewById(R.id.menuDots);
        RelativeLayout alertBar = findViewById(R.id.alertBar);
        TextView profileName = findViewById(R.id.profileName);
        TextView viewProfile = findViewById(R.id.viewProfile);


        profileName.setText(modelClass.getUserName());

        ImageView profileImage = findViewById(R.id.profileImage);
        Picasso.get().load(modelClass.getUserProfile()).into(profileImage);
        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ChatScreen_User.this, Profile.class);
                intent.putExtra("userName", modelClass.getUserName());
                startActivity(intent);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ChatScreen_User.this, Profile.class);
                intent.putExtra("userName", modelClass.getUserName());
                startActivity(intent);
            }
        });


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

        alertBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertBar.setVisibility(View.INVISIBLE);
            }
        });


    }


    private void blockUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ChatScreen_User.this);
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
                Toast.makeText(ChatScreen_User.this, "User blocked succesfully", Toast.LENGTH_SHORT).show();
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

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ChatScreen_User.this);
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

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ChatScreen_User.this);
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
                Toast.makeText(ChatScreen_User.this, "User Reported", Toast.LENGTH_SHORT).show();
                report_userSucessfully_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_userSucessfully_dialog.getWindow().setBackgroundDrawable(inset);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the task and terminate the new thread
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler != null && handler.hasCallbacks(myRunnable)) {
                handler.removeCallbacks(myRunnable);
            }
        }

        if (myThread != null && myThread.isAlive()) {
            myThread.interrupt();
        }
    }
}

class ChatsAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Chats_Modelclass> chatsArrayList;
    int SENDER = 1;
    int RECEIVER = 2;
    RecyclerView recyclerview;
    MediaPlayer mediaPlayer;
    ChatItem_ModelClass modelClass;

    public ChatsAdapter(Context context, ArrayList<Chats_Modelclass> chatsArrayList, RecyclerView recyclerview, MediaPlayer mediaPlayer, ChatItem_ModelClass modelClass) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.recyclerview = recyclerview;
        this.mediaPlayer = mediaPlayer;
        this.modelClass = modelClass;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderVierwHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.userchat_reciver_layout, parent, false);
            return new ReciverViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chats_Modelclass chats = chatsArrayList.get(position);


        long timestamp = Long.parseLong(chats.getTimeStamp()); // Example timestamp value

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MM-dd");
        String formattedDate = sdf.format(date);


        if (chats.getViewType() == 2) {
            ChatsAdapter.ReciverViewHolder reciverViewHolder = (ReciverViewHolder) holder;
            reciverViewHolder.timeStamp.setText(formattedDate);
            Picasso.get()
                    .load(chats.getProfileUrl())
                    .into(reciverViewHolder.profileImage);

            if (chats.getMessageType().equals("mimeType/text")) {
                reciverViewHolder.textMsg.setText(chats.getMessage());
                reciverViewHolder.picMsgLayout.setVisibility(View.GONE);
                reciverViewHolder.audioMsg.setVisibility(View.GONE);
            }
            if (chats.getMessageType().equals("mimeType/audio")) {
                reciverViewHolder.picMsgLayout.setVisibility(View.GONE);
                reciverViewHolder.audioMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            return;
                        }
                        try {
                            reciverViewHolder.audioProgressBar.setVisibility(View.VISIBLE);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build());
                            mediaPlayer.setDataSource(chats.getExtraMsg());
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    reciverViewHolder.audioProgressBar.setVisibility(View.GONE);
                                    reciverViewHolder.playAudiolottie.playAnimation();
                                    mediaPlayer.start();
                                }
                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    reciverViewHolder.playAudiolottie.cancelAnimation();
                                    mediaPlayer.stop();

                                }
                            }); // Set the OnCompletionListener


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                reciverViewHolder.textMsg.setVisibility(View.GONE);
            }
            if (chats.getMessageType().equals("mimeType/image")) {
                Picasso.get()
                        .load(chats.getExtraMsg())
                        .into(reciverViewHolder.picMsg);
                reciverViewHolder.textMsg.setVisibility(View.GONE);
                reciverViewHolder.audioMsg.setVisibility(View.GONE);

                reciverViewHolder.picMsgLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Map<String,String>> imageList = new ArrayList<>();

                        Map<String, String> stringMap = new HashMap<>();
                        stringMap.put("url", modelClass.getUserProfile());
                        stringMap.put("type", "free");
                        imageList.add(stringMap);


                        for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {
                            String extraMsg = "";
                            extraMsg = modelClass.getUserBotMsg().get(i).getExtraMsg();
                            if (extraMsg.length() > 5 && extraMsg.contains(".jpg") || extraMsg.contains(".png")) {
                                Map<String, String> stringMap2 = new HashMap<>();
                                stringMap2.put("url",extraMsg);
                                stringMap2.put("type", "free");
                                imageList.add(stringMap2);
                            }
                        }

                        int index = 0;
                        for (int i = 0; i < imageList.size(); i++) {
                            if (imageList.get(i).equals(chats.getExtraMsg())) {
                                index = i;
                            }

                        }
                        ImageViewerDialog dialog = new ImageViewerDialog(context, imageList, index);
                        dialog.show();

                    }
                });


            }


        }

        if (chats.getViewType() == 1) {
            ChatsAdapter.SenderVierwHolder senderVierwHolder = (ChatsAdapter.SenderVierwHolder) holder;
            senderVierwHolder.msgtxt.setText(chats.getMessage());
            senderVierwHolder.timeStamp.setText(formattedDate);


            if (SplashScreen.userLoggedIAs.equals("Google")) {
                SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
                String urll = sh.getString("photoUrl", "not set");
                Picasso.get()
                        .load(urll)
                        .into(senderVierwHolder.profile);
            }
        }

    }


    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chats_Modelclass messages = chatsArrayList.get(position);
        if (messages.getViewType() == 1) {
            return SENDER;
        } else {
            return RECEIVER;
        }
    }

    static class SenderVierwHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView msgtxt;
        TextView timeStamp;

        public SenderVierwHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profileImage);
            msgtxt = itemView.findViewById(R.id.message);
            timeStamp = itemView.findViewById(R.id.timeStamp);

        }
    }

    static class ReciverViewHolder extends RecyclerView.ViewHolder {
        TextView textMsg, timeStamp;
        ImageView picMsg, profileImage;
        CardView audioMsg;
        FrameLayout picMsgLayout;
        LottieAnimationView playAudiolottie;
        ProgressBar audioProgressBar;


        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            textMsg = itemView.findViewById(R.id.textMsg);
            picMsg = itemView.findViewById(R.id.picMsg);
            audioMsg = itemView.findViewById(R.id.audioMsg);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            profileImage = itemView.findViewById(R.id.profileImage);
            picMsgLayout = itemView.findViewById(R.id.picMsgLayout);
            playAudiolottie = itemView.findViewById(R.id.playAudiolottie);
            audioProgressBar = itemView.findViewById(R.id.audioProgressBar);

        }
    }


}


class Chats_Modelclass {

    String message;
    String messageType;
    String extraMsg;
    String profileUrl;
    String timeStamp;
    int viewType;//viewType 1 is sender 2 is receiver

    public Chats_Modelclass() {
    }

    public Chats_Modelclass(String message, String messageType, String extraMsg, String profileUrl, String timeStamp, int viewType) {
        this.message = message;
        this.messageType = messageType;
        this.extraMsg = extraMsg;
        this.profileUrl = profileUrl;
        this.timeStamp = timeStamp;
        this.viewType = viewType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getExtraMsg() {
        return extraMsg;
    }

    public void setExtraMsg(String extraMsg) {
        this.extraMsg = extraMsg;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}

