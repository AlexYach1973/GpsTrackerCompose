package com.alexyach.compose.gpstracker

import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.alexyach.compose.gpstracker.utils.GpsEnableDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    /** Permission */
    // VERSION_CODES.Q - Android 10 (API 29)
    private val permissionsRequired =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
//            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            GpsTrackerTheme {
                // A surface container using the 'background' color from the theme
                val permissionState =
                    rememberMultiplePermissionsState(permissions = permissionsRequired)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    /** Проверяем разрешения */
                    if (permissionState.allPermissionsGranted) {
                        /** Проверяем GPS */
                        CheckLocationEnabled()

                        /** Запускаем экран */
                        MainScreen()
                        Toast.makeText(
                            this@MainActivity,
                            "All Permissions Granted",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        /** Запрашиваем разрешения */
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {


                            Button(
                                modifier = Modifier,
                                enabled = !permissionState.allPermissionsGranted, // if the permissions are NOT granted
                                onClick = {
                                    permissionState.launchMultiplePermissionRequest()
                                }
                            ) {
                                Text(stringResource(id = R.string.enable_permission))
                            }

                            /** Ссылка на настройки */
                            val annotatedText = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.DarkGray,
                                        fontSize = 16.sp
                                    )
                                ) {
                                    append(stringResource(id = R.string.setting_permission_1))
                                }

                                pushStringAnnotation(
                                    tag = "settings",
                                    annotation = "settings"
                                )

                                withStyle(
                                    style = SpanStyle(
                                        color = Color.Blue,
                                        fontSize = 16.sp
                                    )
                                ) {
                                    append(stringResource(id = R.string.setting_permission_2))
                                }
                                pop()
                            }

                            // Clickable
                            ClickableText(
                                text = annotatedText,
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations(
                                        tag = "settings", // который в buildAnnotatedString
                                        start = offset,
                                        end = offset
                                    )[0].let {
                                        // Open Settings
                                        val intentSettings =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intentSettings.data =
                                            Uri.fromParts("package", packageName, null)
                                        startActivity(intentSettings)
                                    }
                                })
                            /** END Ссылка на настройки */
                        }
                    }
                }
            }
        }
    }

    /** Включен ли Gps */
    @Composable
    private fun CheckLocationEnabled() {
        val lManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isEnabled) {
//            val openDialog = remember { mutableStateOf(false) }
            GpsEnableDialog(
                true
            ) {
                // К настройкам включения GPS
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
//                    Toast.makeText(this, "Gps Click", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Gps Enabled", Toast.LENGTH_SHORT).show()
        }
    }


}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GpsTrackerTheme {
        MainScreen()
    }
}