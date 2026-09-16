package com.yazilim.chefsnap.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.yazilim.chefsnap.data.billing.SubscriptionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageLimitManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chefsnap_usage_prefs", Context.MODE_PRIVATE)

    companion object {
        const val FREE_DAILY_LIMIT = 3
        private const val KEY_LAST_DATE = "key_last_date"
        private const val KEY_SCAN_COUNT = "key_scan_count"
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun checkAndResetDailyCounter() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        if (lastDate != today) {
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_SCAN_COUNT, 0)
                .apply()
        }
    }

    fun getRemainingScans(): Int {
        if (SubscriptionManager.isProUser.value) {
            return Int.MAX_VALUE
        }
        checkAndResetDailyCounter()
        val currentCount = prefs.getInt(KEY_SCAN_COUNT, 0)
        return (FREE_DAILY_LIMIT - currentCount).coerceAtLeast(0)
    }

    fun canPerformScan(): Boolean {
        if (SubscriptionManager.isProUser.value) return true
        return getRemainingScans() > 0
    }

    fun incrementScanCount() {
        if (SubscriptionManager.isProUser.value) return
        checkAndResetDailyCounter()
        val currentCount = prefs.getInt(KEY_SCAN_COUNT, 0)
        prefs.edit().putInt(KEY_SCAN_COUNT, currentCount + 1).apply()
    }
}