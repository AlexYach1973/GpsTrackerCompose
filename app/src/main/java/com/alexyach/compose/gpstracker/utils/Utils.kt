package com.alexyach.compose.gpstracker.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG

/** AlertDialog */
@Composable
fun GpsEnableDialog(
    open: Boolean,
    listener: () -> Unit
) {
    val openDialog = remember { mutableStateOf(open) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = { Text(text = stringResource(id = R.string.title_dialog)) },
            text = { Text(text = stringResource(id = R.string.text_dialog)) },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        listener.invoke()
                }
                ) {
                    Text(text = stringResource(id = R.string.ok_dialog))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.no_dialog))
                }
            }
        )
    }
}

fun checkPermission(context: Context, permission: String): Boolean {

    return when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(context, permission) -> true
        else -> false
    }
}