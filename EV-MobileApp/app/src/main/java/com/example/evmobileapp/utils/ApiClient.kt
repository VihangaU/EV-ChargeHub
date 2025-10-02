package com.example.evmobileapp.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiClient {

    val client = OkHttpClient()

    // Method to make GET requests with authorization
    fun getRequest(url: String, token: String): Response {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        return client.newCall(request).execute()
    }

    // Method to make POST requests with JSON body and authorization
    fun postRequest(url: String, jsonBody: String, token: String): Response {
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        return client.newCall(request).execute()
    }

    // Method to make PUT requests with JSON body and authorization
    fun putRequest(url: String, jsonBody: String, token: String): Response {
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .put(requestBody)
            .build()

        return client.newCall(request).execute()
    }

    // Method to make DELETE requests with authorization
    fun deleteRequest(url: String, token: String): Response {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()

        return client.newCall(request).execute()
    }
}
