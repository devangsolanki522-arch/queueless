package com.queueless.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.queueless.app.api.ApiService;
import com.queueless.app.api.QueueTokenResponse;
import com.queueless.app.api.RetrofitClient;
import com.queueless.app.api.ServiceResponse;
import com.queueless.app.api.TokenRequest;
import com.queueless.app.api.TokenResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout servicesContainer;
    private TextView myQueueTextView;

    private Button logoutButton;
    private Button cancelQueueButton;
    private Button refreshQueueButton;

    private Long currentTokenId;

    // Automatically refresh services every 5 seconds.
    private final Handler serviceRefreshHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable serviceRefreshRunnable =
            new Runnable() {
                @Override
                public void run() {

                    loadServices();

                    serviceRefreshHandler.postDelayed(
                            this,
                            5000
                    );
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        servicesContainer =
                findViewById(R.id.servicesContainer);

        myQueueTextView =
                findViewById(R.id.myQueueTextView);

        logoutButton =
                findViewById(R.id.logoutButton);

        cancelQueueButton =
                findViewById(R.id.cancelQueueButton);

        refreshQueueButton =
                findViewById(R.id.refreshQueueButton);

        logoutButton.setOnClickListener(
                view -> logout()
        );

        cancelQueueButton.setOnClickListener(
                view -> cancelQueue()
        );

        refreshQueueButton.setOnClickListener(
                view -> refreshQueue()
        );

        loadServices();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start automatic service refreshing
        // whenever the customer is actively viewing this screen.
        serviceRefreshHandler.postDelayed(
                serviceRefreshRunnable,
                5000
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop automatic refreshing when the screen
        // is no longer visible.
        serviceRefreshHandler.removeCallbacks(
                serviceRefreshRunnable
        );
    }

    private void logout() {

        // Stop background service refreshing before leaving.
        serviceRefreshHandler.removeCallbacks(
                serviceRefreshRunnable
        );

        SessionManager sessionManager =
                new SessionManager(HomeActivity.this);

        sessionManager.clearSession();

        Intent intent =
                new Intent(
                        HomeActivity.this,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void loadServices() {

        ApiService apiService =
                RetrofitClient
                        .getInstance(HomeActivity.this)
                        .create(ApiService.class);

        apiService.getServices()
                .enqueue(new Callback<List<ServiceResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ServiceResponse>> call,
                            Response<List<ServiceResponse>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            displayServices(
                                    response.body()
                            );

                        } else {

                            // Do not show an error Toast for every
                            // automatic refresh.
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ServiceResponse>> call,
                            Throwable t
                    ) {

                        // Automatic refresh should fail silently.
                    }
                });
    }

    private void displayServices(
            List<ServiceResponse> services
    ) {

        servicesContainer.removeAllViews();

        if (services.isEmpty()) {

            TextView emptyText =
                    new TextView(this);

            emptyText.setText(
                    "No services available right now."
            );

            emptyText.setTextColor(
                    Color.rgb(150, 150, 170)
            );

            emptyText.setTextSize(15);

            servicesContainer.addView(
                    emptyText
            );

            return;
        }

        for (ServiceResponse service : services) {

            LinearLayout serviceCard =
                    new LinearLayout(this);

            serviceCard.setOrientation(
                    LinearLayout.VERTICAL
            );

            serviceCard.setPadding(
                    20,
                    20,
                    20,
                    20
            );

            GradientDrawable cardBackground =
                    new GradientDrawable();

            cardBackground.setColor(
                    Color.rgb(15, 15, 22)
            );

            cardBackground.setStroke(
                    1,
                    Color.rgb(52, 52, 70)
            );

            cardBackground.setCornerRadius(
                    24
            );

            serviceCard.setBackground(
                    cardBackground
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(
                    0,
                    0,
                    0,
                    12
            );

            serviceCard.setLayoutParams(
                    cardParams
            );

            // Service name
            TextView serviceName =
                    new TextView(this);

            serviceName.setText(
                    service.getName()
            );

            serviceName.setTextColor(
                    Color.WHITE
            );

            serviceName.setTextSize(19);

            serviceName.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            // Service time
            TextView serviceTime =
                    new TextView(this);

            serviceTime.setText(
                    "Estimated time  •  "
                            + service.getEstimatedServiceTime()
                            + " min"
            );

            serviceTime.setTextColor(
                    Color.rgb(145, 145, 165)
            );

            serviceTime.setTextSize(13);

            LinearLayout.LayoutParams timeParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            timeParams.setMargins(
                    0,
                    5,
                    0,
                    15
            );

            serviceTime.setLayoutParams(
                    timeParams
            );

            // Join button
            Button joinButton =
                    new Button(this);

            joinButton.setText(
                    "JOIN QUEUE   →"
            );

            joinButton.setTextColor(
                    Color.WHITE
            );

            joinButton.setTextSize(12);

            joinButton.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            GradientDrawable buttonBackground =
                    new GradientDrawable();

            buttonBackground.setColor(
                    Color.rgb(73, 61, 170)
            );

            buttonBackground.setCornerRadius(
                    28
            );

            joinButton.setBackground(
                    buttonBackground
            );

            joinButton.setOnClickListener(
                    view -> joinQueue(
                            service.getId()
                    )
            );

            serviceCard.addView(
                    serviceName
            );

            serviceCard.addView(
                    serviceTime
            );

            serviceCard.addView(
                    joinButton
            );

            servicesContainer.addView(
                    serviceCard
            );
        }
    }

    private void joinQueue(Long serviceId) {

        TokenRequest request =
                new TokenRequest(
                        serviceId
                );

        ApiService apiService =
                RetrofitClient
                        .getInstance(HomeActivity.this)
                        .create(ApiService.class);

        apiService.createToken(request)
                .enqueue(new Callback<TokenResponse>() {

                    @Override
                    public void onResponse(
                            Call<TokenResponse> call,
                            Response<TokenResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            TokenResponse token =
                                    response.body();

                            currentTokenId =
                                    token.getId();

                            cancelQueueButton
                                    .setEnabled(true);

                            refreshQueueButton
                                    .setEnabled(true);

                            Toast.makeText(
                                    HomeActivity.this,
                                    "Queue joined! Token: #"
                                            + token.getTokenNumber(),
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadQueuePosition(
                                    token.getId()
                            );

                        } else {

                            Toast.makeText(
                                    HomeActivity.this,
                                    "Could not join queue: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TokenResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                HomeActivity.this,
                                "Queue error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void refreshQueue() {

        if (currentTokenId == null) {

            Toast.makeText(
                    HomeActivity.this,
                    "No active queue",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        loadQueuePosition(
                currentTokenId
        );
    }

    private void loadQueuePosition(
            Long tokenId
    ) {

        ApiService apiService =
                RetrofitClient
                        .getInstance(HomeActivity.this)
                        .create(ApiService.class);

        apiService.getQueuePosition(tokenId)
                .enqueue(
                        new Callback<QueueTokenResponse>() {

                            @Override
                            public void onResponse(
                                    Call<QueueTokenResponse> call,
                                    Response<QueueTokenResponse> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    showQueueInfo(
                                            response.body()
                                    );

                                } else {

                                    Toast.makeText(
                                            HomeActivity.this,
                                            "Queue API failed: HTTP "
                                                    + response.code(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<QueueTokenResponse> call,
                                    Throwable t
                            ) {

                                Toast.makeText(
                                        HomeActivity.this,
                                        "Queue API error: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void showQueueInfo(
            QueueTokenResponse queue
    ) {

        String queueInfo =
                "TOKEN  #"
                        + queue.getTokenNumber()
                        + "\n\n"
                        + "PRIORITY  "
                        + queue.getPriority()
                        + "\n\n"
                        + "POSITION  "
                        + queue.getPosition()
                        + "\n\n"
                        + "ESTIMATED WAIT  "
                        + queue.getEstimatedWaitTime()
                        + " min"
                        + "\n\n"
                        + "STATUS  "
                        + queue.getStatus();

        myQueueTextView.setText(
                queueInfo
        );

        refreshQueueButton.setEnabled(
                true
        );

        if ("WAITING".equals(
                queue.getStatus()
        )) {

            cancelQueueButton.setEnabled(
                    true
            );

        } else {

            cancelQueueButton.setEnabled(
                    false
            );
        }
    }

    private void cancelQueue() {

        if (currentTokenId == null) {

            Toast.makeText(
                    HomeActivity.this,
                    "No active queue",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ApiService apiService =
                RetrofitClient
                        .getInstance(HomeActivity.this)
                        .create(ApiService.class);

        apiService.cancelToken(
                currentTokenId
        ).enqueue(
                new Callback<TokenResponse>() {

                    @Override
                    public void onResponse(
                            Call<TokenResponse> call,
                            Response<TokenResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            TokenResponse token =
                                    response.body();

                            myQueueTextView.setText(
                                    "TOKEN  #"
                                            + token.getTokenNumber()
                                            + "\n\n"
                                            + "PRIORITY  "
                                            + token.getPriority()
                                            + "\n\n"
                                            + "STATUS  "
                                            + token.getStatus()
                            );

                            cancelQueueButton
                                    .setEnabled(false);

                            refreshQueueButton
                                    .setEnabled(false);

                            currentTokenId = null;

                            Toast.makeText(
                                    HomeActivity.this,
                                    "Queue cancelled",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    HomeActivity.this,
                                    "Could not cancel queue: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TokenResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                HomeActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}