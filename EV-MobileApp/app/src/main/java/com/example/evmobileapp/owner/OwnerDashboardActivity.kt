package com.example.evmobileapp.owner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import org.json.JSONObject
import okhttp3.*
import java.io.IOException

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var pendingReservationsText: TextView
    private lateinit var approvedReservationsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        pendingReservationsText = findViewById(R.id.pending_reservations)
        approvedReservationsText = findViewById(R.id.approved_reservations)

        // Fetch the user data from the backend
        val token = sessionManager.getToken()
        if (token != null) {
            fetchDashboardData(token)
        }
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
                        pendingReservationsText.text = "Pending Reservations: 0"
                        approvedReservationsText.text = "Approved Reservations: 0"
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    pendingReservationsText.text = "Pending Reservations: 0"
                    approvedReservationsText.text = "Approved Reservations: 0"
                }
            }
        })
    }
}
