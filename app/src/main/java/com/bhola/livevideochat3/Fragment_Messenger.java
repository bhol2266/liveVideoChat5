package com.bhola.livevideochat3;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;


public class Fragment_Messenger extends Fragment {


    RecyclerView recyclerview;
    public static ArrayList<ChatItem_ModelClass> userList;
    public static ArrayList<ChatItem_ModelClass> userListTemp;
    LinearLayoutManager layoutManager;
    public static MessengeItemsAdapter adapter;
    public static String currentActiveUser = "";
    public static int count = 0;

    private Dialog alertNotificationDialog;
    private static final long AUTO_DISMISS_DELAY = 4000; // 4 seconds


    public Fragment_Messenger() {
        // Required empty public constructor
    }

    View view;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_messenger, container, false);

        context = getContext();
        // Inflate the layout for this fragment


        setRecyclerView();
        setup_CustomerCare_Chat();

        return view;


    }

    private void setup_CustomerCare_Chat() {

        TextView userName = view.findViewById(R.id.userName);
        TextView lastMessage = view.findViewById(R.id.lastMessage);
        TextView messageTime = view.findViewById(R.id.messageTime);
        TextView messageCount = view.findViewById(R.id.messageCount);
        LinearLayout chatItemClick = view.findViewById(R.id.chatItemClick);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String formattedTime = dateFormat.format(currentTime);
        messageTime.setText(formattedTime);


        chatItemClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.viewPager2.setCurrentItem(2); // Switch to Fragment B
            }
        });


    }


    private void setRecyclerView() {
        recyclerview = view.findViewById(R.id.recyclerview);

        readDataFromJson();


    }

    private void readDataFromJson() {

        userList = new ArrayList<>();
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google") && MyApplication.App_updating.equals("inactive")) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("BotChats/users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                        Long longValue = (Long) userSnapshot.child("id").getValue();
                        int id = longValue.intValue();
                        String userName = (String) userSnapshot.child("userName").getValue();
                        String gender = (String) userSnapshot.child("gender").getValue();
                        String age = (String) userSnapshot.child("age").getValue();
                        String country = (String) userSnapshot.child("country").getValue();

                        ArrayList<String> contentImages = new ArrayList<>();
                        for (DataSnapshot document : userSnapshot.child("content").getChildren()) {
                            contentImages.add(document.getValue(String.class));
                        }


                        String users = (String) userSnapshot.child("users").getValue();
                        String answerRate = (String) userSnapshot.child("answerRate").getValue();
                        String userProfile = (String) userSnapshot.child("userProfile").getValue();

                        Boolean booleanValue = userSnapshot.child("containsQuestion").getValue(Boolean.class);
                        boolean containsQuestion = booleanValue.booleanValue();

                        String recommendationType = (String) userSnapshot.child("recommendationType").getValue();


                        ArrayList<UserBotMsg> userBotMsgList = new ArrayList<>();
                        if (!containsQuestion) {
                            for (DataSnapshot userBotmsq_Snapshot : userSnapshot.child("userBotMsg").getChildren()) {
                                UserBotMsg userBotMsg = userBotmsq_Snapshot.getValue(UserBotMsg.class);
                                userBotMsgList.add(userBotMsg);
                            }
                        }

                        UserQuestionWithAns questionWithAns = null;
                        if (containsQuestion) {

                            String action = (String) userSnapshot.child("questionWithAns").child("action").getValue();
                            String dateTime = (String) userSnapshot.child("questionWithAns").child("dateTime").getValue();
                            String question = (String) userSnapshot.child("questionWithAns").child("question").getValue();
                            String reply = (String) userSnapshot.child("questionWithAns").child("reply").getValue();


                            Long longValue1 = (Long) userSnapshot.child("questionWithAns").child("read").getValue();
                            int read = longValue1.intValue();
                            Long longValue3 = (Long) userSnapshot.child("questionWithAns").child("sent").getValue();
                            int sent = longValue3.intValue();

                            ArrayList<String> answersList = new ArrayList<>();


                            for (DataSnapshot snapshot : userSnapshot.child("questionWithAns").child("answers").getChildren()) {
                                String value = snapshot.getValue(String.class);
                                if (value != null) {
                                    answersList.add(value);
                                }
                            }


                            ArrayList<UserBotMsg> replyToUserList = new ArrayList<>();
                            for (DataSnapshot userBotmsq_Snapshot : userSnapshot.child("questionWithAns").child("replyToUser").getChildren()) {
                                UserBotMsg userBotMsg = userBotmsq_Snapshot.getValue(UserBotMsg.class);
                                replyToUserList.add(userBotMsg);
                            }

                            questionWithAns = new UserQuestionWithAns(question, answersList, dateTime, action, read, sent, reply, replyToUserList);
                        }


                        ChatItem_ModelClass user = new ChatItem_ModelClass(id, userName, gender, age, country, contentImages, users, answerRate, userProfile, containsQuestion, recommendationType, userBotMsgList, questionWithAns);

                        userList.add(user);

                    }


                    sendDataToRecyclerview();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(MyApplication.TAG, " userList.size(): " + databaseError.getMessage());
                }
            });
        } else {

            String json;
            try {
                InputStream is = context.getAssets().open("chats1.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

// Parse JSON and create ArrayList of Map objects
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray usersArray = jsonObject.getJSONArray("users");

                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject userObject = usersArray.getJSONObject(i);
                    int id = userObject.getInt("id");
                    String userName = userObject.getString("userName");

                    String gender = userObject.getString("gender");
                    String age = userObject.getString("age");
                    String country = userObject.getString("country");
                    String users = userObject.getString("users");
                    String answerRate = userObject.getString("answerRate");
                    String userProfile = userObject.getString("userProfile");
                    boolean containsQuestion = userObject.getBoolean("containsQuestion");
                    String recommendationType = userObject.getString("recommendationType");

                    JSONArray userBotMsgArray = userObject.getJSONArray("userBotMsg");
                    ArrayList<UserBotMsg> userBotMsgList = new ArrayList<>();

                    for (int j = 0; j < userBotMsgArray.length(); j++) {
                        JSONObject userBotMsgObject = userBotMsgArray.getJSONObject(j);
                        int msgId = userBotMsgObject.getInt("id");
                        String msg = userBotMsgObject.getString("msg");
                        String mimeType = userBotMsgObject.getString("mimeType");
                        String dateTime = userBotMsgObject.getString("dateTime");
                        String extraMessage = "";
                        try {
                            extraMessage = userBotMsgObject.getString("extraMsg");
                        } catch (Exception e) {
                        }

                        int nextMsgDelay = userBotMsgObject.getInt("nextMsgDelay");
                        int read = userBotMsgObject.getInt("read");
                        int sent = userBotMsgObject.getInt("sent");

                        UserBotMsg userBotMsg = new UserBotMsg(msgId, msg, mimeType, extraMessage, dateTime, nextMsgDelay, read, sent, "free", 2);
                        userBotMsgList.add(userBotMsg);
                    }

                    UserQuestionWithAns questionWithAns = null;
                    if (containsQuestion) {
                        JSONObject questionWithAnsObject = userObject.getJSONObject("questionWithAns");
                        String question = questionWithAnsObject.getString("question");
                        JSONArray answersArray = questionWithAnsObject.getJSONArray("answers");
                        String action = questionWithAnsObject.getString("action");
                        String dateTime = questionWithAnsObject.getString("dateTime");
                        String reply = questionWithAnsObject.getString("reply");
                        int read = questionWithAnsObject.getInt("read");
                        int sent = questionWithAnsObject.getInt("sent");

                        ArrayList<String> answersList = new ArrayList<>();
                        for (int k = 0; k < answersArray.length(); k++) {
                            String answer = answersArray.getString(k);
                            answersList.add(answer);
                        }


                        JSONArray replyToUserArray = questionWithAnsObject.getJSONArray("replyToUser");
                        ArrayList<UserBotMsg> replyToUserList = new ArrayList<>();

                        for (int j = 0; j < replyToUserArray.length(); j++) {
                            JSONObject replyToUseObject = replyToUserArray.getJSONObject(j);
                            int msgId = replyToUseObject.getInt("id");
                            String msg = replyToUseObject.getString("msg");
                            String mimeType = replyToUseObject.getString("mimeType");
                            String dateTime2 = replyToUseObject.getString("dateTime");
                            String extraMessage = "";
                            try {
                                extraMessage = replyToUseObject.getString("extraMsg");
                            } catch (Exception e) {
                            }

                            int nextMsgDelay = replyToUseObject.getInt("nextMsgDelay");
                            int read2 = replyToUseObject.getInt("read");
                            int sent2 = replyToUseObject.getInt("sent");

                            UserBotMsg userBotMsg = new UserBotMsg(msgId, msg, mimeType, extraMessage, dateTime2, nextMsgDelay, read2, sent2, "free", 2);
                            replyToUserList.add(userBotMsg);
                        }


                        questionWithAns = new UserQuestionWithAns(question, answersList, dateTime, action, read, sent, reply, replyToUserList);
                    }

                    ArrayList<String> contentImages = new ArrayList<>();
                    ChatItem_ModelClass user = new ChatItem_ModelClass(id, userName, gender, age, country, contentImages, users, answerRate, userProfile, containsQuestion, recommendationType, userBotMsgList, questionWithAns);
                    userList.add(user);

                }
                sendDataToRecyclerview();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void sendDataToRecyclerview() {


        userListTemp = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context);
        recyclerview.setLayoutManager(layoutManager);
        adapter = new MessengeItemsAdapter(userListTemp, context, adapter, recyclerview);

        recyclerview.setAdapter(adapter);

        if (retreive_sharedPreferences(context)) {

            adapter = new MessengeItemsAdapter(userListTemp, context, adapter, recyclerview);
            recyclerview.setAdapter(adapter);

            int temp = 0;
            for (int i = userListTemp.size(); i < userList.size(); i++) {
                int delayTime = temp * 180000;
                temp++;
                int finalI = i;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userListTemp.add(0, userList.get(finalI));
                        adapter.notifyItemInserted(0);

                        String activityName = MessengeItemsAdapter.getCurrentlyRunningActivity(context);
                        if (activityName != null && activityName.equals("com.bhola.livevideochat.MainActivity")) {
                            playSentAudio();
                        }


                    }
                }, delayTime);
            }

            return;
        }


        for (int i = 0; i < 3; i++) {

            int finalI = i;
            int delayTime = finalI * 20000;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userListTemp.add(0, userList.get(finalI));
                    adapter.notifyItemInserted(0);

                    String activityName = MessengeItemsAdapter.getCurrentlyRunningActivity(context);
                    if (activityName != null && activityName.equals("com.bhola.livevideochat.MainActivity")) {
                        playSentAudio();
                    }

                }
            }, delayTime);
        }


    }

    public static void playSentAudio() {

        boolean foregroud = false;
        try {
            foregroud = new ForegroundCheckTask().execute(adapter.context).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!foregroud) {
            return;
        }
        MediaPlayer mediaPlayer = MediaPlayer.create(adapter.context, R.raw.message_received);
        mediaPlayer.start();
    }

    public static void save_sharedPrefrence(Context context, ArrayList<ChatItem_ModelClass> userList) {


        SharedPreferences sharedPreferences = context.getSharedPreferences("messenger_chats", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


// Convert the ArrayList to JSON string
        Gson gson = new Gson();
        String json = gson.toJson(userList);

// Save the JSON string to SharedPreferences
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {
            editor.putString("userListTemp_Google", json);
        } else {
            editor.putString("userListTemp_Guest", json);
        }
        editor.apply();

    }

    public static boolean retreive_sharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("messenger_chats", MODE_PRIVATE);

// Retrieve the JSON string from SharedPreferences
        String json = "";
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {
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

    public static void updateUnreadmessageCount(Context context) {

        if (userListTemp == null) {
            return;
        }
        int count = 0;

        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {

            ChatItem_ModelClass modelclass = Fragment_Messenger.userListTemp.get(i);

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

        MainActivity.badge_text.setText(String.valueOf(count));
        MainActivity.badge_text.setVisibility(View.VISIBLE);
        MainActivity.badge_text.setBackgroundResource(R.drawable.badge_background);
        MainActivity.unreadMessage_count = count;

        if (count == 0) {
            MainActivity.badge_text.setVisibility(View.GONE);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        updateUnreadmessageCount(context);
    }

}


class MessengeItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;

    ArrayList<ChatItem_ModelClass> userList;
    MessengeItemsAdapter adapter;
    RecyclerView recyclerview;


    public MessengeItemsAdapter(ArrayList<ChatItem_ModelClass> userList, Context context, MessengeItemsAdapter adapter, RecyclerView recyclerview) {
        this.userList = userList;
        this.context = context;
        this.adapter = adapter;
        this.recyclerview = recyclerview;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View UserItem_Viewholder = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items_recyclerview, parent, false);
        return new UserItem_Viewholder(UserItem_Viewholder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        UserItem_Viewholder userItem_viewholder = (UserItem_Viewholder) holder;
        ChatItem_ModelClass modelClass = userList.get(position);


        userItem_viewholder.userName.setText(modelClass.getUserName());
        userItem_viewholder.recommendationType.setText(modelClass.getRecommendationType());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        Date currentTime = new Date();
        String formattedTime = dateFormat.format(currentTime);
        userItem_viewholder.messageTime.setText(formattedTime);

        Picasso.get().load(modelClass.getUserProfile()).into(userItem_viewholder.profileUrl);

        userItem_viewholder.chatItemClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userItem_viewholder.messageCount.setVisibility(View.GONE);
                    }
                }, 500);

                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", modelClass.getUserName());
                editor.apply(); // Apply the changes to SharedPreferences

                Intent intent = new Intent(context, ChatScreen_User.class);
                context.startActivity(intent);
            }
        });

        if (modelClass.isContainsQuestion()) {

            isContainQuestion(modelClass, userItem_viewholder, holder, currentTime);  //method to handler it chat is question

        } else {

            for (int i = 0; i < modelClass.getUserBotMsg().size() - 1; i++) {

                if (modelClass.getUserBotMsg().get(i).getSent() == 0) {


                    userItem_viewholder.lastMessage.setText(modelClass.getUserBotMsg().get(i).getMsg());
                    modelClass.getUserBotMsg().get(i).setSent(1);
                    modelClass.getUserBotMsg().get(i).setDateTime(String.valueOf(currentTime.getTime()));

                    setMessageCount(modelClass.getUserBotMsg(), userItem_viewholder.messageCount, modelClass.getUserName());//set messageCount method
                    Fragment_Messenger.save_sharedPrefrence(context, userList);
                    Fragment_Messenger.updateUnreadmessageCount(context);


                    Random random = new Random();
                    int randomNumber = random.nextInt(1001) + 50; // Generate a random number between 0 and 5000, then add 50
                    int nextMegDelay = modelClass.getUserBotMsg().get(i).getNextMsgDelay() + randomNumber;

                    Handler handler = new Handler();
                    Runnable updateTimeRunnable = new Runnable() {
                        @Override
                        public void run() {


                            int time = 0;
                            if (holder.getAbsoluteAdapterPosition() == -1) {
                                time = 100000;
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int currentPostion = -1;
                                    for (int j = 0; j < userList.size(); j++) {
                                        if (userList.get(j).getUserName().equals(modelClass.getUserName())) {
                                            currentPostion = j;
                                        }
                                    }
                                    userList.remove(currentPostion);
                                    userList.add(0, modelClass);
                                    notifyItemMoved(currentPostion, 0);
                                    notifyItemChanged(0);
                                    recyclerview.smoothScrollToPosition(0);
                                    Fragment_Messenger.updateUnreadmessageCount(context);


                                    String activityName = MessengeItemsAdapter.getCurrentlyRunningActivity(context);
                                    if (activityName != null && activityName.equals("com.bhola.livevideochat.MainActivity")) {
                                        Fragment_Messenger.playSentAudio();
                                    }
                                    if (activityName != null && activityName.equals("com.bhola.livevideochat.ChatScreen_User")) {
                                        if (Fragment_Messenger.currentActiveUser.equals(modelClass.getUserName())) {
                                            Fragment_Messenger.playSentAudio();
                                        }
                                    }
                                }
                            }, time);


                        }
                    };
                    handler.postDelayed(updateTimeRunnable, nextMegDelay);


                    break;
                }


                if (i == modelClass.getUserBotMsg().size() - 2) { //last loop
                    userItem_viewholder.lastMessage.setText(modelClass.getUserBotMsg().get(i + 1).getMsg());
                    modelClass.getUserBotMsg().get(modelClass.getUserBotMsg().size() - 1).setSent(1);
                    modelClass.getUserBotMsg().get(modelClass.getUserBotMsg().size() - 1).setDateTime(String.valueOf(currentTime.getTime()));
                    Fragment_Messenger.save_sharedPrefrence(context, userList);
                    Fragment_Messenger.updateUnreadmessageCount(context);
                    setMessageCount(modelClass.getUserBotMsg(), userItem_viewholder.messageCount, modelClass.getUserName());//set messageCount
                }
            }
        }
    }

    private void isContainQuestion(ChatItem_ModelClass modelClass, UserItem_Viewholder userItem_viewholder, RecyclerView.ViewHolder holder, Date currentTime) {


        UserQuestionWithAns userQuestionWithAns = modelClass.getQuestionWithAns();

        if (userQuestionWithAns.getReply().length() != 0) {
            int messageIndex = -1;
            for (int i = 0; i < userQuestionWithAns.getReplyToUser().size(); i++) {
                UserBotMsg userBotMsg = userQuestionWithAns.getReplyToUser().get(i);

                int delayTime = (i + 1) * userBotMsg.getNextMsgDelay();
                if (userBotMsg.getSent() == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            int time = 0;
                            if (holder.getAbsoluteAdapterPosition() == -1) {
                                time = 100000;
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    userBotMsg.setSent(1);
                                    userBotMsg.setDateTime(String.valueOf(currentTime.getTime()));


                                    int currentPostion = -1;
                                    for (int j = 0; j < userList.size(); j++) {
                                        if (userList.get(j).getUserName().equals(modelClass.getUserName())) {
                                            currentPostion = j;
                                        }
                                    }

                                    userList.remove(currentPostion);
                                    userList.add(0, modelClass);
                                    notifyItemMoved(currentPostion, 0);
                                    notifyItemChanged(0);

                                    recyclerview.smoothScrollToPosition(0);
                                    Fragment_Messenger.updateUnreadmessageCount(context);
                                    Fragment_Messenger.playSentAudio();
                                }
                            }, time);


                        }
                    }, delayTime);
                } else {

                    messageIndex = i;
                }
            }
            if (messageIndex != -1) {
                userItem_viewholder.lastMessage.setText(userQuestionWithAns.getReplyToUser().get(messageIndex).getMsg());
            } else {
                userItem_viewholder.lastMessage.setText(userQuestionWithAns.getReply());

            }
        } else {

            userItem_viewholder.lastMessage.setText(userQuestionWithAns.getQuestion());
            userQuestionWithAns.setSent(1);
            userQuestionWithAns.setDateTime(String.valueOf(currentTime.getTime()));
        }

        setMessageCountQuestion(modelClass.getQuestionWithAns(), userItem_viewholder.messageCount, modelClass.getUserName());//set messageCount method
        Fragment_Messenger.save_sharedPrefrence(context, userList);
        Fragment_Messenger.updateUnreadmessageCount(context);

    }

    private void setMessageCount(ArrayList<UserBotMsg> userBotMsg, TextView messageCount, String userName) {

        String activityName = getCurrentlyRunningActivity(context);
        if (Fragment_Messenger.currentActiveUser.equals(userName) && activityName != null && activityName.equals("com.bhola.livevideochat.ChatScreen_User")) {
            messageCount.setVisibility(View.GONE);
            return; //this is because when we are in the chat screem and pressed back the message count is shwoing which should not show
        }


        messageCount.setVisibility(View.VISIBLE);

        int count = 0;
        for (int i = 0; i < userBotMsg.size(); i++) {
            if (userBotMsg.get(i).getSent() == 1 && userBotMsg.get(i).getRead() == 0) {
                count = count + 1;
            }
        }
        messageCount.setText(String.valueOf(count));
        if (count == 0) {
            messageCount.setVisibility(View.GONE);
        }

    }

    private void setMessageCountQuestion(UserQuestionWithAns userQuestionWithAns, TextView messageCount, String userName) {

        String activityName = getCurrentlyRunningActivity(context);
        if (Fragment_Messenger.currentActiveUser.equals(userName) && activityName != null && activityName.equals("com.bhola.livevideochat.ChatScreen_User")) {
            messageCount.setVisibility(View.GONE);
            return; //this is because when we are in the chat screem and pressed back the message count is shwoing which should not show
        }

        messageCount.setVisibility(View.VISIBLE);

        int count = 0;
        if (userQuestionWithAns.getSent() == 1 && userQuestionWithAns.getRead() == 0) {
            count = count + 1;
        }

        for (int i = 0; i < userQuestionWithAns.getReplyToUser().size(); i++) {
            UserBotMsg userBotMsg = userQuestionWithAns.getReplyToUser().get(i);
            if (userBotMsg.getSent() == 1 && userBotMsg.getRead() == 0) {
                count = count + 1;
            }
        }
        messageCount.setText(String.valueOf(count));
        if (count == 0) {
            messageCount.setVisibility(View.GONE);
        }

    }


    public static String getCurrentlyRunningActivity(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();

        // Get the list of running tasks
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);

        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            ActivityManager.RunningTaskInfo taskInfo = runningTaskInfos.get(0);

            // Check if the top activity in the task matches the package name of your app
            if (taskInfo.topActivity != null && taskInfo.topActivity.getPackageName().equals(packageName)) {
                return taskInfo.topActivity.getClassName();
            }
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public static class UserItem_Viewholder extends RecyclerView.ViewHolder {

        TextView userName, recommendationType, lastMessage, messageTime, messageCount;
        CircleImageView profileUrl;
        LinearLayout chatItemClick;
        CardView recommendationTypeCardview;

        public UserItem_Viewholder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            recommendationType = itemView.findViewById(R.id.recommendationType);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageCount = itemView.findViewById(R.id.messageCount);
            profileUrl = itemView.findViewById(R.id.profileUrl);
            chatItemClick = itemView.findViewById(R.id.chatItemClick);
            recommendationTypeCardview = itemView.findViewById(R.id.recommendationTypeCardview);

        }
    }
}


class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Context... params) {
        final Context context = params[0].getApplicationContext();
        return isAppOnForeground(context);
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}

