package com.bhola.livevideochat2;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VipMembership extends AppCompatActivity {


    AlertDialog dialog;
    private BillingClient billingClient;
    LinearLayout progressBar;
    TextView buyNowTimer, offerTimer;

    private BroadcastReceiver timerUpdateReceiver, timerUpdateReceiverCheck;
    private boolean isTimerRunning = false;
    int backpressCount = 0;
    ArrayList<ProductDetails> mlist_offer;
    final int[] selectedCard = {-1};
    Button btnContinue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_membership);
        actionBar();
        progressBar = findViewById(R.id.progressBar);
        offerTimer = findViewById(R.id.offerTimer);

        addUnderlineTerms_privacy();
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {

                    //first this is triggerd than onResume is called

                    verifyPurchase(purchase);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Payment failed. If your money is deducted, it will be reflected back to your Bank Account soon", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });

                            View snackbarView = snackbar.getView();
                            AppCompatTextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setMaxLines(5); // Adjust this value as needed

//                            snackbar.show();
                            progressBar.setVisibility(View.GONE);

                            startActivity(new Intent(VipMembership.this, SplashScreen.class));

                        }
                    }, 5000);


                }
            } else {
                // Handle any other error codes.
                Toast.makeText(this, "Something went wrong try again!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

            }

        }).build();


        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                //start the connection after initializing the billing client
                connectGooglePlayBilling();
            }
        });
        checkTimeRunning();


    }


    private void checkTimeRunning() {
        isTimerRunning = isServiceRunning(TimerService.class);
        if (isTimerRunning) {
            backpressCount = 1;
            timerUpdateReceiverCheck = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long remainingTime = intent.getLongExtra("remainingTime", 0);
                    updateTimerTextView(remainingTime);
                }
            };


            IntentFilter filter = new IntentFilter();
            filter.addAction("timer-update");
            filter.addAction("timer-finish");

            registerReceiver(timerUpdateReceiverCheck, filter);

        }


    }

    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (isTimerRunning) {
                        getProductDetails("with offer");
                    } else {
                        getProductDetails("no offer");
                    }
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private void getProductDetails(String offer) {

        List<String> productIds = new ArrayList<>();
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();

        productIds.add("coins200");
        productIds.add("coins500");
        productIds.add("coins3001");
        productIds.add("coins200_offer");
        productIds.add("coins500_offer");
        productIds.add("coins3001_offer");

// Add more product IDs as needed

        QueryProductDetailsParams.Builder queryBuilder = QueryProductDetailsParams.newBuilder();

        for (String productId : productIds) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.INAPP).build();
            list.add(product);
        }
        queryBuilder.setProductList(list);


        QueryProductDetailsParams queryProductDetailsParams = queryBuilder.build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                // Handle the product details response for multiple products

                ((Activity) VipMembership.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selectCardView(productDetailsList, offer);


                    }
                });
            }
        });


    }


    private void verifyPurchase(Purchase purchase) {
        int coins = 0;
        String inputString = purchase.getProducts().get(0);
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(inputString);
        while (matcher.find()) {
            String number = matcher.group();
            coins = Integer.parseInt(number);
        }


        savePurchaseDetails_inSharedPreference(purchase.getPurchaseToken(), coins, purchase.getPurchaseTime());

        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(SplashScreen.TAG, "Consumed: ");
                }
            }
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentRef = db.collection("purchases").document(purchase.getPurchaseToken());

        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
