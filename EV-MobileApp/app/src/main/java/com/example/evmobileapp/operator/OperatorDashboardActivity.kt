package com.example.evmobileapp.operator

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.owner.BookingHistoryActivity
import com.example.evmobileapp.owner.OwnerDashboardActivity
import com.example.evmobileapp.owner.ProfileActivity
import com.example.evmobileapp.owner.ReservationActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class OperatorDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var tvTotalStations: TextView
    private lateinit var tvTotalBookings: TextView
    private lateinit var tvActiveBookings: TextView
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvAvailableSlots: TextView
    private lateinit var confirmButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        tvTotalStations = findViewById(R.id.tv_total_stations)
        tvTotalBookings = findViewById(R.id.tv_total_bookings)
        tvActiveBookings = findViewById(R.id.tv_active_bookings)
        tvTotalRevenue = findViewById(R.id.tv_total_revenue)
        tvAvailableSlots = findViewById(R.id.tv_available_slots)
        confirmButton = findViewById(R.id.confirm_button)
        bottomNavigation = findViewById(R.id.bottom_navigation_operator)

        setupBottomNavigation()

        // Fetch the user data from the backend
        val token = sessionManager.getToken()
        if (token != null) {
            fetchDashboardData(token)
        }

        // Handle Confirm button click
        confirmButton.setOnClickListener {
            // Handle the booking confirmation logic here (e.g., scan a QR or manually confirm)
            Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home for operator
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
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun fetchDashboardData(token: String) {
        val client = okhttp3.OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/dashboard/stats")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val totalStations = jsonResponse.optInt("totalStations", 0)
                    val totalBookings = jsonResponse.optInt("totalBookings", 0)
                    val activeBookings = jsonResponse.optInt("activeBookings", 0)
                    val totalRevenue = jsonResponse.optInt("totalRevenue", 0)
                    val availableSlots = jsonResponse.optInt("availableSlots", 0)

                    runOnUiThread {
                        tvTotalStations.text = "Total Stations: $totalStations"
                        tvTotalBookings.text = "Total Bookings: $totalBookings"
                        tvActiveBookings.text = "Active Bookings: $activeBookings"
                        tvTotalRevenue.text = "Total Revenue: LKR $totalRevenue"
                        tvAvailableSlots.text = "Available Slots: $availableSlots"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@OperatorDashboardActivity, "Failed to fetch dashboard data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OperatorDashboardActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}