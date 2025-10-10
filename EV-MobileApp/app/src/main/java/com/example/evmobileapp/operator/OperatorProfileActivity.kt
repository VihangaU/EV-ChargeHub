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
import com.example.evmobileapp.operator.BookingOperatorActivity
import com.example.evmobileapp.operator.OperatorDashboardActivity
import com.example.evmobileapp.data.Repositories
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

        initViews()
        setupBottomNavigation()
        setupButtons()
        loadProfileData()
    }

    private fun initViews() {
        tvName = findViewById(R.id.tv_name)
        tvEmail = findViewById(R.id.tv_email)
        tvRole = findViewById(R.id.tv_role)
        tvStatus = findViewById(R.id.tv_status)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)
        bottomNavigation = findViewById(R.id.bottom_navigation_profile)
    }

    private fun loadProfileData() {
        val token = sessionManager.getToken()
        if (token != null) {
            userId = getUserIdFromToken(token) ?: ""
            if (userId.isNotEmpty()) {
                fetchProfileData(token)
            } else {
                showError("Invalid token. Please login again.")
            }
        } else {
            showError("No token found. Please login.")
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
                R.id.nav_profile_station -> true
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
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile_operator, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)

        // Pre-fill current value
        etName.setText(tvName.text.toString().trim())

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    val token = sessionManager.getToken()
                    if (token != null && userId.isNotEmpty()) {
                        updateProfile(token, userId, name)
                    } else {
                        showError("User data not available")
                    }
                } else {
                    showError("Please enter your name")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getUserIdFromToken(token: String): String? {
        return try {
            val cleanToken = token.replace("Bearer ", "")
            val parts = cleanToken.split("\\.".toRegex())
            if (parts.size != 3) return null

            val payload = parts[1]
            val normalizedPayload = payload.padEnd((payload.length / 4) * 4, '=')
            val decodedBytes = Base64.decode(normalizedPayload, Base64.URL_SAFE)
            val jsonPayload = String(decodedBytes)

            val jsonObject = JSONObject(jsonPayload)
            jsonObject.optString("nameid", null) ?:
            jsonObject.optString("sub", null) ?:
            jsonObject.optString("userId", null)
        } catch (e: Exception) {
            Log.e("TokenDecode", "Error decoding token", e)
            null
        }
    }

    private fun updateProfile(token: String, userId: String, name: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply { put("name", name) }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/users/$userId")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@OperatorProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        fetchProfileData(token)
                    } else {
                        showError("Failed to update profile")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showError("Network error occurred")
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
                        tvRole.text = formatRole(jsonResponse.optString("role", "N/A"))
                        tvStatus.text = formatStatus(jsonResponse.optString("status", "N/A"))
                    }
                } else {
                    runOnUiThread {
                        showError("Failed to fetch profile data")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showError("Network error occurred")
                }
            }
        })
    }

    private fun formatRole(role: String): String {
        return when (role.lowercase()) {
            "station_operator" -> "Station Operator"
            "ev_owner" -> "EV Owner"
            "backoffice" -> "Back Office"
            else -> role.replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatStatus(status: String): String {
        return status.replaceFirstChar { it.uppercase() }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}