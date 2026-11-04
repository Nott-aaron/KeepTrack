package com.example.keeptrack.data.local

import android.content.Context
import java.security.MessageDigest

class UserPreferences(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "keeptrack_profile",
            Context.MODE_PRIVATE
        )

    // =========================================================
    // SESSION MANAGEMENT
    // =========================================================

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        preferences.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.edit().putBoolean("onboarding_completed", completed).apply()
    }

    // =========================================================
    // AUTHENTICATION
    // =========================================================

    fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String = "",
        gender: String = ""
    ): Boolean {
        val cleanEmail = email.trim().lowercase()
        val passwordHash = hashPassword(password)

        preferences.edit()
            .putString("name", name.trim())
            .putString("email", cleanEmail)
            .putString("password_hash", passwordHash)
            .putString("phone", phone.trim())
            .putString("gender", gender)
            .putBoolean("is_logged_in", true)
            .putBoolean("onboarding_completed", true)
            .apply()

        return true
    }

    fun loginUser(
        email: String,
        password: String
    ): Boolean {
        val cleanEmail = email.trim().lowercase()
        val storedEmail = getEmail().lowercase()
        val storedHash = preferences.getString("password_hash", "") ?: ""

        val inputHash = hashPassword(password)

        // Check if stored account matches
        if (storedEmail.isNotBlank() && cleanEmail == storedEmail) {
            if (storedHash.isBlank() || storedHash == inputHash) {
                setLoggedIn(true)
                return true
            }
        }

        // Default demo account fallback for quick evaluation/testing
        if (cleanEmail == "demo@keeptrack.com" && password == "password123") {
            if (getName().isBlank()) {
                saveProfile("Demo User", "demo@keeptrack.com", "1234567890", "Prefer not to say")
            }
            setLoggedIn(true)
            setOnboardingCompleted(true)
            return true
        }

        // If user already registered and password matches
        if (storedHash.isNotBlank() && storedHash == inputHash && cleanEmail == storedEmail) {
            setLoggedIn(true)
            return true
        }

        return false
    }

    fun logout() {
        setLoggedIn(false)
    }

    // =========================================================
    // PROFILE DATA
    // =========================================================

    fun saveProfile(
        name: String,
        email: String,
        phone: String,
        gender: String
    ) {
        preferences.edit()
            .putString("name", name)
            .putString("email", email.trim().lowercase())
            .putString("phone", phone)
            .putString("gender", gender)
            .apply()
    }

    fun getName(): String {
        return preferences.getString("name", "") ?: ""
    }

    fun getEmail(): String {
        return preferences.getString("email", "") ?: ""
    }

    fun getPhone(): String {
        return preferences.getString("phone", "") ?: ""
    }

    fun getGender(): String {
        return preferences.getString("gender", "") ?: ""
    }

    // =========================================================
    // SECURITY UTILS
    // =========================================================

    private fun hashPassword(password: String): String {
        if (password.isBlank()) return ""
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
