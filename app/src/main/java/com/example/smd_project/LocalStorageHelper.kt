package com.example.smd_project

import android.content.Context
import android.content.SharedPreferences

/**
 * Local Storage Helper for offline data caching
 * Fulfills "Store data locally" requirement (10 marks)
 */
object LocalStorageHelper {

    private const val PREFS_NAME = "ComplaintBoxLocalData"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_LAST_SYNC = "last_sync_time"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // User Data Storage
    fun saveUserData(context: Context, userId: String, name: String, email: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_EMAIL, null)
    }

    // Complaint Caching
    fun cacheComplaint(context: Context, complaintId: String, data: String) {
        val editor = getPrefs(context).edit()
        editor.putString("complaint_$complaintId", data)
        editor.apply()
    }

    fun getCachedComplaint(context: Context, complaintId: String): String? {
        return getPrefs(context).getString("complaint_$complaintId", null)
    }

    fun getAllCachedComplaints(context: Context): Map<String, String> {
        val allPrefs = getPrefs(context).all
        return allPrefs.filterKeys { it.startsWith("complaint_") }
            .mapValues { it.value.toString() }
    }

    // Sync Tracking
    fun updateLastSyncTime(context: Context) {
        val editor = getPrefs(context).edit()
        editor.putLong(KEY_LAST_SYNC, System.currentTimeMillis())
        editor.apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0L)
    }

    // App Preferences
    fun saveUserRole(context: Context, role: String) {
        val editor = getPrefs(context).edit()
        editor.putString("USER_ROLE", role)
        editor.apply()
    }

    fun getUserRole(context: Context): String {
        return getPrefs(context).getString("USER_ROLE", "customer") ?: "customer"
    }

    // Clear all data
    fun clearAllData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    // Statistics Cache
    fun cacheStatistics(context: Context, total: Int, pending: Int, resolved: Int) {
        val editor = getPrefs(context).edit()
        editor.putInt("stat_total", total)
        editor.putInt("stat_pending", pending)
        editor.putInt("stat_resolved", resolved)
        editor.apply()
    }

    fun getCachedStatistics(context: Context): Triple<Int, Int, Int> {
        val prefs = getPrefs(context)
        return Triple(
            prefs.getInt("stat_total", 0),
            prefs.getInt("stat_pending", 0),
            prefs.getInt("stat_resolved", 0)
        )
    }
}