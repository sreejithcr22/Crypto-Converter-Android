package com.codit.cryptoconverter.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.codit.cryptoconverter.App
import com.codit.cryptoconverter.presentation.screens.MainScreen
import com.codit.cryptoconverter.presentation.theme.CryptoConverterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as App).container
        setContent {
            CryptoConverterTheme {
                MainScreen(container = container)
            }
        }
    }
}
