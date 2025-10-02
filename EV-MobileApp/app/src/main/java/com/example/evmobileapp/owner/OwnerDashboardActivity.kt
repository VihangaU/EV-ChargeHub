package com.example.evmobileapp.owner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.owner.ReservationActivity
import com.example.evmobileapp.owner.BookingHistoryActivity
import com.example.evmobileapp.owner.ProfileActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import okhttp3.*
import java.io.IOException

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var pendingReservationsText: TextView
    private lateinit var approvedReservationsText: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        pendingReservationsText = findViewById(R.id.pending_reservations)
        approvedReservationsText = findViewById(R.id.approved_reservations)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set up bottom navigation
        setupBottomNavigation()

        // Fetch the user data from the backend
        val token = sessionManager.getToken()
        if (token != null) {
            fetchDashboardData(token)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Select home by default
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun fetchDashboardData(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/dashboard/stats")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody!!)

                    // Use correct field names from dashboard API
                    val totalBookings = jsonResponse.optInt("totalBookings", 0)
                    val activeBookings = jsonResponse.optInt("activeBookings", 0)

                    runOnUiThread {
                        pendingReservationsText.text = "Total Bookings: $totalBookings"
                        approvedReservationsText.text = "Active Bookings: $activeBookings"
                    }
                } else {
                    runOnUiThread {
                        pendingReservationsText.text = "Total Bookings: 0"
                        approvedReservationsText.text = "Active Bookings: 0"
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    pendingReservationsText.text = "Total Bookings: 0"
                    approvedReservationsText.text = "Active Bookings: 0"
                }
            }
        })
    }
}