package com.example.evmobileapp.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.R
import com.example.evmobileapp.utils.ApiClient
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SignupActivity : AppCompatActivity() {

    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val name: EditText = findViewById(R.id.name)
        val nic: EditText = findViewById(R.id.nic)
        val phone: EditText = findViewById(R.id.phone)
        val address: EditText = findViewById(R.id.address)
        val vehicleModel: EditText = findViewById(R.id.vehicle_model)
        val vehicleNumber: EditText = findViewById(R.id.vehicle_number)
        val registerButton: Button = findViewById(R.id.register_button)

        apiClient = ApiClient()

        registerButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val nameText = name.text.toString()
            val nicText = nic.text.toString()
            val phoneText = phone.text.toString()
            val addressText = address.text.toString()
            val vehicleModelText = vehicleModel.text.toString()
            val vehicleNumberText = vehicleNumber.text.toString()

            if (emailText.isNotEmpty() && passwordText.isNotEmpty() && nameText.isNotEmpty() && nicText.isNotEmpty()) {
                registerUser(emailText, passwordText, nameText, nicText, phoneText, addressText, vehicleModelText, vehicleNumberText)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, name: String, nic: String, phone: String, address: String, vehicleModel: String, vehicleNumber: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        json.put("name", name)
        json.put("nic", nic)
        json.put("phone", phone)
        json.put("address", address)
        json.put("vehicleModel", vehicleModel)
        json.put("vehicleNumber", vehicleNumber)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://10.0.2.2:5001/api/auth/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@SignupActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SignupActivity, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SignupActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
