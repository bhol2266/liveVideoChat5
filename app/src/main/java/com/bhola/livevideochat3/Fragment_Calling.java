package com.bhola.livevideochat3;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

public class Fragment_Calling extends Fragment {


    View view;
    Context context;
    String name;
    MediaPlayer mediaPlayer;
    int videoView_Height;
    ImageView endcall;
    private MediaPlayer mediaPlayer2;
    private Ringtone defaultRingtone;
    Handler mHandler;
    Runnable mRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Bundle args = getArguments();
        if (args != null) {
            name = args.getString("name");
        }

        view = inflater.inflate(R.layout.fragment_calling, container, false);
        context = getActivity();

        endcall = view.findViewById(R.id.end_call);
        endcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(Fragment_Calling.this).commit();
                }

            }
        });
        LottieAnimationView calling_lottie = view.findViewById(R.id.calling_lottie);
        calling_lottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mHandler.hasCallbacks(mRunnable)) {
                        mHandler.removeCallbacks(mRunnable);
                    }
                }
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(Fragment_Calling.this).commit();
                }
                context.startActivity(new Intent(context, VipMembership.class));

            }
        });
        init();

        endCallAutomatically();

        return view;
    }

    private void endCallAutomatically() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    endcall.performClick();
                } catch (Exception e) {
                }
            }
        }, 15000);
    }

    private void init() {

        try {
            startRingTone();
        } catch (Exception e) {
        }


        TextView profileName = view.findViewById(R.id.profileName);
        profileName.setText(name);

        TextView message = view.findViewById(R.id.message);
        message.setText(name.substring(0, name.indexOf(" ")) + " invites you for a video call");

        FrameLayout playerLayout = view.findViewById(R.id.fragment_container);
        playerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int videoViewHeight = playerLayout.getHeight();
                int videoViewWidth = playerLayout.getWidth();

                // Remove the listener to avoid redundant calls
                playerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                videoView_Height = videoViewHeight;

            }
        });

        VideoView videoView = view.findViewById(R.id.videoView);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        String videoPath = MyApplication.databaseURL_video + "DesiChatVideos/" + name + ".mp4";

        Log.d("sdafsda", "videoPath: " + videoPath);

        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);
        videoView.setBackgroundColor(getResources().getColor(R.color.color_333333));
        videoView.setBackgroundColor(0xFF333333);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mediaPlayer = mp;
                mp.setVolume(0f, 0f);
                mp.setLooping(true);
                mp.seekTo(20000); // 20 seconds in milliseconds

                int viewHeight = videoView.getHeight();


                float scale = (float) videoView_Height / viewHeight;

                videoView.setScaleY(scale);
                videoView.setScaleX(scale);


                videoView.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        videoView.setBackgroundResource(android.R.color.transparent);
                    }
                }, 1500);


            }
        });


    }

    private void startRingTone() {

        // Get the default ringtone
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);

        // Set up MediaPlayer to play the ringtone
        mediaPlayer2 = MediaPlayer.create(context, defaultRingtoneUri);
        mediaPlayer2.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());

        if (mediaPlayer2 != null && !mediaPlayer2.isPlaying()) {
            mediaPlayer2.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaPlayer2 != null) {
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }
}