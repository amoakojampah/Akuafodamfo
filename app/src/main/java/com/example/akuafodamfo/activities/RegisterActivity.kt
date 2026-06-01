package com.example.akuafodamfo.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akuafodamfo.R
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var etEmail: TextInputEditText? = null
    private var etPassword: TextInputEditText? = null
    private var etName: TextInputEditText? = null
    private var etFarmLocation: TextInputEditText? = null
    private var etCropType: TextInputEditText? = null
    private var btnRegister: Button? = null
    private var btnLoginToggle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        try {
            mAuth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            check(!(mAuth == null || db == null)) { "Firebase initialization failed" }

            initializeViews()
            setupClickListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error", e)
            showToast("Initialization failed. Please try again later.")
            finish()
        }
    }

    private fun initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            etName = findViewById<TextInputEditText>(R.id.etName)
            etFarmLocation = findViewById<TextInputEditText>(R.id.etFarmLocation)
            etCropType = findViewById<TextInputEditText>(R.id.etCropType)
            btnRegister = findViewById<Button>(R.id.btnRegister)
            btnLoginToggle = findViewById<TextView>(R.id.btnLoginToggle)

            check(!(etEmail == null || etPassword == null || etName == null || etFarmLocation == null || etCropType == null || btnRegister == null || btnLoginToggle == null)) { "One or more views failed to initialize" }
        } catch (e: Exception) {
            Log.e(TAG, "View initialization error", e)
            showToast("UI initialization failed. Please restart the app.")
            finish()
        }
    }

    private fun setupClickListeners() {
        btnRegister!!.setOnClickListener { v: View? -> registerUser() }
        btnLoginToggle!!.setOnClickListener { v: View? -> navigateToLogin() }
    }

    private fun registerUser() {
        val email = etEmail!!.text.toString().trim { it <= ' ' }
        val password = etPassword!!.text.toString().trim { it <= ' ' }
        val name = etName!!.text.toString().trim { it <= ' ' }
        val farmLocation = etFarmLocation!!.text.toString().trim { it <= ' ' }
        val cropType = etCropType!!.text.toString().trim { it <= ' ' }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
            TextUtils.isEmpty(name) || TextUtils.isEmpty(farmLocation) ||
            TextUtils.isEmpty(cropType)
        ) {
            showToast("Please fill all fields")
            return
        }

        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return
        }

        showToast("Registering...")
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    if (user != null) {
                        saveUserData(user.uid, name, email, farmLocation, cropType)
                    }
                } else {
                    handleRegistrationError(task.exception!!)
                }
            }
    }

    private fun handleRegistrationError(exception: Exception) {
        try {
            throw exception
        } catch (e: FirebaseAuthWeakPasswordException) {
            showToast("Weak password: " + e.reason)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            showToast("Invalid email format")
        } catch (e: FirebaseAuthUserCollisionException) {
            showToast("Account already exists")
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            showToast("Registration failed: " + e.message)
        }
    }

    private fun saveUserData(
        userId: String, name: String, email: String,
        farmLocation: String, cropType: String,
    ) {
        val user: MutableMap<String, Any> = HashMap()
        user["name"] = name
        user["email"] = email
        user["farmLocation"] = farmLocation
        user["cropType"] = cropType

        db!!.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener { aVoid: Void? ->
                showToast("Registration successful. Please verify your email.")
                navigateToLogin()
            }
            .addOnFailureListener { e: Exception? ->
                Log.e(TAG, "Failed to save user data", e)
                showToast("Registration complete but profile setup failed.")
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}