package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var signInButton: Button

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        roleSpinner = findViewById(R.id.roleSpinner)
        registerButton = findViewById(R.id.registerButton)
        signInButton = findViewById(R.id.loginButton)

        val roles = arrayOf("Select Role", "Admin", "Vendor", "Customer")
        roleSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        setupPasswordToggle()

        signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || role == "Select Role") {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordError = validatePassword(password)
            if (passwordError != null) {
                Toast.makeText(this, passwordError, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser!!.uid
                    val newUser = User(userId, email, role.lowercase())

                    database.child(userId).setValue(newUser).addOnSuccessListener {
                        mAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                            Toast.makeText(this, "Verification email sent! Please verify.", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            startActivity(Intent(this, SignInActivity::class.java))
                            finish()
                        }?.addOnFailureListener {
                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupPasswordToggle() {
        passwordField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = passwordField.compoundDrawables[2]
                if (drawableRight != null && event.rawX >= (passwordField.right - drawableRight.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        passwordField.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0)
                    } else {
                        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0)
                    }
                    passwordField.setSelection(passwordField.text.length)

                    passwordField.performClick() // accessibility fix

                    return@setOnTouchListener true
                }
            }
            false
        }
    }



    private fun validatePassword(password: String): String? {
        if (password.length < 6) {
            return "Password must be at least 6 characters."
        }
        if (!password.any { it.isUpperCase() }) {
            return "Password must contain an uppercase letter."
        }
        if (!password.any { it.isDigit() }) {
            return "Password must contain a number."
        }
        if (!password.any { "!@#\$%^&*()_+-=[]|,./?><".contains(it) }) {
            return "Password must contain a special character."
        }
        return null
    }
}
