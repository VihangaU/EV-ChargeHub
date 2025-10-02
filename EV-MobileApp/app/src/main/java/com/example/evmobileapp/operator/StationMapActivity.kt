package com.example.evmobileapp.operator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StationMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_map)

        // Initialize the Map
        val mapFragment = fragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getMapAsync(this)
    }

    // This method is triggered once the map is ready to be used
    override fun onMapReady(map: GoogleMap) {
        mMap = map // Assign the map to the mMap variable

        // Example: Add a marker at a predefined location (You can dynamically add stations later)
        val stationLocation = LatLng(34.0522, -118.2437) // Example location (Los Angeles)
        mMap.addMarker(MarkerOptions().position(stationLocation).title("Charging Station"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stationLocation, 12f)) // Zoom level 12
    }
}
