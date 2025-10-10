package com.example.evmobileapp.operator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.operator.OperatorDashboardActivity
import com.example.evmobileapp.operator.OperatorProfileActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class BookingOperatorActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bookingListView: ListView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnScanQr: Button
    private val bookings = mutableListOf<JSONObject>()

    // Modern scanner launcher
    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult? ->
        if (result?.contents == null) {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
        } else {
            val scannedData = result.contents
            Log.d("QRScan", "Scanned data: $scannedData")
            parseQRData(scannedData)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_operator)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        bookingListView = findViewById(R.id.booking_list)
        bottomNavigation = findViewById(R.id.bottom_navigation_history)
        btnScanQr = findViewById(R.id.btn_scan_qr)

        setupBottomNavigation()
        btnScanQr.setOnClickListener { startQRScan() }

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
                R.id.nav_home_station -> {
                    startActivity(Intent(this, OperatorDashboardActivity::class.java)); true
                }
                R.id.nav_bookings_station -> true
                R.id.nav_profile_station -> {
                    startActivity(Intent(this, OperatorProfileActivity::class.java)); true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_bookings_station
    }

    private fun startQRScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan a QR Code for Booking")
            setCameraId(0) // Use back camera
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
        }
        scanLauncher.launch(options)
    }

    private fun parseQRData(data: String) {
        val lines = data.lines()
        var bookingId = ""
        lines.forEach { line ->
            if (line.startsWith("Booking ID:")) {
                bookingId = line.substringAfter("Booking ID:").trim()
                return@forEach
            }
        }
        if (bookingId.isNotEmpty()) {
            fetchBookingById(bookingId)
        } else {
            Toast.makeText(this, "Invalid QR Code: Booking ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBookingById(bookingId: String) {
        val token = sessionManager.getToken() ?: return
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings/$bookingId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonStr = response.body?.string()
                    val booking = try {
                        JSONObject(jsonStr)
                    } catch (e: Exception) {
                        null
                    }
                    runOnUiThread {
                        if (booking != null) {
                            showBookingDetails(booking)
                        } else {
                            Toast.makeText(this@BookingOperatorActivity, "Invalid booking data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@BookingOperatorActivity, "Failed to fetch booking details", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BookingOperatorActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
                    val jsonResponse = try { JSONArray(responseBody) } catch (e: Exception) { JSONArray() }

                    bookings.clear()
                    for (i in 0 until jsonResponse.length()) {
                        bookings.add(jsonResponse.getJSONObject(i))
                    }

                    runOnUiThread {
                        if (bookings.isEmpty()) {
                            Toast.makeText(this@BookingOperatorActivity, "No bookings found", Toast.LENGTH_SHORT).show()
                        } else {
                            val adapter = SimpleBookingAdapter(this@BookingOperatorActivity, bookings)
                            bookingListView.adapter = adapter
                            bookingListView.setOnItemClickListener { _, _, position, _ ->
                                showBookingDetails(bookings[position])
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@BookingOperatorActivity, "Failed to fetch booking history", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BookingOperatorActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showBookingDetails(booking: JSONObject) {
        val token = sessionManager.getToken() ?: return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_booking_details_simple, null)

        val tvStationName = dialogView.findViewById<TextView>(R.id.tv_station_name)
        val tvAddress = dialogView.findViewById<TextView>(R.id.tv_address)
        val tvDateTime = dialogView.findViewById<TextView>(R.id.tv_date_time)
        val tvDuration = dialogView.findViewById<TextView>(R.id.tv_duration)
        val tvTotalCost = dialogView.findViewById<TextView>(R.id.tv_total_cost)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tv_status)
        val btnAction = dialogView.findViewById<Button>(R.id.btn_action)
        val btnGenerateQR = dialogView.findViewById<Button>(R.id.btn_generate_qr)

        tvStationName.text = booking.optString("stationName", "N/A")
        tvAddress.text = booking.optString("stationAddress", "N/A")
        val startTime = booking.optString("startTime", "")
        val endTime = booking.optString("endTime", "")
        tvDateTime.text = "${booking.optString("reservationDate", "")} ($startTime - $endTime)"
        tvDuration.text = "${booking.optInt("duration", 0)} hours"
        tvTotalCost.text = "LKR ${booking.optInt("totalCost", 0)}"
        tvStatus.text = booking.optString("status", "N/A").replaceFirstChar { it.uppercase() }

        val status = booking.optString("status", "").lowercase()
        val bookingId = booking.optString("_id", booking.optString("id", ""))

        // Configure buttons based on status
        when (status) {
            "pending" -> {
                btnAction.text = "Approve"
                btnAction.visibility = View.VISIBLE
                btnAction.setOnClickListener {
                    updateBookingStatus(token, bookingId, "approved")
                }
            }
            "approved" -> {
                btnAction.text = "Start Charging"
                btnAction.visibility = View.VISIBLE
                btnGenerateQR.visibility = View.VISIBLE
                btnAction.setOnClickListener {
                    updateBookingStatus(token, bookingId, "in_progress")
                }
            }
            "in_progress" -> {
                btnAction.text = "Complete"
                btnAction.visibility = View.VISIBLE
                btnGenerateQR.visibility = View.VISIBLE
                btnAction.setOnClickListener {
                    updateBookingStatus(token, bookingId, "completed")
                }
            }
            else -> {
                btnAction.visibility = View.GONE
                btnGenerateQR.visibility = View.GONE
            }
        }

        btnGenerateQR.setOnClickListener {
            generateAndShowQR(booking)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .show()
    }

    private fun updateBookingStatus(token: String, bookingId: String, newStatus: String) {
        Log.d("BookingOperator", "Updating status for booking $bookingId to $newStatus")
        val client = OkHttpClient()
        val json = JSONObject().apply { put("status", newStatus) }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings/$bookingId/status")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("BookingOperator", "Response code: ${response.code}")
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BookingOperatorActivity, "Booking status updated to $newStatus", Toast.LENGTH_SHORT).show()
                        val token = sessionManager.getToken()
                        if (token != null) {
                            refreshBookings(token)
                        }
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("BookingOperator", "Failed to update: ${response.code} - $errorBody")
                        Toast.makeText(this@BookingOperatorActivity, "Failed to update booking status: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BookingOperator", "Network failure", e)
                runOnUiThread {
                    Toast.makeText(this@BookingOperatorActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun generateAndShowQR(booking: JSONObject) {
        val bookingId = booking.optString("_id", booking.optString("id", ""))
        if (bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Build QR data string (include key details, including Booking ID)
        val qrData = """
            Booking ID: $bookingId
            Station: ${booking.optString("stationName", "N/A")}
            Address: ${booking.optString("stationAddress", "N/A")}
            Date: ${booking.optString("reservationDate", "N/A")}
            Time: ${booking.optString("startTime", "N/A")} - ${booking.optString("endTime", "N/A")}
            Duration: ${booking.optInt("duration", 0)} hours
            Cost: LKR ${booking.optInt("totalCost", 0)}
            Status: ${booking.optString("status", "N/A")}
        """.trimIndent()

        val bitmap = generateQRCode(qrData) ?: run {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
            return
        }

        // Show QR in dialog (no download/save)
        val qrDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null)
        val ivQR = qrDialogView.findViewById<ImageView>(R.id.iv_qr_code)
        val btnClose = qrDialogView.findViewById<Button>(R.id.btn_close_qr)

        ivQR.setImageBitmap(bitmap)

        val qrDialog = AlertDialog.Builder(this)
            .setTitle("Your QR Code")
            .setView(qrDialogView)
            .create()

        btnClose.setOnClickListener { qrDialog.dismiss() }

        qrDialog.show()
    }

    private fun generateQRCode(data: String, size: Int = 300): Bitmap? {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, EncodeHintType.MARGIN to 1)
        val bitMatrix: BitMatrix
        try {
            bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        } catch (e: WriterException) {
            Log.e("QRCode", "Error generating QR code", e)
            return null
        }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    // Simple Adapter for ListView
    private class SimpleBookingAdapter(
        private val context: Context,
        private val bookings: List<JSONObject>
    ) : BaseAdapter() {

        override fun getCount(): Int = bookings.size
        override fun getItem(position: Int): JSONObject = bookings[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_booking_simple, parent, false)
            val booking = bookings[position]

            val tvStationName = view.findViewById<TextView>(R.id.tv_station_name)
            val tvStatus = view.findViewById<TextView>(R.id.tv_status)
            val tvDateTime = view.findViewById<TextView>(R.id.tv_date_time)
            val tvAddress = view.findViewById<TextView>(R.id.tv_address)

            tvStationName.text = booking.optString("stationName", "Unknown Station")
            val status = booking.optString("status", "Unknown").replaceFirstChar { it.uppercase() }
            tvStatus.text = status
            
            val startTime = booking.optString("startTime", "")
            val endTime = booking.optString("endTime", "")
            tvDateTime.text = "${booking.optString("reservationDate", "")} $startTime - $endTime"
            tvAddress.text = booking.optString("stationAddress", "N/A")

            // Set status color
            val statusBackground = tvStatus.background as? GradientDrawable
            when (status.lowercase()) {
                "pending" -> tvStatus.setBackgroundColor(context.getColor(android.R.color.holo_orange_dark))
                "approved" -> tvStatus.setBackgroundColor(context.getColor(android.R.color.holo_blue_bright))
                "in_progress" -> tvStatus.setBackgroundColor(context.getColor(android.R.color.holo_green_dark))
                "completed" -> tvStatus.setBackgroundColor(context.getColor(android.R.color.holo_green_light))
                "cancelled" -> tvStatus.setBackgroundColor(context.getColor(android.R.color.holo_red_dark))
                else -> tvStatus.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            }

            return view
        }
    }

    private fun refreshBookings(token: String) {
        fetchBookingHistory(token)
    }
}