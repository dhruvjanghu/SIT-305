package com.example.taskmanagerapp
import androidx.compose.runtime.Composable

// data class to represent each item in the bottom nav
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)
