package com.example.evmobileapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView // 1. Import TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.owner.OwnerDashboardActivity
import com.example.evmobileapp.operator.OperatorDashboardActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)
        // 2. Find the new TextView by its ID
        val signupLink: TextView = findViewById(R.id.signup_link)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        loginButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            if (emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                loginUser(emailText, passwordText)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Set an OnClickListener for the sign-up link
        signupLink.setOnClickListener {
            // Create an Intent to start SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/auth/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody!!)
                    val token = jsonResponse.getString("token")
                    val role = jsonResponse.getJSONObject("user").getString("role")
                    sessionManager.saveToken(token)

                    runOnUiThread {
                        when (role) {
                            "ev_owner" -> {
                                startActivity(Intent(this@LoginActivity, OwnerDashboardActivity::class.java))
                            }
                            "station_operator" -> {
                                startActivity(Intent(this@LoginActivity, OperatorDashboardActivity::class.java))
                            }
                            else -> {
                                Toast.makeText(this@LoginActivity, "Unsupported user role: $role", Toast.LENGTH_SHORT).show()
                            }
                        }
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}