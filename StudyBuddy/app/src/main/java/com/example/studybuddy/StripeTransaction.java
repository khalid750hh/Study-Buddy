package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class StripeTransaction extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private static final String BACKEND_URL = "http://10.0.2.2:4242/";
    private OkHttpClient httpClient = new OkHttpClient();

    private String paymentIntentClientSecret;
    private Stripe stripe;

    private boolean paid;

    private DatabaseReference userAccess;

    private Map<String, String> responseMap;


    private int amount;
    private String id;
    private String email;
    private String name;
    private String photo;
    private String chatID;
    private String chatName;
    private String otherUserId;
    private String paymentId;
    private String priceAsString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_page);

        if(paid){
            finish();
        }


        Intent extras = getIntent();
        amount = extras.getIntExtra("price", 100);
        id = extras.getStringExtra(getString(R.string.user_id));
        email = extras.getStringExtra(getString(R.string.user_email));
        name = extras.getStringExtra(getString(R.string.user_name));
        photo = extras.getStringExtra(getString(R.string.user_photo));
        chatID = extras.getStringExtra(getString(R.string.chat_id));
        chatName = extras.getStringExtra(getString(R.string.chat_name));
        otherUserId = extras.getStringExtra(getString(R.string.other_user_id));
        priceAsString = extras.getStringExtra(getString(R.string.price_as_string));

        userAccess = FirebaseDatabase.getInstance().getReference().child(getString(R.string.access)).child(chatID).child(id);

        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51JwUnKCK34XwtTfpQ2DgtHw7ClqfJTCKz9D4Sx5yd3f6M4r3X0WZjZAz0GvtZZ3WSQQBujxrlVQDW4d3Avd59ML600pcRnIpUT")
        );

        startCheckout();
    }

    private void startCheckout() {
        //Create a PaymentIntent by calling the server's endpoint.
        //We will obtain client secret with this function
        //Client secret is required for authorized transactions
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        Map<String,Object> payMap= new HashMap<>();
        Map<String,Object> itemMap= new HashMap<>();
        List<Map<String,Object>> itemList = new ArrayList<>();
        payMap.put("currency","usd");
        itemMap.put("id", "photo_subscription");
        itemMap.put("amount", amount);
        itemList.add(itemMap);
        payMap.put("items", itemList);
        String json = new Gson().toJson(payMap);

        //Send this json to server
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        //Once we have the client secret, we can make the transaction
        Button payButton = findViewById(R.id.pay_button);
        payButton.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe.confirmPayment(this, confirmParams);
            }
        });
    }

    //Upon completed transaction, whether or not successful
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }


    private static final class PayCallback implements Callback {
        @NonNull
        private final WeakReference<StripeTransaction> activityRef;

        private PayCallback(@NonNull StripeTransaction activity) {
            this.activityRef = new WeakReference<>(activity);
        }
        //This will handle errors with the request to server
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final StripeTransaction activity = activityRef.get();
            if (activity == null) {
                return;
            }

            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Error: " + e.toString(), Toast.LENGTH_LONG)
                            .show()
            );
        }

        //
        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final StripeTransaction activity = activityRef.get();
            if (activity == null) {
                return;
            }
            //If payment is response contains error
            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Error: " + response.toString(), Toast.LENGTH_LONG)
                                .show());
            } else {
                //Continue with payment transaction
                activity.onPaymentSuccess(response);

            }
        }
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        //Get client secret, which is the key used for authorized transactions through stripe
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(), type
        );
        paymentIntentClientSecret = responseMap.get("clientSecret");

    }

    private final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull
        private final WeakReference<StripeTransaction> activityRef;

        PaymentResultCallback(@NonNull StripeTransaction activity) {
            activityRef = new WeakReference<>(activity);
        }

        //On successful payment
        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final StripeTransaction activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                activity.displayAlert("Payment completed",
//                        gson.toJson(paymentIntent)
//                );
                paid = true;

                userAccess.setValue(new StudentUser(photo, name, email, id));

                DatabaseReference userPayment = FirebaseDatabase.getInstance()
                        .getReference().child("payments").child(otherUserId).push();

                HashMap<String, String> payment = new HashMap<String, String>();
                payment.put("price", priceAsString);
                payment.put("payer", id);
                payment.put("reciever", otherUserId);
                payment.put("payment_id", paymentIntent.getId());

                userPayment.setValue(payment);


                Intent goToChat = new Intent(getApplicationContext(), MainActivityChat.class);
                goToChat.putExtra(getString(R.string.user_id), id);
                goToChat.putExtra(getString(R.string.user_name), name);
                goToChat.putExtra(getString(R.string.user_email), email);
                goToChat.putExtra(getString(R.string.user_photo), photo);
                goToChat.putExtra(getString(R.string.chat_id), chatID);
                goToChat.putExtra(getString(R.string.chat_name), chatName);

                startActivity(goToChat);
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert("Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).toString()
                        //Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMesssage()
                );
            }
        }

        //On failed payment
        @Override
        public void onError (@NonNull Exception e){
            final StripeTransaction activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString());
        }
    }

    //Will show the transaction results as a popup
    private void displayAlert(@NonNull String title, @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        //Click "Ok" to exit popup
        builder.setPositiveButton("Ok",null);
        builder.create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(paid){
            finish();
        }
    }
}