package com.example.evmobileapp.data

import android.content.Context
import com.example.evmobileapp.model.EvOwner
import com.example.evmobileapp.model.UserSession
import com.example.evmobileapp.utils.ApiClient
import org.json.JSONObject
import okhttp3.*
import java.io.IOException

class Repositories(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val apiClient = ApiClient()

    private fun getToken(): String? {
        val session = dbHelper.getSession()
        return session?.token
    }

    // Insert or update EV Owner profile in local database
    fun saveEvOwnerProfile(evOwner: EvOwner): Long {
        return dbHelper.insertEvOwner(evOwner)
    }

    // Get EV Owner profile by NIC from local database
    fun getEvOwnerProfileByNic(nic: String): EvOwner? {
        return dbHelper.getEvOwnerByNic(nic)
    }

    // Update EV Owner profile
    fun updateEvOwnerProfile(evOwner: EvOwner): Int {
        return dbHelper.updateEvOwner(evOwner)
    }

    // Delete EV Owner profile from the local database
    fun deleteEvOwnerProfile(nic: String): Int {
        return dbHelper.deleteEvOwner(nic)
    }

    // Save user session
    fun saveSession(email: String, role: String, token: String, userId: String? = null) {
        dbHelper.saveSession(email, role, token, userId)
    }

    // Get current user session
    fun getCurrentSession(): UserSession? {
        return dbHelper.getSession()
    }

    // Clear current user session
    fun clearCurrentSession() {
        dbHelper.clearSession()
    }

    // Clear all database data
    fun clearAllData() {
        dbHelper.clearAllData()
    }

    // Fetch EV Owner profile from API
    fun fetchEvOwnerProfileFromApi(nic: String, callback: (EvOwner?) -> Unit) {
        val token = getToken()
        if (token == null) {
            callback(null)
            return
        }
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/evowners/$nic")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody!!)
                    val evOwner = EvOwner(
                        id = jsonResponse.getInt("id"),
                        name = jsonResponse.getString("name"),
                        email = jsonResponse.getString("email"),
                        nic = jsonResponse.getString("nic"),
                        phone = jsonResponse.getString("phone"),
                        address = jsonResponse.getString("address"),
                        vehicleModel = jsonResponse.getString("vehicleModel"),
                        vehicleNumber = jsonResponse.getString("vehicleNumber"),
                        status = jsonResponse.getString("status")
                    )
                    callback(evOwner)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }
        })
    }

    // Fetch list of all EV Owners from the API (admin functionality)
    fun fetchAllEvOwnersFromApi(callback: (List<EvOwner>?) -> Unit) {
        val token = getToken()
        if (token == null) {
            callback(null)
            return
        }
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/evowners")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonArray = JSONObject(responseBody!!)
                        .getJSONArray("evOwners")
                    val evOwners = mutableListOf<EvOwner>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonEvOwner = jsonArray.getJSONObject(i)
                        val evOwner = EvOwner(
                            id = jsonEvOwner.getInt("id"),
                            name = jsonEvOwner.getString("name"),
                            email = jsonEvOwner.getString("email"),
                            nic = jsonEvOwner.getString("nic"),
                            phone = jsonEvOwner.getString("phone"),
                            address = jsonEvOwner.getString("address"),
                            vehicleModel = jsonEvOwner.getString("vehicleModel"),
                            vehicleNumber = jsonEvOwner.getString("vehicleNumber"),
                            status = jsonEvOwner.getString("status")
                        )
                        evOwners.add(evOwner)
                    }
                    callback(evOwners)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }
        })
    }
}