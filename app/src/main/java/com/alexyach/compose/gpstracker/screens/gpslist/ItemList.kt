package com.alexyach.compose.gpstracker.screens.gpslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.ui.theme.BlueSpeed
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.alexyach.compose.gpstracker.ui.theme.GreenSpeed
import com.alexyach.compose.gpstracker.ui.theme.Purple700
import com.alexyach.compose.gpstracker.ui.theme.PurpleGrey40
import com.alexyach.compose.gpstracker.ui.theme.PurpleGrey80

@Composable
fun ItemList(
    item: TrackItem,
    delete: (Boolean) -> Unit,
    click: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp, end = 8.dp)
            .clickable { click() },
        shape = RoundedCornerShape(8.dp),
        elevation = 8.dp,
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PurpleGrey80, PurpleGrey40)
                        )
                    )
            ) {
                FirstColumn(item)
                Spacer(modifier = Modifier.weight(1f))
                TwoColumn(item, delete)
            }
        }
    }
}

@Composable
fun FirstColumn(
    item: TrackItem
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = item.date,
            color = Purple700,
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic
        )

        Row(
            modifier = Modifier
        ) {
            Icon(
                tint = GreenSpeed,
                painter = painterResource(id = R.drawable.ic_speed),
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "Швидкість: ${item.speed}",
                color = GreenSpeed
            )
        }

        Row(
            modifier = Modifier
        ) {
            Icon(
                tint = BlueSpeed,
                painter = painterResource(id = R.drawable.ic_timer),
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "Час: ${item.time}",
                color = BlueSpeed
            )
        }
    }
}


@Composable
fun TwoColumn(
    item: TrackItem,
    delete: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(end = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = item.distance,
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = PurpleGrey40
        )

        Button(
            onClick = {
                delete(true)
            },
            colors = ButtonDefaults.buttonColors(Color.Transparent)
        ) {
            Icon(
                modifier = Modifier
                    .scale(1.3f)
                    .padding(top = 8.dp),
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null,
                tint = Color.Red
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GpsTrackerTheme {
        ItemList(
            TrackItem(
                id = 0,
                time = "12:00",
                distance = " 3.2 km",
                date = " 12.12.2023",
                speed = "2.1 km/h",
                geoPoints = "",
            ),
            { true },
            {}
        )
    }
}