//                    ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
//                    billingClient.consumeAsync(
//                            consumeParams,
//                            new ConsumeResponseListener() {
//                                @Override
//                                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
//                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                                        Log.d("TAGAA", "onConsumeResponse: ");
//                                    }
//                                }
//                            }
//                    );
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("purchaseToken", purchase.getPurchaseToken());
                    data.put("purchaseTime", purchase.getPurchaseTime());
                    data.put("orderId", purchase.getOrderId());
                    data.put("date", new Date());
                    data.put("isValid", true);

                    documentRef.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Data successfully written to Firestore
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error writing data to Firestore
                        }
                    });


                    // Document doesn't exist
                }
            } else {
                // Error occurred while retrieving the document
                FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
                // Handle the exception
                Log.d(SplashScreen.TAG, "FirebaseFirestoreException: " + exception.getMessage());
            }
        });


    }

    private void savePurchaseDetails_inSharedPreference(String purchaseToken, int coins, long purchaseTime) {
        //Reading purchase Token
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String a = sh.getString("purchaseToken", purchaseToken);

        // Creating purchase Token into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("purchaseToken", purchaseToken);
        myEdit.putInt("coins", coins);

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(currentDate);
        myEdit.putString("purchase_date", dateString);
        myEdit.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(BillingClient.ProductType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase purchase : list) {
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                            verifyPurchase(purchase);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);

                                }
                            }, 5000);

                        }
                    }
                }
            }
        });
    }


    private void exit_dialog() {

        if (mlist_offer == null || mlist_offer.size() == 0) {
            return;
        }
        getProductDetails("with offer");
        AlertDialog dialog;

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VipMembership.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_membership_exit, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        LottieAnimationView lottie = promptView.findViewById(R.id.lottie);
        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lottie.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        buyNowTimer = promptView.findViewById(R.id.buyNowTimer);


        dialog = builder.create();
        dialog.show();

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        dialog.getWindow().setBackgroundDrawable(inset);


        if (isTimerRunning) {
            backpressCount = 1;
        } else {
            // Timer is not running, start the service
            startOfferTimer();
        }

        ProductDetails productDetails = mlist_offer.get(0);
        LinearLayout clickForPayment = promptView.findViewById(R.id.clickForPayment);
        TextView buyNowTimer = promptView.findViewById(R.id.buyNowTimer);
        TextView price = promptView.findViewById(R.id.price);
        TextView productName = promptView.findViewById(R.id.productName);

        productName.setText(productDetails.getTitle());
        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
        buyNowTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        clickForPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });


    }

    private void startOfferTimer() {


        timerUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long remainingTime = intent.getLongExtra("remainingTime", 0);
                updateTimerTextView(remainingTime);
            }
        };


        IntentFilter filter = new IntentFilter();
        filter.addAction("timer-update");
        filter.addAction("timer-finish");

        registerReceiver(timerUpdateReceiver, filter);

        Intent intent = new Intent(this, TimerService.class);
        startService(intent);

    }


    private void updateTimerTextView(long remainingTime) {

        long minutes = (remainingTime / (1000 * 60)) % 60;
        long seconds = (remainingTime / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);

        if (timeLeftFormatted.equals("00:10")) {
            offerTimer.setText("Timer Finished!");
            offerTimer.setVisibility(View.GONE);
            unregisterReceiver(timerUpdateReceiver);
        }

        if (!isTimerRunning) {
            buyNowTimer.setText("BUY NOW " + timeLeftFormatted.toString());
        }
        offerTimer.setText("Offer ends in " + timeLeftFormatted);
        offerTimer.setVisibility(View.VISIBLE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void actionBar() {

        ImageView back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
                startActivity(new Intent(VipMembership.this, Terms_Conditions.class));
            }
        });
        privaciy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VipMembership.this, PrivacyPolicy.class));

            }
        });
    }


    private void selectCardView(List<ProductDetails> productDetailsList, String offer) {


        LinearLayout cardsLayout = findViewById(R.id.cardsLayout);
        cardsLayout.setVisibility(View.VISIBLE);

        CardView cardView1, cardView2, cardView3;
        TextView price1, price2, price3, mrp1, mrp2, mrp3;
        cardView1 = findViewById(R.id.cardView1);
        cardView2 = findViewById(R.id.cardView2);
        cardView3 = findViewById(R.id.cardView3);

        price1 = findViewById(R.id.price1);
        price2 = findViewById(R.id.price2);
        price3 = findViewById(R.id.price3);

        mrp1 = findViewById(R.id.mrp1);
        mrp2 = findViewById(R.id.mrp2);
        mrp3 = findViewById(R.id.mrp3);


        ArrayList<ProductDetails> mlist = new ArrayList<ProductDetails>();
        mlist_offer = new ArrayList<ProductDetails>();

        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("coins200")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("coins200_offer")) {
                mlist_offer.add(productDetails);
            }

        }
        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("coins500")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("coins500_offer")) {
                mlist_offer.add(productDetails);
            }

        }
        for (ProductDetails productDetails : productDetailsList) {
            if (productDetails.getProductId().equals("coins3001")) {
                mlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("coins3001_offer")) {
                mlist_offer.add(productDetails);
            }

        }

        if (offer.equals("with offer")) {

            TextView discountRate1 = findViewById(R.id.discountRate1);
            TextView discountRate2 = findViewById(R.id.discountRate2);
            TextView discountRate3 = findViewById(R.id.discountRate3);

            discountRate1.setVisibility(View.VISIBLE);
            discountRate2.setVisibility(View.VISIBLE);
            discountRate3.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < mlist.size(); i++) {
            ProductDetails productDetails;
            if (offer.equals("with offer")) {
                productDetails = mlist_offer.get(i);
            } else {
                productDetails = mlist.get(i);

            }

            if (i == 0) {

                price1.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                mrp1.setText(mlist.get(0).getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                mrp1.setPaintFlags(mrp1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                if (offer.equals("with offer")) {
                    mrp1.setVisibility(View.VISIBLE);
                }
            }
            if (i == 1) {

                price2.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                mrp2.setText(mlist.get(1).getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                mrp2.setPaintFlags(mrp2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                if (offer.equals("with offer")) {
                    mrp2.setVisibility(View.VISIBLE);
                }
            }
            if (i == 2) {

                price3.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                mrp3.setText(mlist.get(2).getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                mrp3.setPaintFlags(mrp3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                if (offer.equals("with offer")) {
                    mrp3.setVisibility(View.VISIBLE);
                }
            }

        }


        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCard[0] == -1) {
                    Toast.makeText(VipMembership.this, "Please select any card", Toast.LENGTH_SHORT).show();
                } else {
                    ProductDetails finalProductDetails;
                    if (offer.equals("with offer")) {
                        finalProductDetails = mlist_offer.get(selectedCard[0]);
                    } else {
                        finalProductDetails = mlist.get(selectedCard[0]);

                    }
                    // An activity reference from which the billing flow will be launched.
                    Activity activity = VipMembership.this;

                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(finalProductDetails)
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    billingClient.launchBillingFlow(activity, billingFlowParams);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });


        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOtherCards_BackgroundWhite(cardView1, cardView2, cardView3, price1, price2, price3, mrp1, mrp2, mrp3);
                int backgroundColor = R.color.themeColor;
                int color = ContextCompat.getColor(VipMembership.this, backgroundColor);
                cardView1.setCardBackgroundColor(color);

                int textColor = Color.parseColor("#FFFFFF"); // Replace "#FF0000" with your desired color
                price1.setTextColor(textColor);
                mrp1.setTextColor(textColor);

                selectedCard[0] = 0;


            }
        });
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOtherCards_BackgroundWhite(cardView1, cardView2, cardView3, price1, price2, price3, mrp1, mrp2, mrp3);

                int backgroundColor = R.color.themeColor;
                int color = ContextCompat.getColor(VipMembership.this, backgroundColor);
                cardView2.setCardBackgroundColor(color);

                int textColor = Color.parseColor("#FFFFFF"); // Replace "#FF0000" with your desired color
                price2.setTextColor(textColor);
                mrp2.setTextColor(textColor);

                selectedCard[0] = 1;


            }
        });

        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOtherCards_BackgroundWhite(cardView1, cardView2, cardView3, price1, price2, price3, mrp1, mrp2, mrp3);

                int backgroundColor = R.color.themeColor;
                int color = ContextCompat.getColor(VipMembership.this, backgroundColor);
                cardView3.setCardBackgroundColor(color);

                int textColor = Color.parseColor("#FFFFFF"); // Replace "#FF0000" with your desired color
                price3.setTextColor(textColor);
                mrp3.setTextColor(textColor);


                selectedCard[0] = 2;


            }
        });


    }

    private void setOtherCards_BackgroundWhite(CardView cardView1, CardView cardView2, CardView cardView3, TextView price1, TextView price2, TextView price3, TextView mrp1, TextView mrp2, TextView mrp3) {
        int backgroundColor = R.color.white;
        int color = ContextCompat.getColor(VipMembership.this, backgroundColor);
        cardView1.setCardBackgroundColor(color);
        cardView2.setCardBackgroundColor(color);
        cardView3.setCardBackgroundColor(color);

        int textColor = Color.parseColor("#000000"); // Replace "#FF0000" with your desired color
        int textColor2 = Color.parseColor("#BF707070"); // Replace "#FF0000" with your desired color
        price1.setTextColor(textColor);
        price2.setTextColor(textColor);
        price3.setTextColor(textColor);

        mrp1.setTextColor(textColor2);
        mrp2.setTextColor(textColor2);
        mrp3.setTextColor(textColor2);
    }

    @Override
    public void onBackPressed() {
        Log.d(SplashScreen.TAG, "backpressCount: " + backpressCount);
        if (backpressCount == 0) {
            exit_dialog();
            backpressCount++;
        } else {
            super.onBackPressed();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerUpdateReceiver != null) {
            unregisterReceiver(timerUpdateReceiver);
        }
        if (timerUpdateReceiverCheck != null) {
            unregisterReceiver(timerUpdateReceiverCheck);
        }
    }


}