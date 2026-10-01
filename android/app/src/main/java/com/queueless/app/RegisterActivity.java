package com.queueless.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.queueless.app.api.ApiService;
import com.queueless.app.api.RegisterRequest;
import com.queueless.app.api.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;

    private RadioGroup roleRadioGroup;
    private RadioButton customerRadioButton;
    private RadioButton staffRadioButton;

    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        nameEditText =
                findViewById(R.id.nameEditText);

        emailEditText =
                findViewById(R.id.emailEditText);

        passwordEditText =
                findViewById(R.id.passwordEditText);

        roleRadioGroup =
                findViewById(R.id.roleRadioGroup);

        customerRadioButton =
                findViewById(R.id.customerRadioButton);

        staffRadioButton =
                findViewById(R.id.staffRadioButton);

        registerButton =
                findViewById(R.id.registerButton);

        registerButton.setOnClickListener(
                view -> register()
        );
    }

    private void register() {

        String name =
                nameEditText
                        .getText()
                        .toString()
                        .trim();

        String email =
                emailEditText
                        .getText()
                        .toString()
                        .trim();

        String password =
                passwordEditText
                        .getText()
                        .toString();

        if (name.isEmpty()
                || email.isEmpty()
                || password.isEmpty()) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String role;

        if (customerRadioButton.isChecked()) {
            role = "CUSTOMER";
        } else {
            role = "STAFF";
        }

        RegisterRequest request =
                new RegisterRequest(
                        name,
                        email,
                        password,
                        role
                );

        ApiService apiService =
                RetrofitClient
                        .getInstance(RegisterActivity.this)
                        .create(ApiService.class);

        apiService.register(request)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent =
                                    new Intent(
                                            RegisterActivity.this,
                                            MainActivity.class
                                    );

                            startActivity(intent);

                            finish();

                        } else {

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration failed: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}