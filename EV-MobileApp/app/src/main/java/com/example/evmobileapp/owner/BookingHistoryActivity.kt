package com.example.evmobileapp.owner

import android.database.DataSetObserver
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.ApiClient
import com.example.evmobileapp.utils.SessionManager
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var bookingListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        sessionManager = SessionManager(this)
        apiClient = ApiClient()

        bookingListView = findViewById(R.id.booking_history_list)

        val token = sessionManager.getToken()
        if (token != null) {
            fetchBookingHistory(token)
        }
    }

    private fun fetchBookingHistory(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/evowners/profile/me/bookings")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONArray(responseBody)

                    runOnUiThread {
                        // Parse and display booking history in ListView
                        val bookingAdapter = BookingHistoryAdapter(this@BookingHistoryActivity, jsonResponse)
                        bookingListView.adapter = bookingAdapter
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
}

class BookingHistoryAdapter(bookingHistoryActivity: BookingHistoryActivity, jsonResponse: JSONArray) : ListAdapter{
    override fun registerDataSetObserver(observer: DataSetObserver?) {
        TODO("Not yet implemented")
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getItem(position: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getItemId(position: Int): Long {
        TODO("Not yet implemented")
    }

    override fun hasStableIds(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getViewTypeCount(): Int {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun areAllItemsEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEnabled(position: Int): Boolean {
        TODO("Not yet implemented")
    }

}
