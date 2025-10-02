package com.example.evmobileapp.operator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class OperatorDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var pendingReservationsText: TextView
    private lateinit var approvedReservationsText: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_dashboard)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        pendingReservationsText = findViewById(R.id.pending_reservations)
        approvedReservationsText = findViewById(R.id.approved_reservations)
        confirmButton = findViewById(R.id.confirm_button)

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

    private fun fetchDashboardData(token: String) {
        val client = apiClient.client
        val request = apiClient.getRequest("http://10.0.2.2:5001/api/bookings/dashboard", token)

        request.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody!!)
                    val pendingCount = jsonResponse.getInt("pendingReservations")
                    val approvedCount = jsonResponse.getInt("approvedReservations")

                    runOnUiThread {
                        pendingReservationsText.text = "Pending Reservations: $pendingCount"
                        approvedReservationsText.text = "Approved Reservations: $approvedCount"
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

private fun Response.enqueue(callback: Callback) {

}
