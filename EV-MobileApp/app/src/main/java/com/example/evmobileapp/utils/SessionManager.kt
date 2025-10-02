package com.example.evmobileapp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("userSession", Context.MODE_PRIVATE)

    // Save the JWT token to SharedPreferences
    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("JWT", token)
        editor.apply()
    }

    // Retrieve the saved JWT token
    fun getToken(): String? {
        return sharedPreferences.getString("JWT", null)
    }

    // Clear the session (used for logout)
    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove("JWT")
        editor.apply()
    }

    // Check if the user is logged in (has a token)
    fun isLoggedIn(): Boolean {
        return sharedPreferences.contains("JWT")
    }
}
