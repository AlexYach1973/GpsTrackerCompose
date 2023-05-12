package com.alexyach.compose.gpstracker.screens.gpslist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.alexyach.compose.gpstracker.ui.theme.PurpleGrey80

@Composable
fun GpsList(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val viewModel: GpsListViewModel = viewModel(
        factory = GpsListViewModel.Factory
    )

    val list: List<TrackItem> = viewModel.allGpsTrack
    var isListOrDetails by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleGrey80)
    ) {

        /** list or Details ? */
        if (isListOrDetails) {


            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center

                    // Загрузка
//            CircularProgressIndicator()
                ) {
                    Text(text = "Empty")
                }

            } else {

                LazyColumn(
                    modifier = Modifier,
                ) {
                    itemsIndexed(list) { index, item ->
                        ItemList(item = item, { isDelete ->
                            if (isDelete) {
                                viewModel.delete(item)
                            }
                        }, {
//                            viewModel.trackDetails = item
                            Log.d(TAG, " Item: ${item.distance}")
                        }
                        )
                    }

                }
            }
        } else {

        }
    }

}


