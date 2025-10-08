package com.example.evmobileapp.operator

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.auth.LoginActivity
import com.example.evmobileapp.owner.BookingHistoryActivity
import com.example.evmobileapp.operator.OperatorDashboardActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OperatorProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var ivAppLogo: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_profile)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        ivAppLogo = findViewById(R.id.iv_app_logo)
        tvName = findViewById(R.id.tv_name)
        tvEmail = findViewById(R.id.tv_email)
        tvRole = findViewById(R.id.tv_role)
        tvStatus = findViewById(R.id.tv_status)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)
        bottomNavigation = findViewById(R.id.bottom_navigation_profile)

        setupBottomNavigation()
        setupButtons()

        val token = sessionManager.getToken()
        if (token != null) {
            userId = getUserIdFromToken(token) ?: ""
            if (userId.isNotEmpty()) {
                fetchProfileData(token)
            } else {
                Toast.makeText(this, "Invalid token. Please login again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No token found. Please login.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserIdFromToken(token: String): String? {
        return try {
            // Remove Bearer prefix if present
            val cleanToken = token.replace("Bearer ", "")
            val parts = cleanToken.split("\\.".toRegex())
            if (parts.size != 3) return null

            // Decode payload (base64)
            val payload = parts[1]
            val normalizedPayload = payload.padEnd((payload.length / 4) * 4, '=')
            val decodedBytes = Base64.decode(normalizedPayload, Base64.URL_SAFE)
            val jsonPayload = String(decodedBytes)

            // Log for debugging (remove after fixing)
            Log.d("TokenDecode", "Full payload: $jsonPayload")

            // Parse JSON to get nameid (from ClaimTypes.NameIdentifier) or fallback to sub/userId
            val jsonObject = JSONObject(jsonPayload)
            jsonObject.optString("nameid", null) ?:
            jsonObject.optString("sub", null) ?:
            jsonObject.optString("userId", null)
        } catch (e: Exception) {
            Log.e("TokenDecode", "Error decoding token", e)
            null
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_station -> {
                    startActivity(Intent(this, OperatorDashboardActivity::class.java))
                    true
                }
                R.id.nav_bookings_station -> {
                    startActivity(Intent(this, BookingOperatorActivity::class.java))
                    true
                }
                R.id.nav_profile_station -> {
                    // Already on profile
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_profile_station
    }

    private fun setupButtons() {
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile_operator, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)

        // Pre-fill current value
        etName.setText(tvName.text.toString().trim())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()

                if (name.isNotEmpty()) {
                    val token = sessionManager.getToken()
                    if (token != null && userId.isNotEmpty()) {
                        updateProfile(token, userId, name)
                    } else {
                        Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill the name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateProfile(token: String, userId: String, name: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("name", name)
        }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/users/$userId")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@OperatorProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        fetchProfileData(token) // Refetch to update UI
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@OperatorProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OperatorProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchProfileData(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/users/$userId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    runOnUiThread {
                        tvName.text = jsonResponse.optString("name", "N/A")
                        tvEmail.text = jsonResponse.optString("email", "N/A")
                        tvRole.text = jsonResponse.optString("role", "N/A")
                        tvStatus.text = jsonResponse.optString("status", "N/A").capitalize()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@OperatorProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OperatorProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}