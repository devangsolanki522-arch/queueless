package com.queueless.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.queueless.app.api.ApiService;
import com.queueless.app.api.PriorityRequest;
import com.queueless.app.api.RetrofitClient;
import com.queueless.app.api.ServiceRequest;
import com.queueless.app.api.ServiceResponse;
import com.queueless.app.api.TokenResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffActivity extends AppCompatActivity {

    private TextView serviceNameTextView;
    private TextView calledTokenTextView;

    private EditText serviceNameEditText;
    private EditText serviceTimeEditText;

    private Button createServiceButton;
    private Button callNextButton;
    private Button startServingButton;
    private Button noShowButton;
    private Button completeButton;
    private Button updatePriorityButton;
    private Button logoutButton;

    private RadioGroup priorityRadioGroup;
    private RadioButton normalPriorityRadioButton;
    private RadioButton highPriorityRadioButton;
    private RadioButton emergencyPriorityRadioButton;

    private LinearLayout waitingQueueContainer;
    private TextView emptyQueueTextView;

    private Long serviceId;
    private Long currentTokenId;

    // Token selected from the WAITING QUEUE.
    private Long selectedWaitingTokenId;

    // ---------------------------------------------------------
    // DYNAMIC QUEUE REFRESH
    // ---------------------------------------------------------

    private final Handler queueHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable queueRefreshRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (serviceId != null) {
                        loadWaitingTokens();
                    }

                    // Refresh every 3 seconds.
                    queueHandler.postDelayed(
                            this,
                            3000
                    );
                }
            };


    // ---------------------------------------------------------
    // ON CREATE
    // ---------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_staff);

        serviceNameTextView =
                findViewById(R.id.serviceNameTextView);

        calledTokenTextView =
                findViewById(R.id.calledTokenTextView);

        serviceNameEditText =
                findViewById(R.id.serviceNameEditText);

        serviceTimeEditText =
                findViewById(R.id.serviceTimeEditText);

        createServiceButton =
                findViewById(R.id.createServiceButton);

        callNextButton =
                findViewById(R.id.callNextButton);

        startServingButton =
                findViewById(R.id.startServingButton);

        noShowButton =
                findViewById(R.id.noShowButton);

        completeButton =
                findViewById(R.id.completeButton);

        updatePriorityButton =
                findViewById(R.id.updatePriorityButton);

        logoutButton =
                findViewById(R.id.logoutButton);

        priorityRadioGroup =
                findViewById(R.id.priorityRadioGroup);

        normalPriorityRadioButton =
                findViewById(R.id.normalPriorityRadioButton);

        highPriorityRadioButton =
                findViewById(R.id.highPriorityRadioButton);

        emergencyPriorityRadioButton =
                findViewById(R.id.emergencyPriorityRadioButton);

        waitingQueueContainer =
                findViewById(R.id.waitingQueueContainer);

        emptyQueueTextView =
                findViewById(R.id.emptyQueueTextView);


        loadService();


        createServiceButton.setOnClickListener(
                view -> createService()
        );


        callNextButton.setOnClickListener(
                view -> callNext()
        );


        startServingButton.setOnClickListener(
                view -> startServing()
        );


        noShowButton.setOnClickListener(
                view -> markNoShow()
        );


        completeButton.setOnClickListener(
                view -> completeToken()
        );


        updatePriorityButton.setOnClickListener(
                view -> updatePriority()
        );


        logoutButton.setOnClickListener(
                view -> logout()
        );
    }


    // ---------------------------------------------------------
    // ACTIVITY LIFECYCLE
    // ---------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        // Immediately refresh when StaffActivity becomes visible.
        if (serviceId != null) {
            loadWaitingTokens();
        }

        // Start automatic queue polling.
        queueHandler.removeCallbacks(
                queueRefreshRunnable
        );

        queueHandler.postDelayed(
                queueRefreshRunnable,
                3000
        );
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Stop polling when StaffActivity is not visible.
        queueHandler.removeCallbacks(
                queueRefreshRunnable
        );
    }


    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    private void logout() {

        SessionManager sessionManager =
                new SessionManager(StaffActivity.this);

        sessionManager.clearSession();

        Intent intent =
                new Intent(
                        StaffActivity.this,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }


    // ---------------------------------------------------------
    // LOAD STAFF SERVICE
    // ---------------------------------------------------------

    private void loadService() {

        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);

        apiService.getMyServices()
                .enqueue(new Callback<List<ServiceResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ServiceResponse>> call,
                            Response<List<ServiceResponse>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            ServiceResponse service =
                                    response.body().get(0);

                            serviceId =
                                    service.getId();

                            serviceNameTextView.setText(
                                    service.getName()
                            );

                            loadWaitingTokens();

                        } else {

                            serviceId = null;

                            serviceNameTextView.setText(
                                    "No service created yet"
                            );

                            clearWaitingQueue();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<List<ServiceResponse>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                StaffActivity.this,
                                "Could not load service: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ---------------------------------------------------------
    // CREATE SERVICE
    // ---------------------------------------------------------

    private void createService() {

        String name =
                serviceNameEditText
                        .getText()
                        .toString()
                        .trim();

        String timeText =
                serviceTimeEditText
                        .getText()
                        .toString()
                        .trim();


        if (name.isEmpty()
                || timeText.isEmpty()) {

            Toast.makeText(
                    StaffActivity.this,
                    "Enter service name and time",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        int estimatedTime;

        try {

            estimatedTime =
                    Integer.parseInt(timeText);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    StaffActivity.this,
                    "Enter a valid time",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (estimatedTime <= 0) {

            Toast.makeText(
                    StaffActivity.this,
                    "Time must be greater than 0",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        ServiceRequest request =
                new ServiceRequest(
                        name,
                        estimatedTime
                );


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.createService(request)
                .enqueue(new Callback<ServiceResponse>() {

                    @Override
                    public void onResponse(
                            Call<ServiceResponse> call,
                            Response<ServiceResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            ServiceResponse service =
                                    response.body();

                            serviceId =
                                    service.getId();

                            serviceNameTextView.setText(
                                    service.getName()
                            );

                            serviceNameEditText
                                    .setText("");

                            serviceTimeEditText
                                    .setText("");

                            selectedWaitingTokenId = null;

                            Toast.makeText(
                                    StaffActivity.this,
                                    "Service created",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadWaitingTokens();

                        } else {

                            Toast.makeText(
                                    StaffActivity.this,
                                    "Could not create service: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<ServiceResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                StaffActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ---------------------------------------------------------
    // WAITING QUEUE
    // ---------------------------------------------------------

    private void loadWaitingTokens() {

        if (serviceId == null) {

            clearWaitingQueue();

            return;
        }


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.getWaitingTokens(serviceId)
                .enqueue(new Callback<List<TokenResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TokenResponse>> call,
                            Response<List<TokenResponse>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            displayWaitingTokens(
                                    response.body()
                            );

                        } else {

                            // Don't show a Toast every 3 seconds.
                            // A temporary refresh failure shouldn't spam
                            // the staff user.

                            if (response.code() == 401
                                    || response.code() == 403) {

                                Toast.makeText(
                                        StaffActivity.this,
                                        "Not authorized to view queue",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<List<TokenResponse>> call,
                            Throwable t
                    ) {

                        // Don't show a Toast every 3 seconds.
                        // Network can temporarily fail and the next
                        // polling cycle will try again.
                    }
                });
    }


    private void displayWaitingTokens(
            List<TokenResponse> tokens
    ) {

        waitingQueueContainer.removeAllViews();


        if (tokens.isEmpty()) {

            selectedWaitingTokenId = null;

            waitingQueueContainer.addView(
                    emptyQueueTextView
            );

            updatePriorityButton.setEnabled(false);

            return;
        }


        boolean selectedTokenStillExists = false;


        for (TokenResponse token : tokens) {

            if (token.getId()
                    .equals(selectedWaitingTokenId)) {

                selectedTokenStillExists = true;

                break;
            }
        }


        if (!selectedTokenStillExists) {

            selectedWaitingTokenId = null;

            updatePriorityButton.setEnabled(false);
        }


        for (TokenResponse token : tokens) {

            LinearLayout tokenCard =
                    createTokenCard(token);

            waitingQueueContainer.addView(
                    tokenCard
            );
        }
    }


    // ---------------------------------------------------------
    // TOKEN CARD
    // ---------------------------------------------------------

    private LinearLayout createTokenCard(
            TokenResponse token
    ) {

        LinearLayout card =
                new LinearLayout(
                        StaffActivity.this
                );


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        card.setPadding(
                20,
                18,
                20,
                18
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


        card.setLayoutParams(cardParams);


        GradientDrawable background =
                new GradientDrawable();


        background.setCornerRadius(18);


        background.setColor(
                Color.rgb(30, 30, 48)
        );


        background.setStroke(
                1,
                Color.rgb(70, 70, 95)
        );


        card.setBackground(background);


        TextView tokenNumberTextView =
                new TextView(
                        StaffActivity.this
                );


        tokenNumberTextView.setText(
                "TOKEN #"
                        + token.getTokenNumber()
        );


        tokenNumberTextView.setTextColor(
                Color.WHITE
        );


        tokenNumberTextView.setTextSize(
                19
        );


        tokenNumberTextView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        TextView priorityTextView =
                new TextView(
                        StaffActivity.this
                );


        priorityTextView.setText(
                "Priority: "
                        + token.getPriority()
        );


        priorityTextView.setTextColor(
                Color.rgb(169, 169, 194)
        );


        priorityTextView.setTextSize(
                14
        );


        LinearLayout.LayoutParams priorityParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        priorityParams.setMargins(
                0,
                6,
                0,
                0
        );


        priorityTextView.setLayoutParams(
                priorityParams
        );


        TextView selectTextView =
                new TextView(
                        StaffActivity.this
                );


        selectTextView.setText(
                "Tap to select"
        );


        selectTextView.setTextColor(
                Color.rgb(130, 130, 170)
        );


        selectTextView.setTextSize(
                12
        );


        LinearLayout.LayoutParams selectParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        selectParams.setMargins(
                0,
                10,
                0,
                0
        );


        selectTextView.setLayoutParams(
                selectParams
        );


        card.addView(
                tokenNumberTextView
        );


        card.addView(
                priorityTextView
        );


        card.addView(
                selectTextView
        );


        if (token.getId()
                .equals(selectedWaitingTokenId)) {

            background.setStroke(
                    2,
                    Color.rgb(103, 103, 255)
            );


            selectTextView.setText(
                    "SELECTED"
            );


            selectTextView.setTextColor(
                    Color.rgb(140, 140, 255)
            );
        }


        card.setOnClickListener(
                view -> selectWaitingToken(
                        token
                )
        );


        return card;
    }


    // ---------------------------------------------------------
    // SELECT WAITING TOKEN
    // ---------------------------------------------------------

    private void selectWaitingToken(
            TokenResponse token
    ) {

        selectedWaitingTokenId =
                token.getId();


        updatePriorityButton.setEnabled(
                true
        );


        setPriorityRadioButtons(
                token.getPriority()
        );


        // Redraw the cards so the selected card
        // gets the SELECTED appearance.
        displayWaitingTokens(
                java.util.Collections.singletonList(token)
        );

        // Reload the actual queue immediately after selection.
        loadWaitingTokens();
    }


    private void setPriorityRadioButtons(
            String priority
    ) {

        if (priority == null) {

            normalPriorityRadioButton.setChecked(
                    true
            );

            return;
        }


        switch (priority) {

            case "HIGH":

                highPriorityRadioButton.setChecked(
                        true
                );

                break;


            case "EMERGENCY":

                emergencyPriorityRadioButton.setChecked(
                        true
                );

                break;


            case "NORMAL":

            default:

                normalPriorityRadioButton.setChecked(
                        true
                );

                break;
        }
    }


    // ---------------------------------------------------------
    // CLEAR WAITING QUEUE
    // ---------------------------------------------------------

    private void clearWaitingQueue() {

        waitingQueueContainer.removeAllViews();

        selectedWaitingTokenId = null;

        updatePriorityButton.setEnabled(false);

        waitingQueueContainer.addView(
                emptyQueueTextView
        );
    }


    // ---------------------------------------------------------
    // CALL NEXT
    // ---------------------------------------------------------

    private void callNext() {

        if (serviceId == null) {

            Toast.makeText(
                    StaffActivity.this,
                    "Create or load a service first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.callNext(serviceId)
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


                            selectedWaitingTokenId =
                                    null;


                            showToken(token);


                            startServingButton
                                    .setEnabled(true);


                            noShowButton
                                    .setEnabled(true);


                            completeButton
                                    .setEnabled(false);


                            updatePriorityButton
                                    .setEnabled(false);


                            loadWaitingTokens();


                            Toast.makeText(
                                    StaffActivity.this,
                                    "Token #"
                                            + token.getTokenNumber()
                                            + " called",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    StaffActivity.this,
                                    "Could not call next: "
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
                                StaffActivity.this,
                                "Queue error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ---------------------------------------------------------
    // START SERVING
    // ---------------------------------------------------------

    private void startServing() {

        if (currentTokenId == null) {
            return;
        }


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.startServing(currentTokenId)
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


                            showToken(token);


                            startServingButton
                                    .setEnabled(false);


                            noShowButton
                                    .setEnabled(false);


                            completeButton
                                    .setEnabled(true);


                            updatePriorityButton
                                    .setEnabled(false);

                        } else {

                            Toast.makeText(
                                    StaffActivity.this,
                                    "Could not start serving: "
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
                                StaffActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ---------------------------------------------------------
    // NO SHOW
    // ---------------------------------------------------------

    private void markNoShow() {

        if (currentTokenId == null) {
            return;
        }


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.markNoShow(currentTokenId)
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


                            showToken(token);


                            startServingButton
                                    .setEnabled(false);


                            noShowButton
                                    .setEnabled(false);


                            completeButton
                                    .setEnabled(false);


                            updatePriorityButton
                                    .setEnabled(false);


                            currentTokenId = null;


                            loadWaitingTokens();


                            Toast.makeText(
                                    StaffActivity.this,
                                    "Customer marked as NO_SHOW",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    StaffActivity.this,
                                    "Could not mark NO_SHOW: "
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
                                StaffActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ---------------------------------------------------------
    // COMPLETE TOKEN
    // ---------------------------------------------------------

    private void completeToken() {

        if (currentTokenId == null) {
            return;
        }


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.completeToken(currentTokenId)
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


                            showToken(token);


                            startServingButton
                                    .setEnabled(false);


                            noShowButton
                                    .setEnabled(false);


                            completeButton
                                    .setEnabled(false);


                            updatePriorityButton
                                    .setEnabled(false);


                            currentTokenId = null;


                            loadWaitingTokens();


                            Toast.makeText(
                                    StaffActivity.this,
                                    "Token completed",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    StaffActivity.this,
                                    "Could not complete token: "
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
                                StaffActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ---------------------------------------------------------
    // UPDATE PRIORITY
    // ---------------------------------------------------------

    private void updatePriority() {

        if (selectedWaitingTokenId == null) {

            Toast.makeText(
                    StaffActivity.this,
                    "Select a waiting token first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String priority;


        if (emergencyPriorityRadioButton.isChecked()) {

            priority = "EMERGENCY";

        } else if (highPriorityRadioButton.isChecked()) {

            priority = "HIGH";

        } else {

            priority = "NORMAL";
        }


        PriorityRequest request =
                new PriorityRequest(priority);


        ApiService apiService =
                RetrofitClient
                        .getInstance(StaffActivity.this)
                        .create(ApiService.class);


        apiService.changePriority(
                selectedWaitingTokenId,
                request
        ).enqueue(new Callback<TokenResponse>() {

            @Override
            public void onResponse(
                    Call<TokenResponse> call,
                    Response<TokenResponse> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    TokenResponse token =
                            response.body();


                    Toast.makeText(
                            StaffActivity.this,
                            "Priority updated to "
                                    + token.getPriority(),
                            Toast.LENGTH_SHORT
                    ).show();


                    loadWaitingTokens();

                } else {

                    Toast.makeText(
                            StaffActivity.this,
                            "Could not update priority: "
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
                        StaffActivity.this,
                        "Network error: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


    // ---------------------------------------------------------
    // CURRENT TOKEN DISPLAY
    // ---------------------------------------------------------

    private void showToken(
            TokenResponse token
    ) {

        calledTokenTextView.setText(
                "Current Token\n\n#"
                        + token.getTokenNumber()
                        + "\n\nPriority: "
                        + token.getPriority()
                        + "\n\nStatus: "
                        + token.getStatus()
        );
    }
}