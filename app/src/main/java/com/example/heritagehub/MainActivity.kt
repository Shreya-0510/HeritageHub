package com.example.heritagehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import com.example.heritagehub.ui.navigation.AppNavHost
import com.example.heritagehub.ui.theme.HeritageHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeritageHubTheme {
                AppNavHost(context = LocalContext.current)
            }
        }
    }
}
