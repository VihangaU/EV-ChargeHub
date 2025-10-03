package com.example.evmobileapp.owner

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
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
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bookingListView: ListView
    private lateinit var bottomNavigation: BottomNavigationView
    private val bookings = mutableListOf<JSONObject>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        bookingListView = findViewById(R.id.booking_history_list)
        bottomNavigation = findViewById(R.id.bottom_navigation_history)

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
                    startActivity(Intent(this, OwnerDashboardActivity::class.java)); true
                }
                R.id.nav_reservations -> {
                    startActivity(Intent(this, ReservationActivity::class.java)); true
                }
                R.id.nav_history -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)); true
                }
                else -> false
            }
        }
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
                    val jsonResponse = try { JSONArray(responseBody) } catch (e: Exception) { JSONArray() }

                    bookings.clear()
                    for (i in 0 until jsonResponse.length()) {
                        bookings.add(jsonResponse.getJSONObject(i))
                    }

                    runOnUiThread {
                        if (bookings.isEmpty()) {
                            Toast.makeText(this@BookingHistoryActivity, "No bookings found", Toast.LENGTH_SHORT).show()
                        }
                        bookingListView.adapter = BookingHistoryAdapter(
                            this@BookingHistoryActivity, bookings
                        ) { booking -> showBookingDetails(booking) }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@BookingHistoryActivity, "Failed to fetch booking history", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BookingHistoryActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun refreshBookings(token: String) = fetchBookingHistory(token)

    private fun showBookingDetails(booking: JSONObject) {
        val token = sessionManager.getToken() ?: return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_booking_details, null)

        val tvStationName = dialogView.findViewById<TextView>(R.id.tv_station_name)
        val tvAddress = dialogView.findViewById<TextView>(R.id.tv_address)
        val tvDateTime = dialogView.findViewById<TextView>(R.id.tv_date_time)
        val tvStartTime = dialogView.findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = dialogView.findViewById<TextView>(R.id.tv_end_time)
        val tvDuration = dialogView.findViewById<TextView>(R.id.tv_duration)
        val tvTotalCost = dialogView.findViewById<TextView>(R.id.tv_total_cost)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tv_status)
        val tvNotes = dialogView.findViewById<TextView>(R.id.tv_notes)
        val btnEdit = dialogView.findViewById<Button>(R.id.btn_edit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnGenerateQR = dialogView.findViewById<Button>(R.id.btn_generate_qr)

        tvStationName.text = booking.optString("stationName", "N/A")
        tvAddress.text = booking.optString("stationAddress", "N/A")
        tvDateTime.text = booking.optString("reservationDate", "N/A")
        tvStartTime.text = booking.optString("startTime", "N/A")
        tvEndTime.text = booking.optString("endTime", "N/A")
        tvDuration.text = "${booking.optInt("duration", 0)} hours"
        tvTotalCost.text = "LKR ${booking.optInt("totalCost", 0)}"
        tvStatus.text = booking.optString("status", "N/A").replaceFirstChar { it.uppercase() }
        tvNotes.text = booking.optString("notes", "No notes")

        val status = booking.optString("status", "").lowercase()
        val isPending = status == "pending"
        val isApproved = status == "approved"

        btnEdit.visibility = if (isPending) View.VISIBLE else View.GONE
        btnCancel.visibility = if (isPending) View.VISIBLE else View.GONE
        btnGenerateQR.visibility = if (isApproved) View.VISIBLE else View.GONE

        val dialog = AlertDialog.Builder(this)
            .setTitle("Booking Details")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        btnEdit.setOnClickListener {
            dialog.dismiss()
            showEditBookingDialog(booking, token)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            showCancelBookingConfirmation(booking, token)
        }

        btnGenerateQR.setOnClickListener {
            dialog.dismiss()
            generateAndShowQR(booking)
        }

        dialog.show()
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

        // Show QR in dialog
        val qrDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null)
        val ivQR = qrDialogView.findViewById<ImageView>(R.id.iv_qr_code)
        val btnDownload = qrDialogView.findViewById<Button>(R.id.btn_download_qr)
        val btnClose = qrDialogView.findViewById<Button>(R.id.btn_close_qr)

        ivQR.setImageBitmap(bitmap)

        val qrDialog = AlertDialog.Builder(this)
            .setTitle("Your QR Code")
            .setView(qrDialogView)
            .create()

        btnDownload.setOnClickListener {
            val uri = saveQRToGallery(bitmap, bookingId)
            if (uri != null) {
                Toast.makeText(this, "QR code saved to Pictures/EVBookings", Toast.LENGTH_SHORT).show()
                qrDialog.dismiss()
            }
        }

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

    private fun saveQRToGallery(bitmap: Bitmap, bookingId: String): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "QR_Booking_${bookingId}_$timestamp.png"

        var outputStream: OutputStream? = null
        var uri: Uri? = null
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EVBookings")
                }
            }

            val resolver = contentResolver
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { safeUri ->
                outputStream = resolver.openOutputStream(safeUri)
                outputStream?.let { safeStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, safeStream)
                    safeStream.close()  // Close immediately after compress to avoid leaks
                } ?: run {
                    Log.e("SaveQR", "Failed to open output stream")
                    Toast.makeText(this, "Failed to open file for writing", Toast.LENGTH_SHORT).show()
                    return null
                }
            } ?: run {
                Log.e("SaveQR", "Failed to insert URI into MediaStore")
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show()
                return null
            }
        } catch (e: Exception) {
            Log.e("SaveQR", "Error saving QR code", e)
            Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show()
            return null
        } finally {
            // No need for close here since we close inside the let block
        }
        return uri
    }

    private fun showEditBookingDialog(booking: JSONObject, token: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_booking, null)
        val etStartTime = dialogView.findViewById<EditText>(R.id.et_start_time)
        val etEndTime = dialogView.findViewById<EditText>(R.id.et_end_time)
        val etNotes = dialogView.findViewById<EditText>(R.id.et_notes)

        etStartTime.setText(booking.optString("startTime", ""))
        etEndTime.setText(booking.optString("endTime", ""))
        etNotes.setText(booking.optString("notes", ""))

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Booking")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val startTime = etStartTime.text.toString().trim()
                val endTime = etEndTime.text.toString().trim()
                val notes = etNotes.text.toString().trim()

                if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                    val bookingId = booking.optString("_id", booking.optString("id", ""))
                    if (bookingId.isNotEmpty()) {
                        val duration = calculateDuration(startTime, endTime)
                        updateBooking(token, bookingId, startTime, endTime, notes, duration)
                    } else {
                        Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun calculateDuration(startTime: String, endTime: String): Int {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.parse(startTime)
        val end = sdf.parse(endTime)
        val calStart = Calendar.getInstance().apply { time = start }
        val calEnd = Calendar.getInstance().apply { time = end }
        val diffInMillis = (calEnd.timeInMillis - calStart.timeInMillis).absoluteValue
        return (diffInMillis / (1000 * 60 * 60)).toInt()
    }

    private fun updateBooking(token: String, bookingId: String, startTime: String, endTime: String, notes: String, duration: Int) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("startTime", startTime)
            put("endTime", endTime)
            put("duration", duration)
            put("notes", notes)
        }
        Log.d("UpdateBooking", "Updating booking with data: $json")
        Log.d("UpdateBooking", "Id: $bookingId")

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings/$bookingId")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BookingHistoryActivity, "Booking updated successfully", Toast.LENGTH_SHORT).show()
                        refreshBookings(token)
                    } else {
                        Toast.makeText(this@BookingHistoryActivity, "Failed to update booking", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BookingHistoryActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showCancelBookingConfirmation(booking: JSONObject, token: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { _, _ ->
                val bookingId = booking.optString("_id", booking.optString("id", ""))
                if (bookingId.isNotEmpty()) {
                    cancelBooking(token, bookingId)
                } else {
                    Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelBooking(token: String, bookingId: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply { put("status", "cancelled") }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings/$bookingId/status")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BookingHistoryActivity, "Booking cancelled successfully", Toast.LENGTH_SHORT).show()
                        refreshBookings(token)
                    } else {
                        Toast.makeText(this@BookingHistoryActivity, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BookingHistoryActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private class BookingHistoryAdapter(
        private val context: Context,
        private val bookings: List<JSONObject>,
        private val onItemClick: (JSONObject) -> Unit
    ) : BaseAdapter() {
        override fun getCount(): Int = bookings.size
        override fun getItem(position: Int): Any = bookings[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false)
            val booking = bookings[position]

            val tvStationName = rowView.findViewById<TextView>(R.id.tv_station_name)
            val tvAddress = rowView.findViewById<TextView>(R.id.tv_address)
            val tvDateTime = rowView.findViewById<TextView>(R.id.tv_date_time)
            val tvStatus = rowView.findViewById<TextView>(R.id.tv_status)
            val ivIcon = rowView.findViewById<ImageView>(R.id.iv_icon)

            tvStationName.text = booking.optString("stationName", "Unknown Station")
            tvAddress.text = booking.optString("stationAddress", "N/A")
            val startTime = booking.optString("startTime", "")
            val endTime = booking.optString("endTime", "")
            tvDateTime.text = "${booking.optString("reservationDate", "")} $startTime - $endTime"
            val status = booking.optString("status", "Unknown").replaceFirstChar { it.uppercase() }
            tvStatus.text = status

            tvStatus.setTextColor(when (status.lowercase()) {
                "pending" -> context.getColor(android.R.color.holo_orange_dark)
                "approved", "completed" -> context.getColor(android.R.color.holo_green_dark)
                "cancelled" -> context.getColor(android.R.color.holo_red_dark)
                else -> context.getColor(android.R.color.black)
            })

            ivIcon.setImageResource(when (status.lowercase()) {
                "pending" -> android.R.drawable.ic_dialog_alert
                "approved" -> android.R.drawable.ic_dialog_info
                "completed" -> android.R.drawable.ic_dialog_email
                "cancelled" -> android.R.drawable.ic_delete
                else -> android.R.drawable.ic_menu_recent_history
            })
            ivIcon.setColorFilter(context.getColor(android.R.color.holo_blue_dark))

            rowView.setOnClickListener { onItemClick(booking) }
            return rowView
        }
    }
}