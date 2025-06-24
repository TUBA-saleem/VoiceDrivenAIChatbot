package com.example.childeducation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.rememberNavController
import com.example.childeducation.navigation.AppNavigator
import com.example.childeducation.ui.theme.ChildEducationTheme
import com.example.childeducation.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode = mainViewModel.isDarkMode.collectAsState().value

            ChildEducationTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                AppNavigator(navController = navController)
            }
        }
    }


}
