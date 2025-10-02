package com.example.evmobileapp.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.owner.OwnerDashboardActivity
import com.example.evmobileapp.owner.ReservationActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bookingListView: ListView
    private lateinit var bottomNavigation: BottomNavigationView
    private val bookings = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        bookingListView = findViewById(R.id.booking_history_list)
        bottomNavigation = findViewById(R.id.bottom_navigation_history)

        // Set up bottom navigation
        setupBottomNavigation()

        val token = sessionManager.getToken()
        if (token != null) {
            fetchBookingHistory(token)
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
                    // Already on history, do nothing
                    true
                }
                R.id.nav_profile -> {
                    // TODO: Navigate to ProfileActivity
                    true
                }
                else -> false
            }
        }

        // Select history by default
        bottomNavigation.selectedItemId = R.id.nav_history
    }

    private fun fetchBookingHistory(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "[]"
                    val jsonResponse = try {
                        JSONArray(responseBody)
                    } catch (e: Exception) {
                        JSONArray()
                    }

                    bookings.clear()
                    for (i in 0 until jsonResponse.length()) {
                        bookings.add(jsonResponse.getJSONObject(i))
                    }

                    runOnUiThread {
                        if (bookings.isEmpty()) {
                            Toast.makeText(this@BookingHistoryActivity, "No bookings found", Toast.LENGTH_SHORT).show()
                        }
                        bookingListView.adapter =
                            BookingHistoryAdapter(this@BookingHistoryActivity, bookings)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@BookingHistoryActivity,
                            "Failed to fetch booking history",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@BookingHistoryActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}

class BookingHistoryAdapter(
    private val context: Context,
    private val bookings: List<JSONObject>
) : BaseAdapter() {

    override fun getCount(): Int = bookings.size

    override fun getItem(position: Int): Any = bookings[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_reservation, parent, false)

        val booking = bookings[position]

        val stationName = rowView.findViewById<TextView>(R.id.reservation_station)
        val dateTime = rowView.findViewById<TextView>(R.id.reservation_date_time)
        val status = rowView.findViewById<TextView>(R.id.reservation_status)

        // Bind data safely
        stationName.text = booking.optString("stationName", "Unknown Station")
        val startTime = booking.optString("startTime", "")
        val endTime = booking.optString("endTime", "")
        dateTime.text = "${booking.optString("reservationDate", "")} ${startTime} - ${endTime}"
        status.text = booking.optString("status", "Unknown").capitalize()

        return rowView
    }
}