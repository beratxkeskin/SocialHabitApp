package com.berat.socialhabitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.berat.socialhabitapp.core.designsystem.SocialHabitAppTheme
import com.berat.socialhabitapp.feature.auth.register.RegisterRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialHabitAppTheme {
                RegisterRoute(viewModel = hiltViewModel())
            }
        }
    }
}