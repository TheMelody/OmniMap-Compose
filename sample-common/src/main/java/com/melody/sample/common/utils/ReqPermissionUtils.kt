package com.melody.sample.common.utils

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.*

/**
 * 请求多个权限
 */
@ExperimentalPermissionsApi
@Composable
fun requestMultiplePermission(
    permissions: List<String>,
    onNoGrantPermission: () -> Unit = {},
    onGrantAllPermission: () -> Unit = {}
): MultiplePermissionsState {
    return rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { mapInfo ->
            val noGrantPermissionMap = mapInfo.filter { !it.value }
            if (noGrantPermissionMap.isNotEmpty()) {
                onNoGrantPermission.invoke()
            } else {
                onGrantAllPermission.invoke()
            }
        }
    )
}