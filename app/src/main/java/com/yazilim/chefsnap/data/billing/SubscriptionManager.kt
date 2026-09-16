package com.yazilim.chefsnap.data.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SubscriptionManager {

    private const val ENTITLEMENT_ID = "pro_access"
    private const val REVENUECAT_API_KEY = "YOUR_REVENUECAT_PUBLIC_KEY"

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _activePlanName = MutableStateFlow("Ücretsiz Plan")
    val activePlanName: StateFlow<String> = _activePlanName.asStateFlow()

    private val _expirationDateString = MutableStateFlow<String?>(null)
    val expirationDateString: StateFlow<String?> = _expirationDateString.asStateFlow()

    private val _availablePackages = MutableStateFlow<List<Package>>(emptyList())
    val availablePackages: StateFlow<List<Package>> = _availablePackages.asStateFlow()

    fun initialize(context: Context) {
        Purchases.debugLogsEnabled = true
        Purchases.configure(
            PurchasesConfiguration.Builder(context, REVENUECAT_API_KEY).build()
        )

        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
            checkProStatus(customerInfo)
        }

        fetchOfferings()
        fetchCustomerInfo()
    }

    private fun fetchCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                checkProStatus(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                Log.e("ChefSnap_Billing", "CustomerInfo error: ${error.message}")
            }
        })
    }

    fun fetchOfferings() {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                Log.e("ChefSnap_Billing", "Offerings error: ${error.message}")
            },
            onSuccess = { offerings ->
                val currentOffering = offerings.current
                if (currentOffering != null) {
                    _availablePackages.value = currentOffering.availablePackages
                }
            }
        )
    }

    private fun checkProStatus(customerInfo: CustomerInfo) {
        val entitlement = customerInfo.entitlements[ENTITLEMENT_ID]
        val hasPro = entitlement?.isActive == true
        _isProUser.value = hasPro

        if (hasPro) {
            val productIdentifier = entitlement?.productIdentifier ?: ""
            _activePlanName.value = if (productIdentifier.contains("annual", ignoreCase = true) || productIdentifier.contains("year", ignoreCase = true)) {
                "ChefSnap PRO (Yıllık)"
            } else {
                "ChefSnap PRO (Aylık)"
            }

            entitlement?.expirationDate?.let { date ->
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
                _expirationDateString.value = sdf.format(date)
            }
        } else {
            _activePlanName.value = "Ücretsiz Plan"
            _expirationDateString.value = null
        }
        Log.d("ChefSnap_Billing", "Kullanıcı Planı: ${_activePlanName.value}")
    }

    fun purchasePackage(
        activity: Activity,
        rcPackage: Package,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.purchaseWith(
            purchaseParams = com.revenuecat.purchases.PurchaseParams.Builder(activity, rcPackage).build(),
            onError = { error, userCancelled ->
                if (!userCancelled) {
                    onError(error.message)
                }
            },
            onSuccess = { _, customerInfo ->
                checkProStatus(customerInfo)
                onSuccess()
            }
        )
    }

    fun restorePurchases(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.restorePurchasesWith(
            onError = { error -> onError(error.message) },
            onSuccess = { customerInfo ->
                checkProStatus(customerInfo)
                onSuccess()
            }
        )
    }

    fun openGooglePlaySubscriptionManagement(context: Context) {
        val packageName = context.packageName
        val playStoreUri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Eğer Play Store uygulaması açılamazsa tarayıcı ile açar
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
            context.startActivity(browserIntent)
        }
    }

    // Test ve Demo Amaçlı Durum Değiştirici
    fun setProUserForTesting(isPro: Boolean, isAnnual: Boolean = true) {
        _isProUser.value = isPro
        if (isPro) {
            _activePlanName.value = if (isAnnual) "ChefSnap PRO (Yıllık)" else "ChefSnap PRO (Aylık)"
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
            // 1 yıl veya 1 ay sonrasını simüle eder
            val futureTime = System.currentTimeMillis() + if (isAnnual) 365L * 24 * 60 * 60 * 1000 else 30L * 24 * 60 * 60 * 1000
            _expirationDateString.value = sdf.format(Date(futureTime))
        } else {
            _activePlanName.value = "Ücretsiz Plan"
            _expirationDateString.value = null
        }
    }
}