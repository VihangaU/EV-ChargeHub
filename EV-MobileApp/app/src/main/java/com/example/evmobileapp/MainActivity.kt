package com.example.evmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evmobileapp.auth.LoginActivity
import com.example.evmobileapp.auth.SignupActivity
import com.example.evmobileapp.data.Repositories
import com.example.evmobileapp.model.UserSession
import com.example.evmobileapp.owner.OwnerDashboardActivity
import com.example.evmobileapp.operator.OperatorDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var repository: Repositories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            repository = Repositories(this)

            val currentSession: UserSession? = repository.getCurrentSession()
            if (currentSession != null) {
                when (currentSession.role) {
                    "ev_owner" -> {
                        startActivity(Intent(this, OwnerDashboardActivity::class.java))
                    }
                    "station_operator" -> {
                        startActivity(Intent(this, OperatorDashboardActivity::class.java))
                    }
                    else -> {
                        // Unsupported role, proceed to login
                    }
                }
                finish()
                return
            }
        } catch (e: Exception) {
            // Handle any database or session loading errors by clearing data and proceeding to login
            Toast.makeText(this, "Session error, redirecting to login", Toast.LENGTH_SHORT).show()
            repository.clearAllData()
        }

        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.login_button)
        val registerButton: Button = findViewById(R.id.register_button)

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}