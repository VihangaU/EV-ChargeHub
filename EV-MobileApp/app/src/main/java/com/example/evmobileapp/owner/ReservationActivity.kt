package com.example.evmobileapp.owner

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ReservationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var stationInput: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        dateInput = findViewById(R.id.reservation_date)
        timeInput = findViewById(R.id.reservation_time)
        stationInput = findViewById(R.id.station)
        createButton = findViewById(R.id.create_reservation_button)

        createButton.setOnClickListener {
            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val station = stationInput.text.toString()

            if (date.isNotEmpty() && time.isNotEmpty() && station.isNotEmpty()) {
                val token = sessionManager.getToken()
                if (token != null) {
                    createReservation(date, time, station, token)
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createReservation(date: String, time: String, station: String, token: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("date", date)
        json.put("time", time)
        json.put("station", station)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@ReservationActivity, "Reservation created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ReservationActivity, "Failed to create reservation", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ReservationActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
