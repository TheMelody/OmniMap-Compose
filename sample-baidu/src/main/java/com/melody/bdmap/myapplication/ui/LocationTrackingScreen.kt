// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
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

package com.melody.bdmap.myapplication.ui

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.bdmap.myapplication.contract.LocationTrackingContract
import com.melody.bdmap.myapplication.dialog.ShowOpenGPSDialog
import com.melody.bdmap.myapplication.viewmodel.LocationTrackingViewModel
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import com.melody.sample.common.launcher.handlerGPSLauncher
import com.melody.sample.common.utils.requestMultiplePermission
import com.melody.sample.common.utils.showToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * 地图默认定位蓝点，这里我们代码动态替换了SDK默认的蓝点图片
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:31
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LocationTrackingScreen() {
    val viewModel: LocationTrackingViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
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

    LaunchedEffect(currentState.isForceLocation) {
        if(currentState.isForceLocation) return@LaunchedEffect
        cameraPosition.animate(MapStatusUpdateFactory.newLatLng(currentState.locationLatLng))
    }

    LaunchedEffect(currentState.isOpenGps, reqGPSPermission.allPermissionsGranted) {
        if(currentState.isOpenGps == true) {
            if (!reqGPSPermission.allPermissionsGranted) {
                reqGPSPermission.launchMultiplePermissionRequest()
            } else {
                viewModel.startMapLocation()
            }
        }
    }

    if(currentState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BDMap(
            cameraPositionState = cameraPosition,
            properties = currentState.mapProperties,
            uiSettings = currentState.mapUiSettings,
            locationSource = currentState.locationSource,
            onMapLoaded = viewModel::checkGpsStatus
        )
    }
}