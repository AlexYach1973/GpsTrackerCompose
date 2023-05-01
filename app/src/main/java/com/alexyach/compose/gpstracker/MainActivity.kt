package com.alexyach.compose.gpstracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    /** Permission */
    val permissionsRequired = listOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

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

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Button Permission
                        Button(
//                            modifier = Modifier.fillMaxWidth(),
                            enabled = !permissionState.allPermissionsGranted, // if the permissions are NOT granted
                            onClick = {
                                permissionState.launchMultiplePermissionRequest()
                            }
                        ) {
                            Text(
                                text = if (permissionState.allPermissionsGranted) {
                                    "All Permissions Granted"
                                } else {
                                    "Enable Permissions"
                                }
                            )
                        }
                        /** Запускаем экран */
                        if (permissionState.allPermissionsGranted) {
                            MainScreen()
                            Toast.makeText(
                                this@MainActivity,
                                "All Permissions Granted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                }
            }
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