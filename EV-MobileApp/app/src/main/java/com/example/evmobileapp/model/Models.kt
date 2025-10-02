package com.example.evmobileapp.model

// Data model for EV Owner profile
data class EvOwner(
    val id: Int,
    val name: String,
    val email: String,
    val nic: String,
    val phone: String,
    val address: String,
    val vehicleModel: String,
    val vehicleNumber: String,
    val status: String // active/inactive
)

// Data model for Booking (Reservation)
data class Booking(
    val id: Int,
    val evOwnerId: Int, // Reference to EV Owner
    val stationId: Int, // Reference to Charging Station
    val date: String,   // Reservation date (YYYY-MM-DD)
    val time: String,   // Reservation time (HH:mm)
    val status: String, // pending, approved, canceled, completed
    val qrCode: String? // Optional QR code for confirmed bookings
)

// Data model for Charging Station
data class ChargingStation(
    val id: Int,
    val name: String,
    val location: String,
    val type: String, // AC or DC
    val availableSlots: Int
)
