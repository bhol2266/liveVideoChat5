package com.bhola.livevideochat5;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bhola.livevideochat5.Models.GalleryModel;
import com.bhola.livevideochat5.Models.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

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
                LoginInComplete("Guest", "Guest", "", "");
            }
        });
    }


    private void addUnderlineTerms_privacy() {
        TextView terms = findViewById(R.id.terms);
        TextView privaciy = findViewById(R.id.privacy);
        terms.setPaintFlags(terms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privaciy.setPaintFlags(privaciy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, Terms_Conditions.class));
            }
        });
        privaciy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, PrivacyPolicy.class));

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
                            if (progressDialog != null && progressDialog.isShowing()) {
                                //Crashlytics error
                                progressDialog.cancel();
                            }
                            ArrayList<String> keyword = new ArrayList<>();
                            checkUserExist(account.getEmail(), account.getDisplayName(), account.getPhotoUrl().toString());

                        } else {
                            Toast.makeText(LoginScreen.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (ApiException e) {
                Log.d(MyApplication.TAG, "onActivityResulttt: " + e.getMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkUserExist(String email, String displayName, String picUrl) {
        showLoadingDialog();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

// Create a query to find the user with your email
        Query query = usersRef.whereEqualTo("email", email);


        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No user with the provided email address found and countinue to new login
                        LoginInComplete("Google", displayName, email, picUrl);


                    } else {


                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            MyApplication.userModel = documentSnapshot.toObject(UserModel.class); // Replace User with your actual user model class
                            Utils utils = new Utils();
                            utils.updateDateonFireStore("date", new Date());

                            // Use the user data as needed
                            MyApplication.userLoggedIn = true;
                            MyApplication.userLoggedIAs = MyApplication.userModel.getLoggedAs();

                            Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();

                            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();


                            editor.putString("email", email);
                            editor.putString("photoUrl", MyApplication.userModel.getProfilepic());
                            editor.putString("loginAs", MyApplication.userModel.getLoggedAs());
                            editor.putString("Bio", MyApplication.userModel.getBio());
                            editor.putString("Language", MyApplication.userModel.getLanguage());

                            editor.putString("nickName", MyApplication.userModel.getFullname());
                            editor.putString("Gender", MyApplication.userModel.getSelectedGender());
                            editor.putString("Birthday", MyApplication.userModel.getBirthday());
                            editor.putInt("userId", MyApplication.userModel.getUserId());
                            editor.putInt("coins", MyApplication.userModel.getCoins());
                            editor.apply();

                            Utils.replaceFCMToken();


                            dismissLoadingDialog();
                            Log.d("sdafsdaf", "checkUserExist: "+MyApplication.userModel.getUserId());
                            if (MyApplication.userModel.getGalleryImages().size() > 1) {
                                saveGalleryImages(MyApplication.userModel.getGalleryImages()); // save gallery images to local storeage from firebase storage
                            } else {
                                startActivity(new Intent(LoginScreen.this, MainActivity.class));
                            }

                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the query
                });


    }


    private void saveGalleryImages(final ArrayList<GalleryModel> galleryImages) {

        DownloadImageTask downloadImageTask = new DownloadImageTask(LoginScreen.this);
        downloadImageTask.execute(galleryImages);

    }

    public static void saveGalleryImagesToSharedPreferences(Context context) {
        // This method is called when all images are downloaded.
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        List<GalleryModel> itemList = new ArrayList<>();
        itemList.addAll(MyApplication.userModel.getGalleryImages());

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        editor.putString("galleryImages", json);

        editor.apply(); // Make sure to apply the changes.
    }


    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching details...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    private void LoginInComplete(String loggedAs, String displayName, String email, String photoUrl) {
        //new login
        MyApplication.userLoggedIn = true;
        Log.d("dasfsadf", "LoginInComplete: " + loggedAs);
        MyApplication.userLoggedIAs = loggedAs;
        finish();
        Intent intent = new Intent(LoginScreen.this, Fill_details.class);
        intent.putExtra("loggedAs", loggedAs);
        intent.putExtra("nickName", displayName);
        intent.putExtra("email", email);
        intent.putExtra("photoUrl", photoUrl);
        startActivity(intent);
    }


}


class DownloadImageTask extends AsyncTask<ArrayList<GalleryModel>, Void, Void> {

    private ProgressDialog progressBarDialog;
    private final Context context;

    public DownloadImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(ArrayList<GalleryModel>... arrayLists) {

        ArrayList<GalleryModel> imageList = arrayLists[0];

        for (int i = 1; i < imageList.size(); i++) {
            GalleryModel galleryModel = imageList.get(i);
            try {
                URL url = new URL(galleryModel.getDownloadUrl());
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                File internalStorage = new File(context.getFilesDir(), "images");

                if (!internalStorage.exists()) {
                    internalStorage.mkdir();
                }
                File file = new File(internalStorage, galleryModel.getImageFileNAme());
                Uri imageURI = Uri.fromFile(file);
                MyApplication.userModel.getGalleryImages().get(i).setImage_uri(String.valueOf(imageURI));

                if (file.exists()) file.delete();

                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

            } catch (Exception e) {
                Log.d("TAGA", "doInBackground: " + e.getMessage());
            }
        }


        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBarDialog = new ProgressDialog(context);
        progressBarDialog.setMessage("Downloading user images...");
        progressBarDialog.setCancelable(false);
        progressBarDialog.show();

    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        progressBarDialog.dismiss();
        LoginScreen.saveGalleryImagesToSharedPreferences(context);
        context.startActivity(new Intent(context, MainActivity.class));

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}



