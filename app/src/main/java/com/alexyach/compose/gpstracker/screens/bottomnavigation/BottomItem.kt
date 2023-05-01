package com.alexyach.compose.gpstracker.screens.bottomnavigation

import androidx.annotation.IdRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import com.alexyach.compose.gpstracker.R

sealed class BottomItem(
    @StringRes val title: Int,
    val iconId: Int,
    val route: String
) {
    object GpsScreen: BottomItem(R.string.gps_screen, R.drawable.ic_home, "GpsScreen")
    object GpsList: BottomItem(R.string.list_screen, R.drawable.ic_list, "GpsList")
    object GpsSettings: BottomItem(R.string.settings_screen, R.drawable.ic_settings, "GpsSettings")
}
