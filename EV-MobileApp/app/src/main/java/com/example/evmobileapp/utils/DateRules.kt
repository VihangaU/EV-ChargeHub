package com.example.evmobileapp.utils

import java.text.SimpleDateFormat
import java.util.*

class DateRules {

    // Check if the booking is within 7 days from the current date
    fun isBookingWithin7Days(bookingDate: String): Boolean {
        val currentDate = Calendar.getInstance()
        val bookingCalendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val bookingDateParsed = dateFormat.parse(bookingDate)
            bookingCalendar.time = bookingDateParsed

            // Set the calendar date to compare with current date
            val diffInMillis = bookingCalendar.timeInMillis - currentDate.timeInMillis
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

            return diffInDays in 1..7
        } catch (e: Exception) {
            return false
        }
    }

    // Check if the booking can be updated/canceled (must be 12+ hours before the reservation time)
    fun isBookingCancelableOrUpdatable(bookingDate: String, bookingTime: String): Boolean {
        val currentDate = Calendar.getInstance()
        val bookingCalendar = Calendar.getInstance()
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        try {
            val bookingDateTime = "$bookingDate $bookingTime"
            val bookingDateTimeParsed = dateTimeFormat.parse(bookingDateTime)
            bookingCalendar.time = bookingDateTimeParsed

            // Calculate the time difference in milliseconds
            val diffInMillis = bookingCalendar.timeInMillis - currentDate.timeInMillis
            val diffInHours = diffInMillis / (1000 * 60 * 60)

            return diffInHours >= 12
        } catch (e: Exception) {
            return false
        }
    }
}
