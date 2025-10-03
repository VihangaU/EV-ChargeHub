package com.example.evmobileapp.owner

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
import com.example.evmobileapp.owner.BookingHistoryActivity
import com.example.evmobileapp.owner.OwnerDashboardActivity
import com.example.evmobileapp.owner.ReservationActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var ivAppLogo: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvNic: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvVehicleModel: TextView
    private lateinit var tvVehicleNumber: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnDeactivate: MaterialButton
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private var evOwnerId: String = ""
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        ivAppLogo = findViewById(R.id.iv_app_logo)
        tvName = findViewById(R.id.tv_name)
        tvNic = findViewById(R.id.tv_nic)
        tvEmail = findViewById(R.id.tv_email)
        tvPhone = findViewById(R.id.tv_phone)
        tvAddress = findViewById(R.id.tv_address)
        tvVehicleModel = findViewById(R.id.tv_vehicle_model)
        tvVehicleNumber = findViewById(R.id.tv_vehicle_number)
        tvStatus = findViewById(R.id.tv_status)
        btnDeactivate = findViewById(R.id.btn_deactivate)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)
        bottomNavigation = findViewById(R.id.bottom_navigation_profile)

        setupBottomNavigation()
        setupButtons()

        val token = sessionManager.getToken()
        if (token != null) {
            fetchProfileData(token)
        } else {
            Toast.makeText(this, "No token found. Please login.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, OwnerDashboardActivity::class.java))
                    true
                }
                R.id.nav_reservations -> {
                    startActivity(Intent(this, ReservationActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, BookingHistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile, do nothing
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_profile
    }

    private fun setupButtons() {
        btnDeactivate.setOnClickListener {
            showDeactivateConfirmation()
        }
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val etPhone = dialogView.findViewById<EditText>(R.id.et_phone)
        val etAddress = dialogView.findViewById<EditText>(R.id.et_address)
        val etVehicleModel = dialogView.findViewById<EditText>(R.id.et_vehicle_model)
        val etVehicleNumber = dialogView.findViewById<EditText>(R.id.et_vehicle_number)

        // Pre-fill current values (strip labels if present)
        etPhone.setText(tvPhone.text.toString().replace("Phone: ", "").trim())
        etAddress.setText(tvAddress.text.toString().replace("Address: ", "").trim())
        etVehicleModel.setText(tvVehicleModel.text.toString().replace("Vehicle Model: ", "").trim())
        etVehicleNumber.setText(tvVehicleNumber.text.toString().replace("Vehicle Number: ", "").trim())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val phone = etPhone.text.toString().trim()
                val address = etAddress.text.toString().trim()
                val vehicleModel = etVehicleModel.text.toString().trim()
                val vehicleNumber = etVehicleNumber.text.toString().trim()

                if (phone.isNotEmpty() && address.isNotEmpty() && vehicleModel.isNotEmpty() && vehicleNumber.isNotEmpty()) {
                    val token = sessionManager.getToken()
                    if (token != null && evOwnerId.isNotEmpty()) {
                        updateProfile(token, evOwnerId, phone, address, vehicleModel, vehicleNumber)
                    } else {
                        Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateProfile(token: String, evOwnerId: String, phone: String, address: String, vehicleModel: String, vehicleNumber: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("phone", phone)
            put("address", address)
            put("vehicleModel", vehicleModel)
            put("vehicleNumber", vehicleNumber)
        }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/evowners/$evOwnerId")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        fetchProfileData(token) // Refetch to update UI
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showDeactivateConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? This action cannot be undone.")
            .setPositiveButton("Deactivate") { _, _ ->
                val token = sessionManager.getToken()
                if (token != null && evOwnerId.isNotEmpty() && userId.isNotEmpty()) {
                    deactivateAccount(token, evOwnerId, userId)
                } else {
                    Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deactivateAccount(token: String, evOwnerId: String, userId: String) {
        // Update EVOwner status
        val jsonEV = JSONObject().apply {
            put("status", "inactive")
        }
        val bodyEV = jsonEV.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val requestEV = Request.Builder()
            .url("http://10.0.2.2:5001/api/evowners/$evOwnerId")
            .addHeader("Authorization", "Bearer $token")
            .put(bodyEV)
            .build()

        // Update User status
        val jsonUser = JSONObject().apply {
            put("status", "inactive")
        }
        val bodyUser = jsonUser.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val requestUser = Request.Builder()
            .url("http://10.0.2.2:5001/api/users/$userId")
            .addHeader("Authorization", "Bearer $token")
            .put(bodyUser)
            .build()

        val client = OkHttpClient()

        // Execute EVOwner update
        client.newCall(requestEV).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Execute User update regardless of EVOwner response
                client.newCall(requestUser).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProfileActivity, "Account deactivated successfully", Toast.LENGTH_SHORT).show()
                                sessionManager.clearSession()
                                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@ProfileActivity, "Failed to deactivate account", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchProfileData(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/evowners/profile/me")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    evOwnerId = jsonResponse.optString("_id", jsonResponse.optString("id", ""))
                    val userIdObj = jsonResponse.optJSONObject("userId")
                    userId = if (userIdObj != null) userIdObj.optString("_id", "") else ""

                    runOnUiThread {
                        tvName.text = jsonResponse.optString("name", "N/A")
                        tvNic.text = jsonResponse.optString("nic", "N/A")
                        tvEmail.text = jsonResponse.optString("email", "N/A")
                        tvPhone.text = jsonResponse.optString("phone", "N/A")
                        tvAddress.text = jsonResponse.optString("address", "N/A")
                        tvVehicleModel.text = jsonResponse.optString("vehicleModel", "N/A")
                        tvVehicleNumber.text = jsonResponse.optString("vehicleNumber", "N/A")
                        tvStatus.text = jsonResponse.optString("status", "N/A").capitalize()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}