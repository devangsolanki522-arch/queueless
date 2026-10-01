package com.queueless.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.queueless.app.api.ApiService;
import com.queueless.app.api.LoginRequest;
import com.queueless.app.api.LoginResponse;
import com.queueless.app.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private Button loginButton;
    private Button registerButton;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager =
                new SessionManager(MainActivity.this);

        String token = sessionManager.getToken();
        String role = sessionManager.getRole();

        if (token != null
                && !token.isEmpty()
                && role != null
                && !role.isEmpty()) {

            openDashboard(role);
            return;
        }

        setContentView(R.layout.activity_main);

        emailEditText =
                findViewById(R.id.emailEditText);

        passwordEditText =
                findViewById(R.id.passwordEditText);

        loginButton =
                findViewById(R.id.loginButton);

        registerButton =
                findViewById(R.id.registerButton);

        loginButton.setOnClickListener(
                view -> login()
        );

        registerButton.setOnClickListener(
                view -> {
                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    RegisterActivity.class
                            );

                    startActivity(intent);
                }
        );
    }

    private void login() {

        String email =
                emailEditText
                        .getText()
                        .toString()
                        .trim();

        String password =
                passwordEditText
                        .getText()
                        .toString();

        if (email.isEmpty()
                || password.isEmpty()) {

            Toast.makeText(
                    MainActivity.this,
                    "Enter email and password",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LoginRequest request =
                new LoginRequest(
                        email,
                        password
                );

        ApiService apiService =
                RetrofitClient
                        .getInstance(MainActivity.this)
                        .create(ApiService.class);

        apiService.login(request)
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(
                            Call<LoginResponse> call,
                            Response<LoginResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            LoginResponse loginResponse =
                                    response.body();

                            sessionManager.saveToken(
                                    loginResponse.getToken()
                            );

                            sessionManager.saveRole(
                                    loginResponse.getRole()
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    "Login successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            openDashboard(
                                    loginResponse.getRole()
                            );

                        } else {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Login failed: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<LoginResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void openDashboard(String role) {

        Intent intent;

        if ("STAFF".equals(role)) {

            intent =
                    new Intent(
                            MainActivity.this,
                            StaffActivity.class
                    );

        } else {

            intent =
                    new Intent(
                            MainActivity.this,
                            HomeActivity.class
                    );
        }

        startActivity(intent);
        finish();
    }
}