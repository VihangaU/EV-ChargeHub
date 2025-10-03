package com.example.evmobileapp.operator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class BookingOperatorActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bookingExpandableListView: ExpandableListView
    private lateinit var bottomNavigation: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_operator)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        bookingExpandableListView = findViewById(R.id.booking_history_list)
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

                    val bookings = mutableListOf<JSONObject>()
                    for (i in 0 until jsonResponse.length()) {
                        bookings.add(jsonResponse.getJSONObject(i))
                    }

                    val stationBookings = bookings.groupBy { it.optString("stationName", "Unknown Station") }
                    val stationList = stationBookings.keys.sorted().toList()

                    runOnUiThread {
                        if (stationList.isEmpty()) {
                            Toast.makeText(this@BookingOperatorActivity, "No bookings found", Toast.LENGTH_SHORT).show()
                        } else {
                            val adapter = BookingExpandableAdapter(
                                this@BookingOperatorActivity, stationList, stationBookings
                            )
                            bookingExpandableListView.setAdapter(adapter)
                            bookingExpandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                                val booking = adapter.getChild(groupPosition, childPosition) as JSONObject
                                showBookingDetails(booking)
                                true
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
        val bookingId = booking.optString("_id", booking.optString("id", ""))

        // Reset visibilities
        btnEdit.visibility = View.GONE
        btnCancel.visibility = View.GONE

        val dialog = AlertDialog.Builder(this)
            .setTitle("Booking Details")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        when (status) {
            "pending" -> {
                btnEdit.text = "Approve"
                btnEdit.setOnClickListener {
                    dialog.dismiss()
                    updateBookingStatus(token, bookingId, "approved")
                }
                btnEdit.visibility = View.VISIBLE

                btnCancel.text = "Reject"
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                    updateBookingStatus(token, bookingId, "cancelled")
                }
                btnCancel.visibility = View.VISIBLE
            }
            "approved" -> {
                btnEdit.text = "Start Charging"
                btnEdit.setOnClickListener {
                    dialog.dismiss()
                    updateBookingStatus(token, bookingId, "in_progress")
                }
                btnEdit.visibility = View.VISIBLE
            }
            "in_progress" -> {
                btnEdit.text = "Complete"
                btnEdit.setOnClickListener {
                    dialog.dismiss()
                    updateBookingStatus(token, bookingId, "completed")
                }
                btnEdit.visibility = View.VISIBLE
            }
            // For other statuses like completed, cancelled: no actions
            // Note: Pending is initial status, cannot set back to pending
        }

        dialog.show()
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
                        refreshBookings(token)
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

    private inner class BookingExpandableAdapter(
        private val context: Context,
        private val groups: List<String>,
        private val children: Map<String, List<JSONObject>>
    ) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = groups.size

        override fun getChildrenCount(groupPosition: Int): Int = children[groups[groupPosition]]?.size ?: 0

        override fun getGroup(groupPosition: Int): Any = groups[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any = children[groups[groupPosition]]!![childPosition]

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 1000 + childPosition).toLong()

        override fun hasStableIds(): Boolean = true

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            val groupView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_station_group, parent, false)
            val stationName = groups[groupPosition]
            val bookingCount = children[stationName]?.size ?: 0

            val tvStationName = groupView.findViewById<TextView>(R.id.tv_station_name_group)
            val tvCount = groupView.findViewById<TextView>(R.id.tv_booking_count)

            tvStationName.text = "$stationName Station Bookings"
            tvCount.text = "($bookingCount)"

            return groupView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_reservation_operator, parent, false)
            val booking = children[groups[groupPosition]]!![childPosition]

            val tvAddress = rowView.findViewById<TextView>(R.id.tv_address)
            val tvDateTime = rowView.findViewById<TextView>(R.id.tv_date_time)
            val tvStatus = rowView.findViewById<TextView>(R.id.tv_status)
            val ivIcon = rowView.findViewById<ImageView>(R.id.iv_icon)

            tvAddress.text = booking.optString("stationAddress", "N/A")
            val startTime = booking.optString("startTime", "")
            val endTime = booking.optString("endTime", "")
            tvDateTime.text = "${booking.optString("reservationDate", "")} $startTime - $endTime"
            val status = booking.optString("status", "Unknown").replaceFirstChar { it.uppercase() }
            tvStatus.text = status

            tvStatus.setTextColor(when (status.lowercase()) {
                "pending" -> context.getColor(android.R.color.holo_orange_dark)
                "approved", "completed", "in_progress" -> context.getColor(android.R.color.holo_green_dark)
                "cancelled" -> context.getColor(android.R.color.holo_red_dark)
                else -> context.getColor(android.R.color.black)
            })

            ivIcon.setImageResource(when (status.lowercase()) {
                "pending" -> android.R.drawable.ic_dialog_alert
                "approved", "in_progress" -> android.R.drawable.ic_dialog_info
                "completed" -> android.R.drawable.ic_dialog_email
                "cancelled" -> android.R.drawable.ic_delete
                else -> android.R.drawable.ic_menu_recent_history
            })
            ivIcon.setColorFilter(context.getColor(android.R.color.holo_blue_dark))

            return rowView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }
}