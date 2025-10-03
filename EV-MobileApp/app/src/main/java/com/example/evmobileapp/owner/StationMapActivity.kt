package com.example.evmobileapp.owner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class StationMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var sessionManager: SessionManager
    private lateinit var googleMap: GoogleMap
    private val stations = mutableListOf<JSONObject>()
    private var mapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_map)

        sessionManager = SessionManager(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Log.e("StationMap", "Map fragment not found")
            Toast.makeText(this, "Map loading failed", Toast.LENGTH_SHORT).show()
            finish()
        }

        val token = sessionManager.getToken()
        if (token != null) {
            fetchStations(token)
        } else {
            Toast.makeText(this, "No token found. Please login.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapReady = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setInfoWindowAdapter(this)

        // Default zoom to Sri Lanka if no stations
        if (stations.isEmpty()) {
            val sriLanka = LatLng(7.8731, 80.7718)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 6f))
        }

        // Add markers if stations loaded
        addMarkers()
    }

    private fun fetchStations(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/stations/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("StationMap", "Stations API Response: ${response.code}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "[]"
                    Log.d("StationMap", "Stations Data: $responseBody")
                    val jsonResponse = try { JSONArray(responseBody) } catch (e: Exception) {
                        Log.e("StationMap", "JSON Parse Error", e)
                        JSONArray()
                    }

                    stations.clear()
                    for (i in 0 until jsonResponse.length()) {
                        stations.add(jsonResponse.getJSONObject(i))
                    }
                    Log.d("StationMap", "Loaded ${stations.size} stations")

                    runOnUiThread {
                        if (mapReady) {
                            addMarkers()
                        }
                    }
                } else {
                    Log.e("StationMap", "API Error: ${response.code} - ${response.body?.string()}")
                    runOnUiThread {
                        Toast.makeText(this@StationMapActivity, "Failed to fetch stations", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("StationMap", "Network Failure", e)
                runOnUiThread {
                    Toast.makeText(this@StationMapActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addMarkers() {
        if (!mapReady || !::googleMap.isInitialized) {
            Log.w("StationMap", "Map not ready, skipping markers")
            return
        }

        googleMap.clear()  // Clear existing markers

        val boundsBuilder = LatLngBounds.Builder()

        for (station in stations) {
            try {
                val lat = station.optDouble("latitude", 0.0)
                val lng = station.optDouble("longitude", 0.0)
                val name = station.optString("name", "Unknown Station")
                val address = station.optString("address", "N/A")

                if (lat == 0.0 || lng == 0.0) {
                    Log.w("StationMap", "Invalid lat/lng for station: $name")
                    continue
                }

                val position = LatLng(lat, lng)
                val marker = googleMap.addMarker(MarkerOptions().position(position).title(name))
                marker?.tag = station  // Attach full station data for info window

                boundsBuilder.include(position)
            } catch (e: Exception) {
                Log.e("StationMap", "Error adding marker for station", e)
            }
        }

        // Zoom to fit all markers
        if (stations.isNotEmpty()) {
            try {
                val bounds = boundsBuilder.build()
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } catch (e: Exception) {
                Log.e("StationMap", "Error zooming to bounds", e)
                // Fallback zoom
                val center = LatLng(7.8731, 80.7718)  // Sri Lanka center
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))
            }
        }
    }

    // InfoWindowAdapter methods for popup details
    override fun getInfoWindow(marker: Marker): View? {
        return null  // Use getInfoContents for custom view
    }

    override fun getInfoContents(marker: Marker): View? {
        return try {
            val station = marker.tag as? JSONObject ?: return null
            val view = LayoutInflater.from(this).inflate(R.layout.info_window_station, null)

            val tvName = view.findViewById<TextView>(R.id.tv_station_name)
            val tvAddress = view.findViewById<TextView>(R.id.tv_station_address)

            tvName.text = station.optString("name", "Unknown")
            tvAddress.text = station.optString("address", "N/A")

            view
        } catch (e: Exception) {
            Log.e("StationMap", "Error inflating info window", e)
            // Fallback to default info window
            null
        }
    }
}