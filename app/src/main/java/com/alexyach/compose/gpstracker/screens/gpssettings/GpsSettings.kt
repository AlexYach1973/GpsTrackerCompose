package com.alexyach.compose.gpstracker.screens.gpssettings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.alexyach.compose.gpstracker.ui.theme.Purple700

@Composable
fun GpsSettings(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize()

    ) {
        Text(
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            text = "Setting Screen"
        )

        // Линия
        Divider(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            color = Purple700,
            thickness = 2.dp
        )
        // пустое пространство
        Spacer(modifier = modifier.height(16.dp))

        ShowUpdateTime()
    }

}

@Composable
fun ShowUpdateTime(
    modifier: Modifier = Modifier
) {

    Row(
        modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.scale(scale = 1.5f),
            painter = painterResource(
                id = R.drawable.update_time
            ),
            contentDescription = "update Time",
            tint = Purple700
        )

        Text(
            modifier = modifier.padding(start = 8.dp),
            text = stringResource(id = R.string.update_time),
            fontSize = 24.sp,
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = modifier.width(8.dp))

        SpinnerMenu()
    }

}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpinnerMenu(
    gpsViewModel: GpsSettingsViewModel = viewModel(
        factory = GpsSettingsViewModel.Factory
    )
) {
    val selectionTextOption = listOf(
        UpdateTimeSelected.Sec3.text,
        UpdateTimeSelected.Sec5.text,
        UpdateTimeSelected.Sec15.text,
        UpdateTimeSelected.Sec30.text
    )

    var expanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
//        modifier = Modifier.background(Color.Red),
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
//            modifier = Modifier.background(Color.Green),
            readOnly = true,
            value = gpsViewModel.updateTimeText,
            onValueChange = {},
//            label = { Text(text = "update") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        )
        {
            selectionTextOption.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        /** Save preferences */
                        gpsViewModel.savePreferences(item)
                    }
                ) {
                    Text(text = item)

                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun GreetingPreview() {
    GpsTrackerTheme {
        GpsSettings()
    }
}

enum class UpdateTimeSelected(val text: String, val time: Int){
    Sec3("3 сек", 3000),
    Sec5("5 сек", 5000),
    Sec15("15 сек", 15000),
    Sec30("30 сек", 30000)
}