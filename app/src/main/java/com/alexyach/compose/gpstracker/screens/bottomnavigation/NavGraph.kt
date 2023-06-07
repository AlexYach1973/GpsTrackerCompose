package com.alexyach.compose.gpstracker.screens.bottomnavigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alexyach.compose.gpstracker.screens.gpslist.GpsList
import com.alexyach.compose.gpstracker.screens.gpsscreen.GpsScreen
import com.alexyach.compose.gpstracker.screens.gpssettings.GpsSettings

@Composable
fun NavGraph(
    navHostController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navHostController,
        startDestination = BottomItem.GpsScreen.route,
        modifier = modifier
    ) {
        composable(BottomItem.GpsScreen.route) {
            GpsScreen()
        }

        composable(BottomItem.GpsList.route) {
            GpsList()
        }

        composable(BottomItem.GpsSettings.route) {
            GpsSettings()
        }
    }
}