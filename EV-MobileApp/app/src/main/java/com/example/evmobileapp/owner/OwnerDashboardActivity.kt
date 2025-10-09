package com.example.evmobileapp.owner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import okhttp3.*
import java.io.IOException

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var tvTotalBookings: TextView
    private lateinit var tvActiveBookings: TextView
    private lateinit var tvCompletedBookings: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvAvailableStations: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var createReservationButton: MaterialButton
    private lateinit var viewHistoryButton: MaterialButton
    private lateinit var viewStationMapButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        tvTotalBookings = findViewById(R.id.tv_total_bookings)
        tvActiveBookings = findViewById(R.id.tv_active_bookings)
        tvCompletedBookings = findViewById(R.id.tv_completed_bookings)
        tvTotalSpent = findViewById(R.id.tv_total_spent)
        tvAvailableStations = findViewById(R.id.tv_available_stations)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        createReservationButton = findViewById(R.id.create_reservation_button)
        viewHistoryButton = findViewById(R.id.view_history_button)
        viewStationMapButton = findViewById(R.id.view_station_map_button)

        // Set up bottom navigation
        setupBottomNavigation()

        // Set up button clicks
        createReservationButton.setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
        }

        viewHistoryButton.setOnClickListener {
            startActivity(Intent(this, BookingHistoryActivity::class.java))
        }

        viewStationMapButton.setOnClickListener {
            startActivity(Intent(this, StationMapActivity::class.java))
        }

        // Fetch the user data from the backend
        val token = sessionManager.getToken()
        if (token != null) {
            fetchDashboardData(token)
        } else {
            runOnUiThread {
                tvTotalBookings.text = "0"
                tvActiveBookings.text = "0"
                tvCompletedBookings.text = "0"
                tvTotalSpent.text = "0"
                tvAvailableStations.text = "0"
                val statColor = resources.getColor(R.color.blue_primary, null)
                tvTotalBookings.setTextColor(statColor)
                tvActiveBookings.setTextColor(statColor)
                tvCompletedBookings.setTextColor(statColor)
                tvTotalSpent.setTextColor(statColor)
                tvAvailableStations.setTextColor(statColor)
            }
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
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    // Use correct field names from dashboard API
                    val totalBookings = jsonResponse.optInt("totalBookings", 0)
                    val activeBookings = jsonResponse.optInt("activeBookings", 0)
                    val completedBookings = jsonResponse.optInt("completedBookings", 0)
                    val totalSpent = jsonResponse.optInt("totalSpent", 0)
                    val availableStations = jsonResponse.optInt("availableStations", 0)

                    runOnUiThread {
                        tvTotalBookings.text = totalBookings.toString()
                        tvActiveBookings.text = activeBookings.toString()
                        tvCompletedBookings.text = completedBookings.toString()
                        tvTotalSpent.text = totalSpent.toString()
                        tvAvailableStations.text = availableStations.toString()
                        // Optionally set text color if not set in XML:
                        val statColor = resources.getColor(R.color.blue_primary, null)
                        tvTotalBookings.setTextColor(statColor)
                        tvActiveBookings.setTextColor(statColor)
                        tvCompletedBookings.setTextColor(statColor)
                        tvTotalSpent.setTextColor(statColor)
                        tvAvailableStations.setTextColor(statColor)
                    }
                } else {
                    runOnUiThread {
                        tvTotalBookings.text = "0"
                        tvActiveBookings.text = "0"
                        tvCompletedBookings.text = "0"
                        tvTotalSpent.text = "0"
                        tvAvailableStations.text = "0"
                        val statColor = resources.getColor(R.color.blue_primary, null)
                        tvTotalBookings.setTextColor(statColor)
                        tvActiveBookings.setTextColor(statColor)
                        tvCompletedBookings.setTextColor(statColor)
                        tvTotalSpent.setTextColor(statColor)
                        tvAvailableStations.setTextColor(statColor)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    tvTotalBookings.text = "0"
                    tvActiveBookings.text = "0"
                    tvCompletedBookings.text = "0"
                    tvTotalSpent.text = "0"
                    tvAvailableStations.text = "0"
                    val statColor = resources.getColor(R.color.blue_primary, null)
                    tvTotalBookings.setTextColor(statColor)
                    tvActiveBookings.setTextColor(statColor)
                    tvCompletedBookings.setTextColor(statColor)
                    tvTotalSpent.setTextColor(statColor)
                    tvAvailableStations.setTextColor(statColor)
                }
            }
        })
    }
}