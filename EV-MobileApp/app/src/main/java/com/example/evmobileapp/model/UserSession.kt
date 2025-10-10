package com.example.evmobileapp.model

data class UserSession(
    val email: String,
    val role: String,
    val token: String,
    val userId: String? = null
)