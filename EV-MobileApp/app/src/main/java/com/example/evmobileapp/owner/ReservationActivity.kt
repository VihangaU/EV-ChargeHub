package com.example.evmobileapp.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.evmobileapp.R
import com.example.evmobileapp.owner.BookingHistoryActivity
import com.example.evmobileapp.owner.OwnerDashboardActivity
import com.example.evmobileapp.owner.ProfileActivity
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import java.util.Date
import java.util.Locale

class ReservationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var rvStations: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var tvSelectedStation: TextView
    private lateinit var etDate: android.widget.EditText
    private lateinit var etStartTime: android.widget.EditText
    private lateinit var etEndTime: android.widget.EditText
    private lateinit var etNotes: android.widget.EditText
    private lateinit var btnPickDate: MaterialButton
    private lateinit var btnPickStartTime: MaterialButton
    private lateinit var btnPickEndTime: MaterialButton
    private lateinit var btnBackToStations: MaterialButton
    private lateinit var btnCreateBooking: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView
    private val stations = mutableListOf<JSONObject>()
    private var selectedStation: JSONObject? = null
    private var stationId: String = ""

    private lateinit var stationAdapter: StationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        rvStations = findViewById(R.id.rv_stations)
        tvTitle = findViewById(R.id.reservation_title)
        tvSelectedStation = findViewById(R.id.tv_selected_station)
        etDate = findViewById(R.id.et_date)
        etStartTime = findViewById(R.id.et_start_time)
        etEndTime = findViewById(R.id.et_end_time)
        etNotes = findViewById(R.id.et_notes)
        btnPickDate = findViewById(R.id.btn_pick_date)
        btnPickStartTime = findViewById(R.id.btn_pick_start_time)
        btnPickEndTime = findViewById(R.id.btn_pick_end_time)
        btnBackToStations = findViewById(R.id.btn_back_to_stations)
        btnCreateBooking = findViewById(R.id.btn_create_booking)
        bottomNavigation = findViewById(R.id.bottom_navigation_reservation)

        stationAdapter = StationAdapter(this, stations) { station ->
            onStationSelected(station)
        }
        rvStations.layoutManager = LinearLayoutManager(this)
        rvStations.adapter = stationAdapter

        setupBottomNavigation()
        setupClickListeners()

        val token = sessionManager.getToken()
        if (token != null) {
            fetchStations(token)
        } else {
            Toast.makeText(this, "No token found. Please login.", Toast.LENGTH_SHORT).show()
            finish()
        }

        showStations()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, OwnerDashboardActivity::class.java))
                    true
                }
                R.id.nav_reservations -> {
                    // Already on reservations
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
        bottomNavigation.selectedItemId = R.id.nav_reservations
    }

    private fun setupClickListeners() {
        btnPickDate.setOnClickListener { showDatePicker() }
        btnPickStartTime.setOnClickListener { showTimePicker(etStartTime) }
        btnPickEndTime.setOnClickListener { showTimePicker(etEndTime) }
        btnBackToStations.setOnClickListener { showStations() }
        btnCreateBooking.setOnClickListener { createBooking() }
    }

    private fun showDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
        val constraints = constraintsBuilder.build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(constraints)
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDate.setText(sdf.format(Date(selection)))
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker(editText: android.widget.EditText) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Select Time")
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePicker.hour, timePicker.minute)
            editText.setText(formattedTime)
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun fetchStations(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/stations")
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

                    stations.clear()
                    for (i in 0 until jsonResponse.length()) {
                        stations.add(jsonResponse.getJSONObject(i))
                    }

                    runOnUiThread {
                        stationAdapter.notifyDataSetChanged()
                        if (stations.isEmpty()) {
                            Toast.makeText(this@ReservationActivity, "No stations found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ReservationActivity, "Failed to fetch stations", Toast.LENGTH_SHORT).show()
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

    private fun onStationSelected(station: JSONObject) {
        selectedStation = station
        stationId = station.optString("id", station.optString("_id", ""))
        Log.d("Reservation", "Selected station ID: $stationId")
        tvSelectedStation.text = station.optString("name", "Unknown Station")
        tvTitle.text = "Book at ${station.optString("name", "")}"
        showForm()
    }

    private fun showStations() {
        tvTitle.text = "Select Station"
        rvStations.visibility = View.VISIBLE
        findViewById<View>(R.id.booking_form).visibility = View.GONE
        clearForm()
    }

    private fun showForm() {
        rvStations.visibility = View.GONE
        findViewById<View>(R.id.booking_form).visibility = View.VISIBLE
    }

    private fun clearForm() {
        etDate.text.clear()
        etStartTime.text.clear()
        etEndTime.text.clear()
        etNotes.text.clear()
        selectedStation = null
        stationId = ""
    }

    private fun createBooking() {
        val date = etDate.text.toString().trim()
        val startTime = etStartTime.text.toString().trim()
        val endTime = etEndTime.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        Log.d("Reservation", "Creating booking with stationId: $stationId, date: $date, start: $startTime, end: $endTime")

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || stationId.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields and select a station", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = calculateDuration(startTime, endTime)
        if (duration <= 0) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("stationId", stationId)
            put("reservationDate", date)
            put("startTime", startTime)
            put("endTime", endTime)
            put("duration", duration)
            put("notes", if (notes.isNotEmpty()) notes else JSONObject.NULL)
        }

        val client = OkHttpClient()
        val requestBody = okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/bookings")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Snackbar.make(rvStations, "Booking created successfully", Snackbar.LENGTH_LONG).show()
                        showStations()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ReservationActivity, "Failed to create booking", Toast.LENGTH_SHORT).show()
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

    private fun calculateDuration(startTime: String, endTime: String): Int {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.parse(startTime)
        val end = sdf.parse(endTime)
        val calStart = Calendar.getInstance().apply { time = start }
        val calEnd = Calendar.getInstance().apply { time = end }
        val diffInMillis = (calEnd.timeInMillis - calStart.timeInMillis).absoluteValue
        return (diffInMillis / (1000 * 60 * 60)).toInt()
    }

    private class StationAdapter(
        private val context: Context,
        private val stations: List<JSONObject>,
        private val onStationClick: (JSONObject) -> Unit
    ) : RecyclerView.Adapter<StationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_station, parent, false)
            return StationViewHolder(view, onStationClick)
        }

        override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
            holder.bind(stations[position])
        }

        override fun getItemCount(): Int = stations.size
    }

    private class StationViewHolder(
        itemView: View,
        private val onStationClick: (JSONObject) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_station_name)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_station_address)
        private val tvSlots: TextView = itemView.findViewById(R.id.tv_slots)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)

        fun bind(station: JSONObject) {
            tvName.text = station.optString("name", "Unknown")
            tvAddress.text = station.optString("address", "No address")
            val available = station.optInt("availableSlots", 0)
            val total = station.optInt("totalSlots", 0)
            tvSlots.text = "$available / $total slots available"
            tvPrice.text = "LKR ${station.optInt("pricePerHour", 0)} / hour"

            itemView.setOnClickListener { onStationClick(station) }
        }
    }
}