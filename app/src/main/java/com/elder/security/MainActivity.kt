package com.elder.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.elder.security.ui.ElderHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElderHomeScreen(
                elderName = "张阿姨",
                todayRiskCount = 2,
                onPayClicked = {},
                onCallFamilyClicked = {},
                onCheckUpdateClicked = {}
            )
        }
    }
}
