package com.bhola.livevideochat2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class LoginScreen extends AppCompatActivity {

    AlertDialog dialog;


    //Google
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    LinearLayout googleBtn;


    //Credentials
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        fullscreenMode();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        addUnderlineTerms_privacy();
        loginStuffs();

    }

    private void loginStuffs() {
        TextView loginGoogle = findViewById(R.id.loginWithGoogle);
        loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignInStuffs();

            }
        });
        TextView loginGuest = findViewById(R.id.loginAsguest);
        loginGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGuestLoginDialog();
            }
        });
    }

    private void openGuestLoginDialog() {

        TextInputEditText editTextName, editTextAge;
        RadioGroup radioGroupGender;
        TextView buttonSubmit;

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LoginScreen.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_guestlogin, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        editTextName = promptView.findViewById(R.id.editTextName);
        editTextAge = promptView.findViewById(R.id.editTextAge);
        radioGroupGender = promptView.findViewById(R.id.radioGroupGender);
        buttonSubmit = promptView.findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String age = editTextAge.getText().toString();
                String gender = "Male";

//                int selectedId = radioGroupGender.getCheckedRadioButtonId();
//                if (selectedId != -1) {
//                    RadioButton radioButton = findViewById(selectedId);
//                    gender = radioButton.getText().toString();
//                }

                // Display the submitted information
                if (name.length() == 0 || age.length() == 0 || gender.length() == 0) {
                    Toast.makeText(LoginScreen.this, "Enter details", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sh.edit();
                editor.putString("name", name);
                editor.putString("age", age);
                editor.putString("gender", gender);
                editor.putString("loginAs", "Guest");
                editor.apply();
                LoginInComplete("Guest");
            }
        });


        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    }

    private void addUnderlineTerms_privacy() {
        TextView terms = findViewById(R.id.terms);
        TextView privaciy = findViewById(R.id.privacy);
        terms.setPaintFlags(terms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privaciy.setPaintFlags(privaciy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this,Terms_Conditions.class));
            }
        });
        privaciy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this,PrivacyPolicy.class));

            }
        });
    }

    private void fullscreenMode() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }


    private void googleSignInStuffs() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            //Google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.cancel();
                            ArrayList<String> keyword = new ArrayList<>();
                            saveUserdataFireStore(account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString(), false);

                            SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString("name", account.getDisplayName());
                            editor.putString("email", account.getEmail());
                            editor.putString("photoUrl", account.getPhotoUrl().toString());
                            editor.putString("loginAs", "Google");
                            editor.apply();
                            LoginInComplete("Google");

                        } else {
                            Toast.makeText(LoginScreen.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (ApiException e) {
                Log.d(SplashScreen.TAG, "onActivityResult: " + e.getMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void saveUserdataFireStore(String displayName, String email, String profileUrl, boolean membership) {
        firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).set(new UserModel(displayName, email, profileUrl, membership, new java.util.Date(), "not set")).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void LoginInComplete(String loggedAs) {
        SplashScreen.userLoggedIn = true;
        SplashScreen.userLoggedIAs = loggedAs;
        Toast.makeText(this, "Logged In!", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
        startActivity(intent);
    }


}

class UserModel {

    String fullname, email, profilepic;
    boolean membership;
    Date date;
    String memberShipExpiryDate;

    public UserModel(String fullname, String email, String profilepic, boolean membership, Date date, String memberShipExpiryDate) {
        this.fullname = fullname;
        this.email = email;
        this.profilepic = profilepic;
        this.membership = membership;
        this.date = date;
        this.memberShipExpiryDate = memberShipExpiryDate;
    }

    public UserModel() {
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public boolean isMembership() {
        return membership;
    }

    public void setMembership(boolean membership) {
        this.membership = membership;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMemberShipExpiryDate() {
        return memberShipExpiryDate;
    }

    public void setMemberShipExpiryDate(String memberShipExpiryDate) {
        this.memberShipExpiryDate = memberShipExpiryDate;
    }
}
