package com.bhola.livevideochat2;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class Fragment_UserProfile extends Fragment {


    ImageView profileImage;
    TextView name, coins;
    LinearLayout logout;

    public Fragment_UserProfile() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_user__profile, container, false);

        Context context = getContext();

        profileImage = view.findViewById(R.id.profileUrl);
        name = view.findViewById(R.id.profileName);
        coins = view.findViewById(R.id.coins);
        coins.setText(String.valueOf("Coins: "+SplashScreen.coins));
        logout = view.findViewById(R.id.logout);
        SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);


        if (SplashScreen.userLoggedIn) {

            String fullname = sh.getString("name", "not set");
            name.setText(fullname);
            Log.d(SplashScreen.TAG, "onCreateView: "+fullname);

            if (SplashScreen.userLoggedIAs.equals("Google")) {
                String urll = sh.getString("photoUrl", "not set");
                Picasso.get()
                        .load(urll)
                        .into(profileImage);
            }

        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso;
                GoogleSignInClient gsc;
                if (SplashScreen.userLoggedIAs.equals("Google")) {
                    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                    gsc = GoogleSignIn.getClient(context, gso);
                    gsc.signOut().addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            FirebaseAuth.getInstance().signOut();

                            SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString("name", "not set");
                            editor.putString("email", "not set");
                            editor.putString("photoUrl", "not set");
                            editor.putString("loginAs", "not set");
                            editor.apply();

                        }
                    });

                } else {

                    SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sh.edit();
                    editor.putString("name", "not set");
                    editor.putString("age", "not set");
                    editor.putString("gender", "not set");
                    editor.putString("loginAs", "not set");
                    editor.apply();

                

                }






                Toast.makeText(context, "Logged Out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(context, LoginScreen.class));

            }
        });

        LinearLayout memberShip = view.findViewById(R.id.memberShip);
        memberShip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,VipMembership.class ));
            }
        });

        oprnPrivacy_Terms(view,context);

        LinearLayout about=view.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, About.class));
            }
        });

        return view;
    }

    private void oprnPrivacy_Terms(View view, Context context) {
        LinearLayout terms=view.findViewById(R.id.terms);
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,Terms_Conditions.class));
            }
        });

        LinearLayout privacy=view.findViewById(R.id.privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,PrivacyPolicy.class));
            }
        });
    }
}