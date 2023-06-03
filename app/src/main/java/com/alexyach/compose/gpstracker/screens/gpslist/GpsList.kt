package com.alexyach.compose.gpstracker.screens.gpslist

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.databinding.MapBinding
import com.alexyach.compose.gpstracker.ui.theme.Purple40
import com.alexyach.compose.gpstracker.ui.theme.PurpleGrey40
import com.alexyach.compose.gpstracker.ui.theme.PurpleGrey80
import com.alexyach.compose.gpstracker.ui.theme.Transparent100
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun GpsList() {
    val context = LocalContext.current
    val viewModel: GpsListViewModel = viewModel(
        factory = GpsListViewModel.Factory
    )

    val uiState: GpsListUiState = viewModel.gpsListUiState
    when (uiState) {
        is GpsListUiState.Loading -> LoadingScreen()
        is GpsListUiState.Success ->ResultScreen(
            context = context,
            viewModel = viewModel,
            list = uiState.list
        )
        is GpsListUiState.Error -> ErrorScreen()
    }
}

@Composable
private fun ResultScreen(
    context: Context,
    viewModel: GpsListViewModel,
    list: List<TrackItem>
) {

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

                ) {
                    Text(text = "Empty")
                }

            } else {

                LazyColumn(
                    modifier = Modifier.padding(bottom = 50.dp),
                ) {
                    itemsIndexed(list) { index, item ->
                        ItemList(item = item, { isDelete ->
                            if (isDelete) {
                                viewModel.delete(item)
                            }
                        }, {
                            viewModel.trackDetails = item
                            isListOrDetails = false
//                            Log.d(TAG, "GpsList Item: ${item.id}")
                        }
                        )
                    }

                }
            }
        } else {
            TrackDetails(
                context = context,
                viewModel = viewModel,
                { isListOrDetails = it }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleGrey80)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleGrey80)
    ) {
        Text(text = "ERROR")
    }

}


/** Details */
@Composable
fun TrackDetails(
    context: Context,
    viewModel: GpsListViewModel,
    backToList: (Boolean) -> Unit
) {

    // Привязываемся к XML
    MapView(
        context = context
    )

    // Работа с самой картой
    IniOsm(
        context,
        viewModel
    )

    Box {
        Column {
            TrackInfo(
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.weight(1f))

            TwoButtons(
                viewModel = viewModel,
                backToList = backToList
            )
        }
    }
}

@Composable
fun TrackInfo(
    viewModel: GpsListViewModel
) {

    val data = viewModel.trackDetails.date
    val time = viewModel.trackDetails.time
    val speed = viewModel.trackDetails.speed
    val distance = viewModel.trackDetails.distance

    Box(
        modifier = Modifier
            .padding(start = 4.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = data,
                fontSize = 20.sp,
                color = PurpleGrey40,
                fontStyle = FontStyle.Italic,
            )

            Row(
                modifier = Modifier
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    tint = PurpleGrey40,
                    painter = painterResource(id = R.drawable.ic_timer),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = "Час: $time",
                    fontSize = 20.sp,
                    color = PurpleGrey40
                )
            }

            Row(
                modifier = Modifier
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    tint = PurpleGrey40,
                    painter = painterResource(id = R.drawable.ic_speed),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = "Швидкість: $speed",
                    fontSize = 20.sp,
                    color = PurpleGrey40
                )
            }


            Row(
                modifier = Modifier
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    tint = PurpleGrey40,
                    painter = painterResource(id = R.drawable.ic_walk),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = "Шлях: $distance",
                    fontSize = 26.sp,
                    color = PurpleGrey40
                )
            }
        }
    }
}

@Composable
private fun TwoButtons(
    viewModel: GpsListViewModel,
    backToList: (Boolean) -> Unit
) {

    // StartPosition
    var startPosition by remember { mutableStateOf(false) }
    if (startPosition) {
        ToStartPosition(viewModel)
        startPosition = false
    }


    Box(
        modifier = Modifier
            .padding(start = 4.dp)
            .fillMaxWidth()

    ) {
        Column {
            FloatingActionButton(
                onClick = {
                    backToList(true)
                },
                backgroundColor = Transparent100,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.to_list),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = Purple40
                )
            }

            FloatingActionButton(
                onClick = {
                    startPosition = true
//                    toStartPosition(map, viewModel)
                },
                modifier = Modifier.padding(bottom = 50.dp),
                backgroundColor = Transparent100,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    painterResource(id = org.osmdroid.library.R.drawable.person),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = Purple40
                )
            }


        }
    }
}

@Composable
private fun ToStartPosition(viewModel: GpsListViewModel) {

    AndroidViewBinding(MapBinding::inflate) {
        // StartPosition
        map.controller.animateTo(
            viewModel.getTrackDetailsPolyline().actualPoints[0],
            10.0, 100L
        )
    }
}

@Composable
private fun IniOsm(
    context: Context,
    viewModel: GpsListViewModel
) {
//    Log.d(TAG, "IniOsm")
    val pl: Polyline = viewModel.getTrackDetailsPolyline()
    pl.outlinePaint?.color = ContextCompat.getColor(context, R.color.purple_700)

    AndroidViewBinding(MapBinding::inflate) {
        map.overlays.add(pl)
        map.controller.setZoom(17.0)

        if (pl.actualPoints.isEmpty()) {
            map.controller.animateTo(GeoPoint(50.4501, 30.5241)) // Kyiv
            Toast.makeText(context, "Шлях нульовий", Toast.LENGTH_SHORT).show()
            return@AndroidViewBinding
        }

        setMarker(context, map, pl.actualPoints)
        map.controller.animateTo(pl.actualPoints[0]) // StartPosition

        /** Compass *//*
         val compassOverlay =
             CompassOverlay(context, InternalCompassOrientationProvider(context), map)
         compassOverlay.enableCompass()
         mapDetails.overlays.add(compassOverlay)*/
    }
}

private fun setMarker(context: Context, mapDetails: MapView, list: List<GeoPoint>) {

    val startMarker = Marker(mapDetails)
    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//    startMarker.icon = getDrawable(context, org.osmdroid.library.R.drawable.person)
    startMarker.position = list[0]

    mapDetails.overlays.add(startMarker)

    val stopMarker = Marker(mapDetails)
    stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//    stopMarker.icon = getDrawable(context, org.osmdroid.library.R.drawable.person)
    stopMarker.position = list.last()
    mapDetails.overlays.add(stopMarker)
}

@Composable
private fun MapView(
    context: Context,
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null
) {
    val mapViewState = rememberMapView(context)

    AndroidView(
        factory = { mapViewState },
        modifier
    ) { mapView ->
        onLoad?.invoke(mapView)
    }
}

@Composable
private fun rememberMapView(context: Context): MapView {
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    return mapView
}




