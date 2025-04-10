package com.example.purrytify.presentation.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.purrytify.Screen
import com.example.purrytify.presentation.theme.Black

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<Screen>
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF121212)),
        containerColor = Color(0xFF121212), // Dark background matching Figma
        contentColor = Color.White
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val iconAlpha = if (isSelected) 1f else 0.5f
            val textColor = if (isSelected) Color.White else Color.Gray

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.icon),
                        contentDescription = screen.title,
                        tint = Color.White.copy(alpha = iconAlpha)
                    )
                },
                label = {
                    Text(
                        screen.title,
                        color = textColor
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}