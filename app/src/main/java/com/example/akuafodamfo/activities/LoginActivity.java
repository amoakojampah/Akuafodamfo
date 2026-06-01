package com.example.akuafodamfo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.example.akuafodamfo.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView btnRegisterToggle, btnForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            mAuth = FirebaseAuth.getInstance();
            if (mAuth == null) {
                throw new IllegalStateException("Firebase initialization failed");
            }

            initializeViews();
            checkCurrentUser();
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            showToast("Initialization failed. Please try again later.");
            finish();
        }
    }

    private void initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            btnRegisterToggle = findViewById(R.id.btnRegisterToggle);
            btnForgotPassword = findViewById(R.id.btnForgotPassword);

            if (etEmail == null || etPassword == null || btnLogin == null ||
                    btnRegisterToggle == null || btnForgotPassword == null) {
                throw new IllegalStateException("One or more views failed to initialize");
            }
        } catch (Exception e) {
            Log.e(TAG, "View initialization error", e);
            showToast("UI initialization failed. Please restart the app.");
            finish();
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            navigateToDashboard();
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegisterToggle.setOnClickListener(v -> navigateToRegister());
        btnForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Please fill all fields");
            return;
        }

        showToast("Logging in...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            navigateToDashboard();
                        } else {
                            showToast("Please verify your email first");
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        try {
            throw exception;
        } catch (FirebaseAuthInvalidUserException e) {
            showToast("No account found with this email");
        } catch (FirebaseAuthInvalidCredentialsException e) {
            showToast("Invalid email or password");
        } catch (Exception e) {
            Log.e(TAG, "Login failed", e);
            showToast("Login failed: " + e.getMessage());
        }
    }

    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
        // Don't call finish() here so users can go back to login if needed
    }

    private void navigateToForgotPassword() {
        // Implement forgot password functionality
        showToast("Forgot password clicked");
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}