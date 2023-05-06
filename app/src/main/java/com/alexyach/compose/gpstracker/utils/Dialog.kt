package com.alexyach.compose.gpstracker.utils

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.alexyach.compose.gpstracker.R

/** AlertDialog */
@Composable
fun GpsEnableDialog(
    open: Boolean,
    listener: (Boolean) -> Unit
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
                        listener(true)
                }
                ) {
                    Text(text = stringResource(id = R.string.ok_dialog))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        listener(false)
                    }
                ) {
                    Text(text = stringResource(id = R.string.no_dialog))
                }
            }
        )
    }
}
