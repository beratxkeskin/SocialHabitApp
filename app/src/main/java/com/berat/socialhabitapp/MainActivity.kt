package com.berat.socialhabitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.berat.socialhabitapp.core.designsystem.SocialHabitAppTheme
import com.berat.socialhabitapp.navigation.SocialHabitAppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialHabitAppTheme {
                val authState by mainViewModel.authState.collectAsStateWithLifecycle()
                SocialHabitAppNavigation(
                    authState = authState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}