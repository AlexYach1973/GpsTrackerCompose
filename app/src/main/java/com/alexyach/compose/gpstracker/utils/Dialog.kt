package com.alexyach.compose.gpstracker.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.alexyach.compose.gpstracker.ui.theme.Pink80
import com.alexyach.compose.gpstracker.ui.theme.Pink950
import com.alexyach.compose.gpstracker.ui.theme.Purple400
import com.alexyach.compose.gpstracker.ui.theme.Purple700
import com.alexyach.compose.gpstracker.ui.theme.Purple80

/** AlertDialog */
@Composable
fun GpsPermissionEnableDialog(
    open: Boolean,
    listener: (Boolean) -> Unit
) {
    val openDialog = remember { mutableStateOf(open) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = { Text(text = stringResource(id = R.string.title_gps_permission_dialog)) },
            text = { Text(text = stringResource(id = R.string.text_gps_permission_dialog)) },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        listener(true)
                }
                ) {
                    Text(text = stringResource(id = R.string.ok_gps_permission_dialog))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        listener(false)
                    }
                ) {
                    Text(text = stringResource(id = R.string.no_gps_permission_dialog))
                }
            }
        )
    }
}

@Composable
fun SaveTrackDialog(
    item: TrackItem,
    listenerClick: (Boolean) -> Unit,
    openSaveDialog: (Boolean) -> Unit
) {
//    Log.d(TAG, "SaveTrackDialog invoke")
        Card(
            modifier = Modifier.padding(top = 50.dp, start = 50.dp, end = 50.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Purple400,Purple80)
                        )
                    )
            ) {

                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier= Modifier.padding(16.dp),
                        text = stringResource(id = R.string.title_save_track_dialog),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple700
                    )

//                    ColumnTrackItem(item)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        TextButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Pink80, Pink950),
                                        //                                    0.0f,
                                        //                                    150.0f
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            onClick = {
                                listenerClick(true)
                                openSaveDialog(false)
                        }
                        ) {
                            Text(
                                text = stringResource(id = R.string.ok_gps_permission_dialog),
                                color = Purple700,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold

                            )
                        }

                        TextButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Pink950, Pink80), 0.0f, 150.0f
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            onClick = {
                                listenerClick(false)
                                openSaveDialog(false)
                            }
                        ) {
                            Text(
                                text = stringResource(id = R.string.no_gps_permission_dialog),
                                color = Purple700,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
private fun ColumnTrackItem(item: TrackItem) {
    Column(
        modifier = Modifier
//            .background(Color.Red)
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween

    ) {

        Text(
            text = "${stringResource(id = R.string.time)} ${item.time}",
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.velosity) + item.speed,
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.distance) + item.distance,
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.date) + item.date,
            fontSize = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GpsTrackerTheme {
//        GpsPermissionEnableDialog(true, {true})
        SaveTrackDialog(TrackItem(
            time = "12:00",
            date = "01.01.2002",
            distance = "1254",
            speed = "12",
            geoPoints = " -//-"
        ),{}, {}/*, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)*/)
    }
}
