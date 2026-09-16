package com.yazilim.chefsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yazilim.chefsnap.data.billing.SubscriptionManager
import com.yazilim.chefsnap.presentation.home.HomeScreen
import com.yazilim.chefsnap.ui.theme.ChefSnapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RevenueCat Başlatma
        SubscriptionManager.initialize(applicationContext)

        enableEdgeToEdge()
        setContent {
            ChefSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}