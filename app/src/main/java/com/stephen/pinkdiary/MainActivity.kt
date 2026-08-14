package com.stephen.pinkdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephen.pinkdiary.ui.home.HomeScreen
import com.stephen.pinkdiary.ui.home.HomeViewModel
import com.stephen.pinkdiary.ui.theme.PinkdiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PinkdiaryApp
        setContent {
            PinkdiaryTheme {
                val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
