package com.example.evmobileapp.operator

import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OperatorProfileActivity : AppCompatActivity() {

    private lateinit var repository: Repositories
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

        repository = Repositories(this)

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

        val currentSession = repository.getCurrentSession()
        if (currentSession != null) {
            userId = currentSession.userId ?: ""
            if (userId.isNotEmpty()) {
                fetchProfileData(currentSession.token)
            } else {
                Toast.makeText(this, "Invalid session. Please login again.", Toast.LENGTH_SHORT).show()
                repository.clearAllData()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } else {
            Toast.makeText(this, "No session found. Please login.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
            repository.clearAllData()
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
                    val currentSession = repository.getCurrentSession()
                    if (currentSession != null && userId.isNotEmpty()) {
                        updateProfile(currentSession.token, userId, name)
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
                        val currentSession = repository.getCurrentSession()
                        if (currentSession != null) {
                            fetchProfileData(currentSession.token) // Refetch to update UI
                        }
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