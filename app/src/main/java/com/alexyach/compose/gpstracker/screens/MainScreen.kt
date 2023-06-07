package com.alexyach.compose.gpstracker.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.alexyach.compose.gpstracker.screens.bottomnavigation.BottomNavigationScreen
import com.alexyach.compose.gpstracker.screens.bottomnavigation.NavGraph

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationScreen(navController = navController)
        }
    )
    { innerPadding ->
        NavGraph(
            navHostController = navController,
            modifier = modifier.padding(innerPadding)
        )
    }
}



