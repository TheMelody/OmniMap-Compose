// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.petal.map_sample.ui

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.LatLng
import com.melody.map.petal_compose.HWMap
import com.melody.map.petal_compose.position.rememberCameraPositionState
import com.melody.petal.map_sample.contract.LocationTrackingContract
import com.melody.petal.map_sample.dialog.ShowOpenGPSDialog
import com.melody.petal.map_sample.viewmodel.LocationTrackingViewModel
import com.melody.sample.common.launcher.handlerGPSLauncher
import com.melody.sample.common.utils.requestMultiplePermission
import com.melody.sample.common.utils.showToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * 默认定位蓝点，这里我们代码动态替换了SDK默认的蓝点图片
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/9/6 11:01
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LocationTrackingScreen() {
    val viewModel: LocationTrackingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cameraPosition = rememberCameraPositionState()
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is LocationTrackingContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    val openGpsLauncher = handlerGPSLauncher(viewModel::checkGpsStatus)
    val reqGPSPermission = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onGrantAllPermission = viewModel::handleGrantLocationPermission,
        onNoGrantPermission = viewModel::handleNoGrantLocationPermission
    )

    LaunchedEffect(Unit) {
        snapshotFlow { reqGPSPermission.allPermissionsGranted }.collect {
            // 从app应用权限开关页面，打开权限，需要再检查一下GPS开关
            viewModel.checkGpsStatus()
        }
    }

    LaunchedEffect(uiState.locationLatLng) {
        if(null == uiState.locationLatLng) return@LaunchedEffect
        cameraPosition.move(CameraUpdateFactory.newLatLng(uiState.locationLatLng))
    }

    LaunchedEffect(uiState.isOpenGps, reqGPSPermission.allPermissionsGranted) {
        if(uiState.isOpenGps == true) {
            if (!reqGPSPermission.allPermissionsGranted) {
                reqGPSPermission.launchMultiplePermissionRequest()
            }else {
                viewModel.handleGrantLocationPermission()
            }
        }
    }

    if(uiState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HWMap(
            cameraPositionState = cameraPosition,
            properties = uiState.mapProperties,
            uiSettings = uiState.mapUiSettings,
            locationSource = if(uiState.mapProperties.isMyLocationEnabled) viewModel else null,
            onMapLoaded = viewModel::checkGpsStatus
        )
    }
}