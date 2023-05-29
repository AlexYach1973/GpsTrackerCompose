package com.alexyach.compose.gpstracker.screens.bottomnavigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.ui.theme.Purple40
import com.alexyach.compose.gpstracker.ui.theme.Teal_200

@Composable
fun BottomNavigationScreen(
    navController: NavController
) {
    val listBottomItem = listOf(
        BottomItem.GpsScreen,
        BottomItem.GpsList,
        BottomItem.GpsSettings
    )

    BottomNavigation(
        backgroundColor = Color.White
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        // Рисуем меню
        listBottomItem.forEach { bottomItem ->
            BottomNavigationItem(
                selected = currentRoute == bottomItem.route,
                onClick = {
                    navController.navigate(bottomItem.route) {
                        // Избежать несклько копий при повторном нажатии
                        launchSingleTop = true
                        // Восстанавливаем состояние при повторном выборе элемента
                        restoreState = true
                        // При BackStack возврат на главный экран
                        popUpTo(navController.graph.findStartDestination().id)

                    }

                },
                icon = {
                    Icon(
                        painter = painterResource(bottomItem.iconId),
                        contentDescription = bottomItem.route
                    )
                },
                label = {
//                    if (currentRoute== bottomItem.route) {
                    Text(
                        text = stringResource(bottomItem.title),
                        fontSize = 12.sp
                    )
//                    }

                },
                selectedContentColor = Teal_200,
                unselectedContentColor = Purple40
            )
        }
    }
}