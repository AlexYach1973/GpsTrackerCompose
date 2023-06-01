package com.alexyach.compose.gpstracker.screens.bottomnavigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.ui.theme.Purple40
import com.alexyach.compose.gpstracker.ui.theme.Purple400
import com.alexyach.compose.gpstracker.ui.theme.PurpleGrey80

@Composable
fun BottomNavigationScreen(
    navController: NavController
) {
    val listBottomItem = listOf(
        BottomItem.GpsScreen,
        BottomItem.GpsList,
        BottomItem.GpsSettings
    )

    /** Это костыль !!!
     * При работающем Сервисе и навигации по вкладкам выдает ошибку связанную с
     * osmdroid... . Поэтому, если Сервис работает - нефиг лазать по вкладкам*/
    val serviceRunning = LocationService.isRunningLiveData.observeAsState().value

    BottomNavigation(
        backgroundColor = PurpleGrey80
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        if(!serviceRunning!!) {
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
                        Text(
                            text = stringResource(bottomItem.title),
                            fontSize = 12.sp
                        )
                    },
                    selectedContentColor = Purple400,
                    unselectedContentColor = Purple40
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                    fontSize = 18.sp,
                    color = Purple400,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily((Font(R.font.ubuntu_bold))),
                    text = "Кто не курит и не пьет- ровно дышит, сильно бьет!")
            }
        }
    }
}