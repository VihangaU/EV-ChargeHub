package com.example.evmobileapp.operator

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.SessionManager
import com.google.zxing.integration.android.IntentIntegrator
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull


class OperatorScanActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_scan)

        sessionManager = SessionManager(this)

        // Start scanning QR code
        startQRCodeScanner()
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setCameraId(0)  // Use rear camera
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    // Handle the result of the QR code scan
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {  // Check if the scanned data exists
                val scannedData = result.contents
                confirmBooking(scannedData)
            } else {
                Toast.makeText(this, "QR Scan failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmBooking(reservationId: String) {
        val token = sessionManager.getToken()

        if (token != null) {
            // Call backend API to confirm the booking using the reservation ID
            val client = OkHttpClient()
            val json = JSONObject()
            json.put("reservationId", reservationId)

            val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("http://10.0.2.2:5001/api/bookings/confirm")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@OperatorScanActivity, "Booking confirmed!", Toast.LENGTH_SHORT).show()
                            finish()  // Navigate back to the dashboard or other screen
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@OperatorScanActivity, "Failed to confirm booking", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@OperatorScanActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}
