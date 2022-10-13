package com.melody.map.myapplication.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.melody.map.myapplication.R
import com.melody.ui.components.SimpleDialog

/**
 * ShowOpenGPSDialog
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/10 15:31
 */
@Composable
internal fun ShowOpenGPSDialog(onPositiveClick: () -> Unit, onDismiss: () -> Unit) {
    SimpleDialog(
        positiveButtonResId = R.string.gd_map_location_gps_dialog_ok,
        negativeButtonResId = R.string.gd_map_location_gps_dialog_cancel,
        content = stringResource(id = R.string.gd_map_location_gps_no_open),
        onPositiveClick = onPositiveClick,
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}